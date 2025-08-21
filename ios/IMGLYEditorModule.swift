import UIKit

/// The iOS implementation for the `@imgly/editor-react-native` React Native module.
@objc(IMGLYEditorModuleSwiftAdapter) @objcMembers public class IMGLYEditorModuleSwiftAdapter: NSObject {
  // MARK: - Typealias

  /// A closure to specify an `EditorBuilder.Builder` based on a given `preset` and `metadata`.
  public typealias IMGLYBuilderClosure = (_ preset: EditorPreset?, _ metadata: [String: Any]?) -> EditorBuilder.Builder

  // MARK: - Properties

  /// The shared instance.
  public static var shared = IMGLYEditorModuleSwiftAdapter()

  /// The `IMGLYBuilderClosure` to use for UI creation.
  public var builderClosure: IMGLYBuilderClosure?

  /// The `UIViewController` hosting the editor.
  private var presentationController: UIViewController?

  /// The current `EditorBuilder.EditorBuilderResult`.
  private var currentResultHandler: EditorBuilder.EditorBuilderResult?

  // MARK: - Editor

  /// Opens the creative editor.
  /// - Parameters:
  ///   - preset: The `EditorPreset` used to determine which UI preset to use.
  ///   - settings: The `EditorSettings` containing all relevant information for the editor.
  ///   - metadata: Any custom metadata used for the `EditorBuilder.Builder`.
  ///   - completion: The completion handler to execute once the editor failed, cancelled or exported.
  public func openEditor(
    _ preset: EditorPreset,
    settings: EditorSettings,
    metadata: [String: Any]?,
    completion: @escaping (_ result: EditorResult?, _ error: NSError?) -> Void,
  ) {
    _openEditor(preset, settings: settings, metadata: metadata, completion: completion)
  }

  /// Opens the creative editor.
  /// - Parameters:
  ///   - settings: The `EditorSettings` containing all relevant information for the editor.
  ///   - metadata: Any custom metadata used for the `EditorBuilder.Builder`.
  ///   - completion: The completion handler to execute once the editor failed, cancelled or exported.
  public func openEditor(
    settings: EditorSettings,
    metadata: [String: Any]?,
    completion: @escaping (_ result: EditorResult?, _ error: NSError?) -> Void,
  ) {
    _openEditor(nil, settings: settings, metadata: metadata, completion: completion)
  }

  private func _openEditor(
    _ preset: EditorPreset?,
    settings: EditorSettings,
    metadata: [String: Any]?,
    completion: @escaping (_ result: EditorResult?, _ error: NSError?) -> Void,
  ) {
    let builder = builderClosure?(preset, metadata) ?? builderForPreset(preset)
    // Assign the `currentResultHandler` in order to manage the calls to the completion handler.
    currentResultHandler = { [weak self] result in
      switch result {
      case let .success(artifact):
        completion(artifact, nil)
      case let .failure(error):
        completion(nil, error as NSError)
      }
      // Remove the handler to prevent multiple invokations.
      self?.currentResultHandler = nil
      self?.presentationController?.presentingViewController?.dismiss(animated: true)
    }

    presentationController = builder(settings, preset, metadata) { [weak self] result in
      self?.currentResultHandler?(result)
    }
    presentationController?.modalPresentationStyle = .fullScreen

    if let presentationController, let windowScene = UIApplication.shared.connectedScenes
      .filter({ $0.activationState == .foregroundActive })
      .first as? UIWindowScene {
      if let rootViewController = windowScene.windows
        .filter(\.isKeyWindow).first?.rootViewController {
        rootViewController.present(presentationController, animated: true, completion: nil)
      }
    }
  }

  // MARK: - Helpers

  /// Returns the suitable `EditorBuilder.Builder` for a given `EditorPreset`.
  /// - Parameter preset: The `EditorPreset`.
  /// - Returns: The suitable `EditorBuilder.Builder`. Defaults to `EditorPreset.design`.
  private func builderForPreset(_ preset: EditorPreset?) -> EditorBuilder.Builder {
    switch preset {
    case .apparel:
      EditorBuilder.apparel()
    case .postcard:
      EditorBuilder.postcard()
    case .photo:
      EditorBuilder.photo()
    case .video:
      EditorBuilder.video()
    case .design:
      EditorBuilder.design()
    default:
      EditorBuilder.design()
    }
  }
}
