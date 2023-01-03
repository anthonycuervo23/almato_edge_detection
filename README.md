# almato_scanner

A flutter plugin to detect edges of objects, scan paper, detect corners, detect rectangles. It allows cropping of the detected object image and returns the path of the cropped image.

## Usage:

### iOS

iOS 10.0 or higher is needed to use the plugin. If compiling for any version lower than 10.0 make sure to check the iOS version before using the plugin. Change the minimum platform version to 10 (or higher) in your `ios/Podfile` file.

Add below permission to the `ios/Runner/Info.plist`:

- one with the key `Privacy - Camera Usage Description` and a usage description.

Or in text format add the key:

```xml
<key>NSCameraUsageDescription</key>
<string>Can I use the camera please?</string>
```

Add to your need localizations to your app through XCode for localize actions buttons from WeScan (https://github.com/WeTransfer/WeScan/tree/master/WeScan/Resources/Localisation)

### Android

The plugin code is written in kotlin 1.5.31 so the same has to be set to the android project of yours for compilation.
Change the kotlin_version to 1.5.31 in your `android/build.gradle` file.

```
ext.kotlin_version = '1.5.31'
```

Change the minimum Android SDK version to 21 (or higher) in your `android/app/build.gradle` file.

```
minSdkVersion 21
```

### Add dependency：

Please check the latest version before installation.

```
dependencies:
  flutter:
    sdk: flutter
  almato_scanner: ^1.0.0
  permission_handler: ^10.0.0
```

### Add the following imports to your Dart code:

```
import 'package:almato_scanner/almato_scanner.dart';
```

```dart
// Check permissions and request its
bool isCameraGranted = await Permission.camera.request().isGranted;
if (!isCameraGranted) {
    isCameraGranted = await Permission.camera.request() == PermissionStatus.granted;
}

if (!isCameraGranted) {
    // Have not permission to camera
    return;
}

// Use below code for live camera detection.
       
try {
    //Make sure to await the call to detectEdge.
    String? imagePath = await AlmatoScanner.detectEdge(
        androidScanTitle: 'Scanning', // use custom localizations for android
        androidCropTitle: 'Crop',
        androidCropBlackWhiteTitle: 'Black White',
        androidCropReset: 'Reset',
    );
} catch (e) {
    print(e);
}

// Use below code for selecting directly from the gallery.

try {
    //Make sure to await the call to detectEdgeFromGallery.
    String? imagePath = await AlmatoScanner.detectEdgeFromGallery(
        androidCropTitle: 'Crop', // use custom localizations for android
        androidCropBlackWhiteTitle: 'Black White',
        androidCropReset: 'Reset',
    );
} catch (e) {
    print(e);
}

```

## Demo

<p align="center">
  <img src="https://raw.githubusercontent.com/sawankumarbundelkhandi/almato_scanner/master/screenshots/demo.gif" alt="Demo" style="margin:auto" width="372" height="686">
</p>

## Screenshots

# Android

<div style="text-align: center">
   <table>
      <tr>
         <td style="text-align: center">
            <img src="https://raw.githubusercontent.com/sawankumarbundelkhandi/almato_scanner/master/screenshots/android/1.png" width="200"/>
         </td>
         <td style="text-align: center">
            <img src="https://raw.githubusercontent.com/sawankumarbundelkhandi/almato_scanner/master/screenshots/android/2.png" width="200" />
         </td>
         <td style="text-align: center">
            <img src="https://raw.githubusercontent.com/sawankumarbundelkhandi/almato_scanner/master/screenshots/android/3.png" width="200"/>
         </td>
      </tr>
   </table>
</div>

# iOS

<div style="text-align: center">
   <table>
      <tr>
         <td style="text-align: center">
            <img src="https://raw.githubusercontent.com/sawankumarbundelkhandi/almato_scanner/master/screenshots/ios/1.PNG" width="200"/>
         </td>
         <td style="text-align: center">
            <img src="https://raw.githubusercontent.com/sawankumarbundelkhandi/almato_scanner/master/screenshots/ios/2.PNG" width="200" />
         </td>
         <td style="text-align: center">
            <img src="https://raw.githubusercontent.com/sawankumarbundelkhandi/almato_scanner/master/screenshots/ios/3.PNG" width="200"/>
         </td>
      </tr>
   </table>
</div>
   
Using these native implementation   
<a>https://github.com/WeTransfer/WeScan</a>

<a>https://github.com/KePeng1019/SmartPaperScan</a>
