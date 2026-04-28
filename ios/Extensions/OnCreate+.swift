import Foundation
import IMGLYEditor
import IMGLYEngine

// MARK: - React Native Source Loading

public extension OnCreate {
  /// Loads a source from React Native editor settings.
  ///
  /// This helper is designed to be used as the `createScene` parameter of starter kit
  /// `defaultOnCreate` callbacks. It resolves the source URL from settings and loads it
  /// based on the specified source type.
  ///
  /// ## Example
  /// ```swift
  /// builder.onCreate { engine, _ in
  ///   if let createScene = try OnCreate.loadFromSettings(settings) {
  ///     try await DesignEditorConfiguration.defaultOnCreate(createScene: createScene)(engine)
  ///   } else {
  ///     try await DesignEditorConfiguration.defaultOnCreate()(engine)
  ///   }
  /// }
  /// ```
  ///
  /// - Parameter settings: The `EditorSettings` containing the source configuration from React Native.
  /// - Returns: An `OnCreate.Callback` that loads the source, or `nil` if no source is specified
  ///   (allowing the starter kit's default scene to be used instead).
  /// - Throws: If a source is specified but its URL is invalid.
  static func loadFromSettings(
    _ settings: EditorSettings?,
  ) throws -> Callback? {
    guard let source = settings?.source else { return nil }
    guard let url = source.url else {
      throw "Specified URL is not valid: \(source.source)"
    }
    return { engine in
      switch source.type {
      case .image:
        try await engine.scene.create(fromImage: url)
      case .video:
        try await engine.scene.create(fromVideo: url)
      case .scene:
        try await engine.scene.load(from: url)
      }
    }
  }
}
