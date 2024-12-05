/// A struct to declare a suitable source for the editor.
public struct Source: Codable {
  /// The source to load into the editor.
  public var source: String

  /// The source type.
  public var type: EditorSourceType
}
