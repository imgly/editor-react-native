import IMGLYEditor
import IMGLYEngine
import SwiftUI
import UIKit

/// The `EditorBuilder` is used to build different editor configurations.
public enum EditorBuilder {
  /// A closure used to return a `Result<EditorResult?, Error>` from the editor.
  public typealias EditorBuilderResult = (Result<EditorResult?, Error>) -> Void

  /// Closure allowing to return a custom UI based on various input.
  /// - Parameters:
  ///   - settings: The `EditorSettings`.
  ///   - preset: The `EditorPreset` pointing to a prebuild UI configuration.
  ///   - metadata: The metadata.
  ///   - result: The `EditorBuilderResult` used to return a result.
  /// - Returns: A `UIViewController`.
  public typealias Builder = @MainActor (
    _ settings: EditorSettings,
    _ preset: EditorPreset?,
    _ metadata: [String: Any]?,
    _ result: @escaping EditorBuilderResult
  ) -> UIViewController

  /// The default design editor.
  public static func design() -> Builder {
    { settings, _, _, result in
      UIHostingController(rootView: NavigationView {
        Editor(engineSettings(for: settings))
          .imgly.configuration {
            DesignEditorConfiguration { builder in
              builder.onCreate { engine, _ in
                if let createScene = try OnCreate.loadFromSettings(settings) {
                  try await DesignEditorConfiguration.defaultOnCreate(createScene: createScene)(engine)
                } else {
                  try await DesignEditorConfiguration.defaultOnCreate()(engine)
                }
              }
              builder.onExport { engine, _, _ in
                do {
                  let editorResult = try await OnExport.export(engine, .pdf)
                  result(.success(editorResult))
                } catch {
                  result(.failure(error))
                }
              }
            }
            ModalEditorConfiguration(result: result)
          }
      }.navigationViewStyle(.stack))
    }
  }

  /// The default photo editor.
  public static func photo() -> Builder {
    { settings, _, _, result in
      UIHostingController(rootView: NavigationView {
        Editor(engineSettings(for: settings))
          .imgly.configuration {
            PhotoEditorConfiguration { builder in
              builder.onCreate { engine, _ in
                if let createScene = try OnCreate.loadFromSettings(settings) {
                  try await PhotoEditorConfiguration.defaultOnCreate(createScene: createScene)(engine)
                } else {
                  try await PhotoEditorConfiguration.defaultOnCreate()(engine)
                }
              }
              builder.onExport { engine, _, _ in
                do {
                  let editorResult = try await OnExport.export(engine, .png)
                  result(.success(editorResult))
                } catch {
                  result(.failure(error))
                }
              }
            }
            ModalEditorConfiguration(result: result)
          }
      }.navigationViewStyle(.stack))
    }
  }

  /// The default video editor.
  public static func video() -> Builder {
    { settings, _, _, result in
      UIHostingController(rootView: NavigationView {
        Editor(engineSettings(for: settings))
          .imgly.configuration {
            VideoEditorConfiguration { builder in
              builder.onCreate { engine, _ in
                if let createScene = try OnCreate.loadFromSettings(settings) {
                  try await VideoEditorConfiguration.defaultOnCreate(createScene: createScene)(engine)
                } else {
                  try await VideoEditorConfiguration.defaultOnCreate()(engine)
                }
              }
              builder.onExport { engine, eventHandler, _ in
                do {
                  let editorResult = try await OnExport.exportVideo(engine, eventHandler, .mp4)
                  result(.success(editorResult))
                } catch {
                  if error is CancellationError { return }
                  result(.failure(error))
                }
              }
            }
            ModalEditorConfiguration(result: result)
          }
      }.navigationViewStyle(.stack))
    }
  }

  /// The default apparel editor.
  public static func apparel() -> Builder {
    { settings, _, _, result in
      UIHostingController(rootView: NavigationView {
        Editor(engineSettings(for: settings))
          .imgly.configuration {
            ApparelEditorConfiguration { builder in
              builder.onCreate { engine, _ in
                if let createScene = try OnCreate.loadFromSettings(settings) {
                  try await ApparelEditorConfiguration.defaultOnCreate(createScene: createScene)(engine)
                } else {
                  try await ApparelEditorConfiguration.defaultOnCreate()(engine)
                }
              }
              builder.onExport { engine, _, _ in
                do {
                  let editorResult = try await OnExport.export(engine, .pdf)
                  result(.success(editorResult))
                } catch {
                  result(.failure(error))
                }
              }
            }
            ModalEditorConfiguration(result: result)
          }
      }.navigationViewStyle(.stack))
    }
  }

  /// The default postcard editor.
  public static func postcard() -> Builder {
    { settings, _, _, result in
      UIHostingController(rootView: NavigationView {
        Editor(engineSettings(for: settings))
          .imgly.configuration {
            PostcardEditorConfiguration { builder in
              builder.onCreate { engine, _ in
                if let createScene = try OnCreate.loadFromSettings(settings) {
                  try await PostcardEditorConfiguration.defaultOnCreate(createScene: createScene)(engine)
                } else {
                  try await PostcardEditorConfiguration.defaultOnCreate()(engine)
                }
              }
              builder.onExport { engine, _, _ in
                do {
                  let editorResult = try await OnExport.export(engine, .pdf)
                  result(.success(editorResult))
                } catch {
                  result(.failure(error))
                }
              }
            }
            ModalEditorConfiguration(result: result)
          }
      }.navigationViewStyle(.stack))
    }
  }

  /// A custom editor implementation.
  /// - Parameter contentProvider:
  /// - Returns: A `Builder` providing the custom view.
  public static func custom(_ contentProvider: @escaping (
    _ settings: EditorSettings,
    _ preset: EditorPreset?,
    _ metadata: [String: Any]?,
    _ result: @escaping EditorBuilderResult
  ) -> some View) -> Builder {
    { settings, preset, metadata, result in
      let hostingController = UIHostingController(rootView: contentProvider(settings, preset, metadata, result))
      return hostingController
    }
  }
}

// MARK: - Helpers

extension EditorBuilder {
  /// Generates `EngineSettings` based on an `EditorSettings`.
  /// - Parameter settings: The `EditorSettings`.
  /// - Returns: The derived `EngineSettings`.
  private static func engineSettings(for settings: EditorSettings) -> EngineSettings {
    if let url = URL(string: settings.baseUri) {
      EngineSettings(license: settings.license, userID: settings.userId, baseURL: url)
    } else {
      EngineSettings(license: settings.license, userID: settings.userId)
    }
  }
}
