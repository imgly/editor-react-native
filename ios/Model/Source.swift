/// A struct to declare a suitable source for the editor.
public struct Source: Codable {
  /// The source to load into the editor.
  public var source: String

  /// The source type.
  public var type: EditorSourceType

  /// The resolved source URL, or `nil` if the source string is not a valid URL with a scheme.
  public var url: URL? {
    guard let url = URL(string: source), url.scheme != nil else {
      return nil
    }
    return url
  }
}
