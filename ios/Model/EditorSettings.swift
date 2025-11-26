/// A struct containing all necessary settings to setup the editor.
@objc @objcMembers public class EditorSettings: NSObject, Codable {
  /// The license key. Pass `nil` to run the SDK in evaluation mode with a watermark.
  public let license: String?

  /// The base uri for the assets included in the scene that have a relative source.
  public let sceneBaseUri: String

  /// The base uri of the default assets in the asset library.
  public let assetBaseUri: String?

  /// The id of the current user.
  public let userId: String?

  /// The source to load into the editor.
  public var source: Source?

  /// Creates a new instance from a given dictionary.
  ///
  /// - Parameters:
  ///   - dictionary: The dictionary to convert the instance from.
  /// - Returns: The `EditorSettings` instance.
  public static func fromDictionary(_ dictionary: [String: Any]) -> EditorSettings? {
    do {
      let data = try JSONSerialization.data(withJSONObject: dictionary, options: [])
      let settings = try JSONDecoder().decode(EditorSettings.self, from: data)
      return settings
    } catch {
      print("Error decoding dictionary: \(error)")
      return nil
    }
  }
}
