# Introduction

A simple android tool app to re-organize pictures to respective directories by its shot time from EXIF. Still in development.

# Build and run

just execute

    $ ./gradlew installDebug --daemon
    $ adb shell am start -c android.intent.category.LAUNCHER -a android.intent.action.MAIN -n cc.jchu.imgorg/.MainActivity

