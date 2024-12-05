import IMGLYApparelEditor
import IMGLYDesignEditor
import IMGLYPhotoEditor
import IMGLYPostcardEditor
import IMGLYVideoEditor
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
  public typealias Builder = (
    _ settings: EditorSettings,
    _ preset: EditorPreset?,
    _ metadata: [String: Any]?,
    _ result: @escaping EditorBuilderResult
  ) -> UIViewController

  /// The default apparel editor.
  public static func apparel() -> Builder {
    { config, _, _, result in
      @ViewBuilder func apparelEditor(settings _: EditorSettings, result: @escaping EditorBuilderResult) -> some View {
        ModalApparelEditor(settings: config, result: result)
      }
      return UIHostingController(rootView: apparelEditor(settings: config, result: result))
    }
  }

  /// The default postcard editor.
  public static func postcard() -> Builder {
    { config, _, _, result in
      @ViewBuilder func postcardEditor(settings _: EditorSettings, result: @escaping EditorBuilderResult) -> some View {
        ModalPostcardEditor(settings: config, result: result)
      }
      return UIHostingController(rootView: postcardEditor(settings: config, result: result))
    }
  }

  /// The default design editor.
  public static func design() -> Builder {
    { settings, _, _, result in
      @ViewBuilder func designEditor(settings: EditorSettings, result: @escaping EditorBuilderResult) -> some View {
        ModalDesignEditor(settings: settings, result: result)
      }
      return UIHostingController(rootView: designEditor(settings: settings, result: result))
    }
  }

  /// The default photo editor.
  public static func photo() -> Builder {
    { settings, _, _, result in
      @ViewBuilder func photoEditor(settings: EditorSettings, result: @escaping EditorBuilderResult) -> some View {
        ModalPhotoEditor(settings: settings, result: result)
      }
      return UIHostingController(rootView: photoEditor(settings: settings, result: result))
    }
  }

  /// The default video editor.
  public static func video() -> Builder {
    { settings, _, _, result in
      @ViewBuilder func videoEditor(settings: EditorSettings, result: @escaping EditorBuilderResult) -> some View {
        ModalVideoEditor(settings: settings, result: result)
      }
      return UIHostingController(rootView: videoEditor(settings: settings, result: result))
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

// MARK: - Custom

/// An extension for the editor views.
extension EditorBuilder {
  /// The message to send if the native error has been dismissed after throwing an error.
  private static let exportFailedMessage =
    "Failed to initialize the editor. Please verify that the loaded license is correct."

  /// Generates `EngineSettings` based on an `EditorSettings`.
  /// - Parameter settings: The `EditorSettings`.
  /// - Returns: The derived `EngineSettings`.
  private static func engineSettings(for settings: EditorSettings) -> EngineSettings {
    if let url = URL(string: settings.sceneBaseUri) {
      EngineSettings(license: settings.license, userID: settings.userId, baseURL: url)
    } else {
      EngineSettings(license: settings.license, userID: settings.userId)
    }
  }

  /// A modal apparel editor handling native errors.
  private struct ModalApparelEditor: View {
    @Environment(\.dismiss) private var dismiss
    private let settings: EditorSettings
    private let result: EditorBuilderResult

    init(settings: EditorSettings, result: @escaping EditorBuilderResult) {
      self.settings = settings
      self.result = result
    }

    var body: some View {
      ModalEditor {
        ApparelEditor(engineSettings(for: settings))
          .imgly.onCreate { engine in
            try await OnCreate.load(settings, defaultSource: ApparelEditor.defaultScene)(engine)
          }
          .imgly.onExport { engine, _ in
            do {
              let editorResult = try await OnExport.export(engine, .pdf)
              result(.success(editorResult))
            } catch {
              result(.failure(error))
            }
          }
      } onDismiss: { cancelled in
        if cancelled {
          result(.success(nil))
          dismiss()
        } else {
          result(.failure(exportFailedMessage))
        }
      }
    }
  }

  /// A modal postcard editor handling native errors.
  private struct ModalPostcardEditor: View {
    @Environment(\.dismiss) private var dismiss
    private let settings: EditorSettings
    private let result: EditorBuilderResult

    init(settings: EditorSettings, result: @escaping EditorBuilderResult) {
      self.settings = settings
      self.result = result
    }

    var body: some View {
      ModalEditor {
        PostcardEditor(engineSettings(for: settings))
          .imgly.onCreate { engine in
            try await OnCreate.load(settings, defaultSource: PostcardEditor.defaultScene)(engine)
          }
          .imgly.onExport { engine, _ in
            do {
              let editorResult = try await OnExport.export(engine, .pdf)
              result(.success(editorResult))
            } catch {
              result(.failure(error))
            }
          }
      } onDismiss: { cancelled in
        if cancelled {
          result(.success(nil))
          dismiss()
        } else {
          result(.failure(exportFailedMessage))
        }
      }
    }
  }

  /// A modal design editor handling native errors.
  private struct ModalDesignEditor: View {
    @Environment(\.dismiss) private var dismiss
    private let settings: EditorSettings
    private let result: EditorBuilderResult

    init(settings: EditorSettings, result: @escaping EditorBuilderResult) {
      self.settings = settings
      self.result = result
    }

    var body: some View {
      ModalEditor {
        DesignEditor(engineSettings(for: settings))
          .imgly.onCreate { engine in
            try await OnCreate.load(settings, defaultSource: DesignEditor.defaultScene)(engine)
          }
          .imgly.onExport { engine, _ in
            do {
              let editorResult = try await OnExport.export(engine, .pdf)
              result(.success(editorResult))
            } catch {
              result(.failure(error))
            }
          }
      } onDismiss: { cancelled in
        if cancelled {
          result(.success(nil))
          dismiss()
        } else {
          result(.failure(exportFailedMessage))
        }
      }
    }
  }

  /// A modal photo editor handling native errors.
  private struct ModalPhotoEditor: View {
    @Environment(\.dismiss) private var dismiss
    private let settings: EditorSettings
    private let result: EditorBuilderResult

    init(settings: EditorSettings, result: @escaping EditorBuilderResult) {
      self.settings = settings
      self.result = result
    }

    var body: some View {
      ModalEditor {
        PhotoEditor(engineSettings(for: settings))
          .imgly.onCreate { engine in
            try await OnCreate
              .load(settings, settings.source?.type ?? .image, defaultSource: PhotoEditor.defaultImage)(engine)
          }
          .imgly.onExport { engine, _ in
            do {
              let editorResult = try await OnExport.export(engine, .png)
              result(.success(editorResult))
            } catch {
              result(.failure(error))
            }
          }
      } onDismiss: { cancelled in
        if cancelled {
          result(.success(nil))
          dismiss()
        } else {
          result(.failure(exportFailedMessage))
        }
      }
    }
  }

  /// A modal video editor handling native errors.
  private struct ModalVideoEditor: View {
    @Environment(\.dismiss) private var dismiss
    private let settings: EditorSettings
    private let result: EditorBuilderResult

    init(settings: EditorSettings, result: @escaping EditorBuilderResult) {
      self.settings = settings
      self.result = result
    }

    var body: some View {
      ModalEditor {
        VideoEditor(engineSettings(for: settings))
          .imgly.onCreate { engine in
            try await OnCreate.load(settings, defaultSource: VideoEditor.defaultScene)(engine)
          }
          .imgly.onExport { engine, eventHandler in
            do {
              let editorResult = try await OnExport.exportVideo(engine, eventHandler, .mp4)
              result(.success(editorResult))
            } catch {
              result(.failure(error))
            }
          }
      } onDismiss: { cancelled in
        if cancelled {
          result(.success(nil))
          dismiss()
        } else {
          result(.failure(exportFailedMessage))
        }
      }
    }
  }
}
