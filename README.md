# React Native Video Editor and Image Editor

The `@imgly/editor-react-native` React Native module contains the prebuilt iOS and Android editors of the _Creative Engine_, the UI for CE.SDK - made accessible for React Native.
The Creative Engine enables you to build any design editing UI, automation and creative workflow.
It offers performant and robust graphics processing capabilities combining the best of layout, typography and image processing with advanced workflows centered around templating and adaptation.

Visit our [documentation](https://img.ly/docs/cesdk) for more tutorials on how to integrate and customize the engine for your specific use case.

## License

The CreativeEditor SDK is a commercial product. You can purchase a license at https://img.ly/pricing. Alternatively, you can use `null` as the license parameter to run the SDK in evaluation mode with a watermark.

## Getting Started

To get started with IMG.LY Editor for React Native, please [refer to our documentation](https://img.ly/docs/cesdk/mobile-editor/quickstart?platform=react-native). There you will learn how to integrate and configure them for your use case.

## Integration

```ts
import IMGLYEditor from '@imgly/editor-react-native';

// Configure the editor.
const settings = new EditorSettingsModel({ license: "YOUR_LICENSE_KEY" });

// Open the editor and retrieve the result.
const result = await IMGLYEditor.openEditor(settings);
```

## Changelog

To keep up-to-date with the latest changes, visit [CHANGELOG](https://img.ly/docs/cesdk/changelog/).

## Swift Package Manager (experimental)

React Native 0.87 introduced an experimental Swift Package Manager mode (`npx react-native spm`) — see the [React Native 0.87 announcement](https://reactnative.dev/blog/2026/08/11/react-native-0.87) and the [0.87.0 release notes](https://github.com/react/react-native/releases/tag/v0.87.0). This module dual-ships a SwiftPM manifest (`ios/Package.swift`) next to its podspec, so React Native's SPM autolinking resolves it out of the box. CocoaPods currently remains the default integration path; the React Native team does not recommend the SPM mode for production yet.

Expo SDK 58 supports this mode too. Set `"experiments": { "swiftPackageManager": true }`
under `expo` in `app.json`, add `expo-constants` as a direct dependency with
`npx expo install expo-constants`, then run `npx expo prebuild -p ios`. Without the direct
dependency, a package manager that does not hoist `expo-constants`, such as npm 10, installs it
under `expo`, and the first build fails with `expo-constants could not be resolved`. If you add the
dependency after a prebuild, run `npx expo prebuild -p ios --clean`. Expo SDK 58 needs iOS 16.4
or later, so an `ios.deploymentTarget` that you set in the app config, for example with
`expo-build-properties`, must be 16.4 or later.

We verified this support on React Native 0.87.0, 0.87.1 and 0.88.0, and on Expo SDK 58. We have not
verified newer versions. React Native calls this mode experimental and can change its commands,
flags, and generated layout in any release. Check the release notes of the React Native version you
use.

Note: Set the **iOS Deployment Target** of the app target in Xcode to 16.0 or later. The React Native template scaffolds iOS 15.1. On React Native 0.88, set it before you run `npx react-native spm`, or raise it later and run `npx react-native spm update`. React Native 0.88 takes the platform version of its generated packages from the app target ([iOS deployment target](https://github.com/react/react-native/blob/v0.88.0/packages/react-native/scripts/spm/__docs__/spm-scripts.md#ios-deployment-target)), so a lower target fails the build. On React Native 0.87, a lower target builds until your app code imports this module directly, for example for native customization. In an Expo app, `npx expo prebuild` writes the target from the app config, so set it there.

Note: React Native 0.87 pins its generated autolinking package to iOS 15 while this module requires iOS 16. On 0.87, raise the `platforms:` entries in `react-native/scripts/spm/generate-spm-autolinking.js` to `.iOS(.v16)` (for example with `patch-package`). React Native 0.88 needs no patch.

Note: React Native 0.87.0 downloads the Hermes runtime that the `latest-v1` tag of `hermes-compiler` names, while its own `hermes-compiler` emits bytecode version 98. When the two differ, any build with an embedded JS bundle crashes at startup. Update to React Native 0.87.1, which pins the runtime to the installed `hermes-compiler`. To stay on 0.87.0, install the `hermes-compiler` version that `npm view hermes-compiler@latest-v1 version` prints.

Note: On Expo SDK 58, this mode cannot build `expo-dev-client`: `expo-dev-launcher` and `expo-dev-menu` ship no Swift Package Manager manifest, so `npx expo prebuild` fails. Build the app without `expo-dev-client`.
