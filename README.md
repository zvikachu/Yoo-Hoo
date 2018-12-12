# Yoo-Hoo

Yoo-Hoo is an open source android app that listens to ambient sound while you're hearing music on your headphones. Yoo-Hoo will decrease the phone's volume when certain words are heard in the background.
Yoo-Hoo is using the Google Speech API to transform speech to text.

NOTICE: Google Speech API is not free, have a looksy [here](https://cloud.google.com/speech-to-text/pricing)

## Installation

1. git clone the project
2. Open on Android Studio
3. Create a [Firebase project](https://console.firebase.google.com)
4. copy thr google-services.json file to ```Yoo-Hoo/app``` folder
5. Create an android app on your firebase project with the same package name as Yoo-Hoo is using ```com.amplez.yoo_hoo```
6. create a service account json file from firebase project settings or from [Google Cloud Console](https://console.cloud.google.com) and put it in ```Yoo-Hoo/app/src/main/res/raw/credential.json```
7. activate the speech API for your project on [Google Cloud Console](https://console.cloud.google.com)
0. das it you're done :0



## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## Limitations
Android can only have one service listen to the microphone simultiniasly, so any call recording apps will not work properly when Yoo-Hoo is active. (it's in the todo list)

## License
GPL- 2.0

#  TODO
0. fix leak when activating more than once.
1. make a button inside the foreground notification to kill the service
2. stop the service when a phone call is received
