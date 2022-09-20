# human-milk-bank-app

An android reference application that leverages Google's FHIR Android SDK and WHO's Neo-natal Digital Adaptation Kit (NNDAK) to build a standard's based mobile solution to link Newborn Units and Human Milk Banks.

## Getting Started 
You are welcome to use the app as is, or you can build it from source.The app is built using Android Studio and the latest version of the Android SDK.
* [Android Studio 3.5.3](https://developer.android.com/studio)
* [Android SDK 29](https://developer.android.com/studio/releases/platforms)
* [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [FHIR Android SDK 0.5.0](https://github.com/google/android-fhir)

### Installing and Setting
Ensure you have android studio installed on your machine.

Clone the repository below

```
git clone https://github.com/IntelliSOFT-Consulting/human-milk-bank.git
```

Open the project in android studio and sync the dependencies.

## FHIR Server Configuration
The app is configured to use the [HAPI FHIR Server](https://hapifhir.io/) as the FHIR server. You can use any FHIR server of your choice. To configure the app to use your FHIR server, you will need to change the following.
```
Base URL of the FHIR server in [ServerConfiguration](https://github.com/IntelliSOFT-Consulting/human-milk-bank/blob/main/app/src/main/java/com/intellisoft/nndak/FhirApplication.kt) file.
```

## Deployment
To create a release build, you will need to create a keystore and add it to the project. You can follow the instructions [here](https://developer.android.com/studio/publish/app-signing#generate-key) to create a keystore. 

Once you have created the keystore, add the keystore file to the project and update the following in the [gradle.properties]()
```
storeFile=keystore.jks
storePassword=your_keystore_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

To create a release build, run the following command in the terminal
```
./gradlew assembleRelease
```
  
## Built With
* [Android Studio](https://developer.android.com/studio) - The IDE used
* [Gradle](https://gradle.org/) - Dependency Management
  
## Acknowledgments 
* [FHIR Android SDK](https://github.com/google/android-fhir) - The FHIR SDK used to build the app

 