#!/usr/bin/env python3

"""
The MIT License (MIT)

Copyright (c) 2021 Julian Chu

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
"""

import argparse
import os.path
import sys
import re
from PIL import Image, ExifTags


class FileItem:
    def __init__(self, filename, dest_dir):
        self.filename = filename
        self.dest_dir = dest_dir

    def __repr__(self):
        return "name: {n:s}, dest_dir: {t:s}".format(
            n=self.filename,
            t=self.dest_dir
        )


def parse_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument("-i",
                        dest="input_dir",
                        metavar="INPUT_DIR",
                        type=str,
                        help="Input dir to search files",
                        required=True)
    parser.add_argument('-o',
                        dest="output_dir",
                        metavar="OUTPUT_DIR",
                        type=str,
                        help="Output dir to move files",
                        required=True)
    parser.add_argument('--go',
                        dest="real_go",
                        action="store_true",
                        default=False,
                        help="Add this to really execute, otherwise just dry run")

    return parser.parse_args()


re_time = r'.*_(\d\d\d\d)(\d\d)(\d\d)_.*'  # foo_20140104_bar.mp4


def parse_item_by_filename(file):
    match_time = re.match(re_time, file)
    if not match_time:
        return
    return FileItem(filename=file, dest_dir=match_time.group(1) + "-" + match_time.group(2))


re_exif_time = r'.*(\d\d\d\d):(\d\d):(\d\d).*'  # 2021:01:01 16:36:21


def parse_item_by_exif(input_dir, file):
    filepath = os.path.join(input_dir, file)
    time_str = get_exif_time(filepath)
    if time_str is not None:
        match_time = re.match(re_exif_time, time_str)
        if not match_time:
            return
        return FileItem(filename=file, dest_dir=match_time.group(1) + "-" + match_time.group(2))


def get_exif_time(filepath):
    img = Image.open(filepath)
    exif = img.getexif()
    if exif is not None:
        exif_dict = dict(exif)
        if 0x0132 in exif_dict:
            return exif_dict[0x0132]
        elif 0x9003 in exif_dict:
            return exif_dict[0x9003]
        elif 0x9004 in exif_dict:
            return exif_dict[0x900]
    return None


def parse_items(input_dir):
    parsed_items = []
    re_ext = re.compile(r'.*(png|gif|jpg|jpeg)$', flags=re.IGNORECASE)
    for file in os.listdir(input_dir):
        if file.startswith("."):
            continue
        match_ext = re.match(re_ext, file)
        if not match_ext:
            continue

        exif_item = parse_item_by_exif(input_dir, file)
        if exif_item:
            parsed_items.append(exif_item)
            continue

        item = parse_item_by_filename(file)
        if not item:
            continue
        parsed_items.append(item)

    return parsed_items


if __name__ == '__main__':
    args = parse_arguments()
    items = parse_items(args.input_dir)

    dirs = list(sorted(set(map(lambda it: it.dest_dir, items))))
    cmds = []
    for d in dirs:
        path = os.path.join(args.output_dir, d)
        cmd = "mkdir -p " + path
        cmds.append(cmd)
    for i in items:
        from_path = os.path.join(args.input_dir, i.filename)
        to_path = os.path.join(args.output_dir, i.dest_dir)
        cmd = "mv -n {f:s} {t:s}/".format(f=from_path, t=to_path)
        cmds.append(cmd)

    if args.real_go:
        for cmd in cmds:
            os.system(cmd)
        print("finished")
    else:
        for cmd in cmds:
            print(cmd)
        print("finished dry run")
    sys.exit(0)
