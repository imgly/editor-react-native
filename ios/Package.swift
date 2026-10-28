// swift-tools-version: 6.1
// 6.1 is the parse floor for the trailing-comma syntax SwiftFormat enforces (SE-0439). The
// toolchain floor comes from the IMGLYUI-swift dependency (tools 6.3.1) — do not lower this.

// Hand-authored manifest for React Native's experimental Swift Package Manager
// mode (React Native >= 0.87, `npx react-native spm`). CocoaPods
// (IMGLYEditorModule.podspec) remains the supported default integration.
//
// React Native's SPM autolinking consumes this manifest through a symlink at
// <app>/ios/build/generated/autolinking/libs/EditorReactNative -> this
// directory, so the relative package references below resolve against that
// location. React Native documents this layout for libraries that ship their
// own manifest under "Self-managed community packages" in
// https://github.com/react/react-native/blob/v0.88.0/packages/react-native/scripts/spm/__docs__/spm-scripts.md
// The package and product must be named EditorReactNative. React Native 0.87
// derives that name from the npm package, and 0.88 reads it from
// `swiftpmConfig.name` in package.json, otherwise it uses the podspec name.
//
// The Objective-C++ bridge and the Swift implementation are separate targets
// because SwiftPM cannot compile mixed-language targets (SE-0403). The bridge
// resolves the Swift implementation at runtime (IMGLYEditorModuleSupport.h),
// so no Objective-C -> Swift compile-time dependency exists.
//
// The StarterKits directory holds tooling-provided sources and resources; do not
// hand-edit it. Its entry files are excluded from the target below because they
// reference app-side symbols that do not exist in the module.

import PackageDescription

let package = Package(
  name: "EditorReactNative",
  platforms: [
    .iOS(.v16),
  ],
  products: [
    .library(name: "EditorReactNative", targets: ["IMGLYEditorModule", "IMGLYEditorModuleObjC"]),
  ],
  dependencies: [
    .package(name: "ReactNative", path: "../../../../xcframeworks"),
    .package(name: "React-GeneratedCode", path: "../../../ios"),
    // Pinned exactly: the module and IMGLYUI are versioned in lockstep, matching the podspec.
    // The release bump rewrites this text. Xcode's cache of the evaluated manifest ignores the
    // files it reads, so a version read from package.json stays old after an upgrade.
    .package(url: "https://github.com/imgly/IMGLYUI-swift.git", exact: "1.85.0"),
  ],
  targets: [
    .target(
      name: "IMGLYEditorModuleObjC",
      dependencies: [
        .product(name: "ReactHeaders", package: "ReactNative"),
        .product(name: "ReactNativeHeaders", package: "ReactNative"),
        .product(name: "ReactNativeDependenciesHeaders", package: "ReactNative"),
        .product(name: "ReactAppHeaders", package: "React-GeneratedCode"),
      ],
      path: "bridge",
      publicHeadersPath: "include",
      cxxSettings: [
        // React Native's SwiftPM autolinking defines this for no target, while
        // CocoaPods defines it for every one. Without it this module would
        // compile its legacy bridge branch and run behind the TurboModule
        // interop layer, so the integration path would decide the dispatch
        // mechanism. The codegen'd spec headers the branch needs come from the
        // ReactAppHeaders product above.
        .define("RCT_NEW_ARCH_ENABLED", to: "1"),
        .define("DEBUG", .when(configuration: .debug)),
        .define("NDEBUG", .when(configuration: .release)),
      ],
      linkerSettings: [.linkedFramework("Foundation")],
    ),
    .target(
      name: "IMGLYEditorModule",
      dependencies: [
        "IMGLYEditorModuleObjC",
        // The umbrella product is the only product that vends the IMGLYEditor module.
        .product(name: "IMGLYUI", package: "IMGLYUI-swift"),
      ],
      path: ".",
      exclude: [
        "bridge",
        "StarterKits/Apparel/ApparelEditorStarterKit.swift",
        "StarterKits/Design/DesignEditorStarterKit.swift",
        "StarterKits/Photo/PhotoEditorStarterKit.swift",
        "StarterKits/Postcard/PostcardEditorStarterKit.swift",
        "StarterKits/Video/VideoEditorStarterKit.swift",
      ],
      resources: [
        .copy("StarterKits/apparel-ui-b-empty.scene"),
        .copy("StarterKits/design-ui-empty.scene"),
        .copy("StarterKits/photo-ui-empty.png"),
        .copy("StarterKits/postcard-empty.scene"),
        .copy("StarterKits/video-empty.scene"),
      ],
    ),
  ],
  // The module sources are written for the Swift 5 language mode.
  swiftLanguageModes: [.v5],
  cxxLanguageStandard: .cxx20,
)
