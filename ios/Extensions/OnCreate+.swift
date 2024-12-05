import Foundation
import IMGLYEditor
import IMGLYEngine

/// An extension that provides convenience methods for `OnCreate` calls.
extension OnCreate {
  /// Populates the editor based on a given `config`, `sourceType` and `defaultScene`.
  /// - Parameters:
  ///   - settings: The `EditorSettings` containing the relevant editor information.
  ///   - sourceType: The `EditorSourceType` defining of which type the source is.
  ///   - defaultSource: The `URL` of the defaultScene if no `EditorSettings.source` is specified.
  /// - Returns: An `OnCreate.Callback`.
  @MainActor
  public static func load(
    _ settings: EditorSettings?,
    _ sourceType: EditorSourceType = .scene,
    defaultSource: URL? = nil
  ) -> OnCreate.Callback {
    { engine in
      var url: URL?

      if let source = settings?.source?.source {
        if let sourceUrl = URL(string: source), sourceUrl.scheme != nil {
          url = sourceUrl
        } else {
          throw "Specified URL is not valid."
        }
      } else if let defaultSource {
        url = defaultSource
      }

      if let url {
        switch settings?.source?.type ?? sourceType {
        case .image:
          try await engine.scene.create(fromImage: url)
          let pages = try engine.scene.getPages()
          let images = try engine.block.find(byType: .graphic)
          guard let image = images.first, let page = pages.first else { throw "Scene could not be loaded." }
          try engine.block.setFill(page, fill: engine.block.getFill(image))
          try engine.block.destroy(image)
        case .video:
          try await engine.scene.create(fromVideo: url)
        default:
          try await engine.scene.load(from: url)
        }
      } else {
        try await OnCreate.default(engine)
      }

      var assetUrl: URL?
      if let assetBaseUri = settings?.assetBaseUri {
        if let temp = URL(string: assetBaseUri), temp.scheme != nil {
          assetUrl = temp
        }
      }
      try await Self.loadDefaultAssets(baseURL: assetUrl)(engine)
    }
  }

  /// Loads the default assets for the asset library.
  /// - Parameter baseURL: The base `URL` from your CDN where the assets are hosted.
  /// - Returns: A `Callback`.
  static func loadDefaultAssets(baseURL: URL?) -> Callback {
    { engine in
      let baseURI = baseURL ?? Engine.assetBaseURL
      try await engine.addDefaultAssetSources(baseURL: baseURI)
      try await engine.addDemoAssetSources(
        exclude: [.image, .video, .audio],
        sceneMode: engine.scene.getMode(),
        withUploadAssetSources: true
      )
      try await engine.asset.addSource(TextAssetSource(engine: engine))
    }
  }
}
