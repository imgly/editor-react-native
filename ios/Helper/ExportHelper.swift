import IMGLYEngine
import UIKit
import UniformTypeIdentifiers

/// A helper class for exporting.
@MainActor
class ExportHelper {
  /// Saves given `data` to a file.
  /// - Parameters:
  ///   - data: The data to save.
  ///   - contentType: The `UTType` of the file.
  /// - Returns: The file `URL`.
  func saveFile(_ data: Data, _ contentType: UTType) throws -> URL {
    let fileName = "cesdk_export_artifact_" + UUID().uuidString
    let url = FileManager.default.temporaryDirectory.appendingPathComponent(fileName, conformingTo: contentType)
    try data.write(to: url, options: [.atomic])
    return url
  }

  /// Saves given scene string to a file.
  /// - Parameter scene: The scene as a `String`.
  /// - Returns: The file `URL`.
  func saveScene(_ scene: String) throws -> URL? {
    let fileName = "cesdk_export_scene_" + UUID().uuidString
    let url = FileManager.default.temporaryDirectory.appendingPathComponent(fileName, conformingTo: .plainText)
    if let data = scene.data(using: .utf8) {
      try data.write(to: url, options: [.atomic])
      return url
    }
    return nil
  }

  // MARK: - Thumbnails

  /// Generates and saves a thumbnail for a given block.
  /// - Parameters:
  ///   - id: The `DesignBlockID` of the block.
  ///   - engine: The `Engine` to use.
  ///   - height: The height of the thumbnail.
  /// - Returns: The `URL` to the thumbnail.
  func saveThumbnail(_ id: DesignBlockID, _ engine: Engine, _ height: CGFloat) async throws -> URL {
    let thumbnail = try await generateThumbnail(id, engine, height)
    let fileName = "cesdk_export_thumbnail_" + UUID().uuidString
    let url = FileManager.default.temporaryDirectory.appendingPathComponent(fileName, conformingTo: .png)
    try thumbnail?.pngData()?.write(to: url, options: [.atomic])
    return url
  }

  /// Generates a thumbnail for a given block.
  /// - Parameters:
  ///   - id: The `DesignBlockID` of the block.
  ///   - engine: The `Engine` to use.
  ///   - height: The height of the thumbnail.
  /// - Returns: The thumbnail as an `UIImage`.
  private func generateThumbnail(_ id: DesignBlockID, _ engine: Engine, _ height: CGFloat) async throws -> UIImage? {
    let stream = engine.block.generateVideoThumbnailSequence(
      id,
      thumbnailHeight: Int(height * UIScreen.main.scale),
      timeRange: 0 ... 0.1,
      numberOfFrames: 1
    )
    for try await thumbnail in stream {
      return .init(cgImage: thumbnail.image, scale: UIScreen.main.scale, orientation: .up)
    }
    return nil
  }
}
