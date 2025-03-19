# React Native Video Editor and Image Editor

The `@imgly/editor-react-native` React Native module contains the prebuilt iOS and Android editors of the _Creative Engine_, the UI for CE.SDK - made accessible for React Native.
The Creative Engine enables you to build any design editing UI, automation and creative workflow.
It offers performant and robust graphics processing capabilities combining the best of layout, typography and image processing with advanced workflows centered around templating and adaptation.

Visit our [documentation](https://img.ly/docs/cesdk) for more tutorials on how to integrate and customize the engine for your specific use case.

## License

The CreativeEditor SDK is a commercial product. To use it and get access you need to unlock the SDK with a license file. You can purchase a license at https://img.ly/pricing.

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
