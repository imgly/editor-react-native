import Foundation
#if canImport(IMGLYEditorModuleObjC)
  // Swift Package Manager builds the Objective-C++ bridge as a separate target.
  // Under CocoaPods both languages share one module, so no import is needed.
  import IMGLYEditorModuleObjC
#endif

/// The Swift implementation behind the Objective-C++ React Native bridge.
///
/// The bridge resolves this class at runtime by its stable Objective-C name
/// (see `IMGLYEditorModuleImplementationProvider`), so no static
/// Objective-C → Swift reference exists. This is what allows building the
/// module with Swift Package Manager, which cannot compile mixed
/// Swift/Objective-C++ targets (SE-0403).
@objc(IMGLYEditorModuleDefaultImplementation)
final class IMGLYEditorModuleDefaultImplementation: NSObject, IMGLYEditorModuleImplementation {
  func openEditor(
    withSettings settings: [AnyHashable: Any],
    preset: String?,
    metadata: [AnyHashable: Any]?,
    resolve: @escaping IMGLYEditorResolveBlock,
    reject: @escaping IMGLYEditorRejectBlock,
  ) {
    guard let settings = settings as? [String: Any],
          let convertedSettings = EditorSettings.fromDictionary(settings) else {
      reject(IMGLYErrorParsing, IMGLYErrorParsingMessage, nil)
      return
    }
    // Unknown and missing presets both fall back to the design editor,
    // matching `EditorPreset.fromString`.
    let convertedPreset = EditorPresetParser.fromString((preset ?? "design") as NSString)
    let metadata = metadata as? [String: Any]

    DispatchQueue.main.async {
      MainActor.assumeIsolated {
        IMGLYEditorModuleSwiftAdapter.shared.openEditor(
          convertedPreset,
          settings: convertedSettings,
          metadata: metadata,
        ) { result, error in
          if let result {
            // Missing fields resolve as null, matching the Android module.
            // The annotated binding keeps the heterogeneous literal out of the
            // surrounding closure expression, which overwhelms the type checker
            // ("failed to produce diagnostic" on the Xcode 26.6 toolchain).
            let payload: [String: Any] = [
              "artifact": result.artifact ?? NSNull(),
              "scene": result.scene ?? NSNull(),
              "thumbnail": result.thumbnail ?? NSNull(),
              "metadata": result.metadata,
            ]
            resolve(payload)
          } else if let error {
            reject(IMGLYErrorExportFailed, IMGLYErrorExportFailedMessage, error)
          } else {
            resolve(NSNull())
          }
        }
      }
    }
  }
}
