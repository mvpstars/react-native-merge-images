# react-native-merge-images

## Getting started

`$ npm install react-native-merge-images --save`

### Mostly automatic installation

`$ react-native link react-native-merge-images`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-merge-images` and add `RNMergeImages.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNMergeImages.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.mvpstars.RNMergeImagesPackage;` to the imports at the top of the file
  - Add `new RNMergeImagesPackage()` to the list returned by the `getPackages()` method. Add a comma to the previous item if there's already something there.
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-merge-images'
  	project(':react-native-merge-images').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-merge-images/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-merge-images')
  	```


## Usage
```javascript
import RNMergeImages from 'react-native-merge-images';

// TODO: What to do with the module?
RNMergeImages;
```
  