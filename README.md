# Build and run

just execute

    $ ./gradlew installDebug --daemon
    $ adb shell am start -c android.intent.category.LAUNCHER -a android.intent.action.MAIN -n org.zeroxlab.imgorg/.MainActivity

