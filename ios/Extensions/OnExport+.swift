import Foundation
import IMGLYEditor
import IMGLYEngine

/// An extension that provides convenience methods for `OnExport` calls.
public extension OnExport {
  /// Exports the asset to an `EditorResult`.
  /// - Parameters:
  ///   - engine: The `Engine` that should be used.
  ///   - mimeType: The `MIMEType` of the exported artifact.
  ///   - thumbnailHeight: The height of the generated thumbnail.
  /// - Returns: A populates `EditorResult`.
  @MainActor
  static func export(_ engine: Engine, _ mimeType: MIMEType,
                     _ thumbnailHeight: CGFloat = 100) async throws -> EditorResult {
    let helper = ExportHelper()
    let (data, type) = try await OnExport.export(engine, mimeType: mimeType)
    let file = try helper.saveFile(data, type)
    let scene = try await engine.scene.saveToString()
    let localScene = try helper.saveScene(scene)

    var thumbnail: URL?
    if let firstPage = try engine.scene.getPages().first {
      thumbnail = try await helper.saveThumbnail(firstPage, engine, thumbnailHeight)
    }
    return EditorResult(
      scene: localScene?.absoluteString,
      artifact: file.absoluteString,
      thumbnail: thumbnail?.absoluteString
    )
  }

  /// Exports a video to an `EditorResult`.
  /// - Parameters:
  ///   - engine: The `Engine` that should be used.
  ///   - eventHandler: The `EditorEventHandler` used for event handling.
  ///   - mimeType: The `MIMEType` of the exported artifact.
  ///   - thumbnailHeight: The height of the generated thumbnail.
  /// - Returns: A populates `EditorResult`.
  @MainActor
  static func exportVideo(
    _ engine: Engine,
    _ eventHandler: EditorEventHandler,
    _ mimeType: MIMEType,
    _ thumbnailHeight: CGFloat = 100
  ) async throws -> EditorResult {
    let helper = ExportHelper()
    let (data, type) = try await OnExport.exportVideo(engine, eventHandler, mimeType: mimeType)
    let file = try helper.saveFile(data, type)
    let scene = try await engine.scene.saveToString()
    let localScene = try helper.saveScene(scene)

    var thumbnail: URL?
    if let firstPage = try engine.scene.getPages().first {
      thumbnail = try await helper.saveThumbnail(firstPage, engine, thumbnailHeight)
    }
    eventHandler.send(.exportCompleted {})
    return EditorResult(
      scene: localScene?.absoluteString,
      artifact: file.absoluteString,
      thumbnail: thumbnail?.absoluteString
    )
  }
}
