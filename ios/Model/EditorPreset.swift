/// An enum containing all prebuild editor configurations.
@objc public enum EditorPreset: Int {
  /// The apparel editor.
  case apparel

  /// The postcard editor.
  case postcard

  /// The photo editor.
  case photo

  /// The video editor.
  case video

  /// The design editor.
  case design

  // Converts enum case to the corresponding string
  public var stringValue: String {
    switch self {
    case .apparel: "apparel"
    case .postcard: "postcard"
    case .photo: "photo"
    case .video: "video"
    case .design: "design"
    }
  }

  // Initialize from string
  public static func fromString(_ rawValue: String) -> EditorPreset {
    switch rawValue {
    case "apparel": .apparel
    case "postcard": .postcard
    case "photo": .photo
    case "video": .video
    case "design": .design
    default: .design
    }
  }
}

@objc @objcMembers public class EditorPresetParser: NSObject {
  // Method to convert from NSString to EditorPreset enum
  public static func fromString(_ rawValue: NSString) -> EditorPreset {
    EditorPreset.fromString(rawValue as String)
  }

  // Method to convert from EditorPreset enum to NSString
  public static func toString(_ preset: EditorPreset) -> NSString {
    preset.stringValue as NSString
  }
}
