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
// location. This is React Native's documented contract for libraries that ship
// their own manifest (react-native/scripts/spm/__doc__/spm-header-paths-contract.md);
// the package and product must be named after the npm package
// (@imgly/editor-react-native -> EditorReactNative).
//
// The Objective-C++ bridge and the Swift implementation are separate targets
// because SwiftPM cannot compile mixed-language targets (SE-0403). The bridge
// resolves the Swift implementation at runtime (IMGLYEditorModuleSupport.h),
// so no Objective-C -> Swift compile-time dependency exists.
//
// The StarterKits directory holds tooling-provided sources and resources; do not
// hand-edit it. Its entry files are excluded from the target below because they
// reference app-side symbols that do not exist in the module.

import Foundation
import PackageDescription

// Single-source the version from package.json, like the podspec does, so
// release automation bumps the IMG.LY dependency pin together with the
// package version. React Native's autolinking references this manifest
// through a symlink, so resolve it before walking up to the package root.
let version: Version = {
  let packageJSON = URL(fileURLWithPath: #filePath)
    .resolvingSymlinksInPath()
    .deletingLastPathComponent()
    .deletingLastPathComponent()
    .appendingPathComponent("package.json")
  guard let data = try? Data(contentsOf: packageJSON),
        let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
        let versionString = json["version"] as? String,
        let version = Version(versionString) else {
    fatalError("Unable to read the package version from \(packageJSON.path).")
  }
  return version
}()

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
    .package(url: "https://github.com/imgly/IMGLYUI-swift.git", exact: version),
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
