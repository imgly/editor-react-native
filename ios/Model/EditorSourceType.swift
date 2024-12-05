/// The `EditorSourceType` defines the type of source that is loaded into the editor.
public enum EditorSourceType: String, Codable {
  /// A `.scene` file.
  case scene

  /// An image file, e.g. `.png`.
  case image

  /// A video file, e.g. `.mp4`.
  case video
}
