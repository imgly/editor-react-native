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

The notes below describe React Native **0.87.0**. That is the version this
support was built and verified against. We have not verified newer React Native
versions, so we do not know whether these workarounds still apply there. React
Native calls this mode experimental and can change its commands, flags, and
generated layout in any release. Check the release notes of the React Native
version you use.

Note: React Native 0.87 pins its generated autolinking package to iOS 15 while this module requires iOS 16. Until the floor is derived from the linked libraries upstream, raise the `platforms:` entries in `react-native/scripts/spm/generate-spm-autolinking.js` to `.iOS(.v16)` (for example with `patch-package`).

Note: The app target itself must also build with an iOS 16 deployment target. The React Native template scaffolds a lower one, which works until your app code imports this module directly — for example for native customization — and then fails with a minimum-deployment-target error. Raise the **iOS Deployment Target** of the app target in Xcode to 16.0.

Note: React Native 0.87.0 ships `hermes-compiler` for bytecode version 98 while its prebuilt SwiftPM Hermes runtime expects 99, so any build with an embedded JS bundle crashes at startup. Install `hermes-compiler@^260318099` until a React Native release aligns the two.
