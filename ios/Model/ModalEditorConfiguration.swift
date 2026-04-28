import IMGLYEditor
import SwiftUI

/// An editor configuration that handles modal dismiss behavior for React Native.
public final class ModalEditorConfiguration: EditorConfiguration {
  private let result: EditorBuilder.EditorBuilderResult

  /// Creates a modal editor configuration.
  /// - Parameters:
  ///   - result: The closure used to return a result to React Native.
  ///   - customize: An optional closure to further customize the configuration.
  public init(
    result: @escaping EditorBuilder.EditorBuilderResult,
    customize: (_ builder: Builder) -> Void = { _ in },
  ) {
    self.result = result
    super.init(customize)
  }

  override public var navigationBar: NavigationBar.Configuration? {
    NavigationBar.Configuration { navBuilder in
      navBuilder.modify { [result] _, items in
        items.replace(id: NavigationBar.Buttons.ID.closeEditor) {
          NavigationBar.Buttons.closeEditor(
            action: { _ in result(.success(nil)) },
            label: { _ in SwiftUI.Label("Home", systemImage: "house") },
          )
        }
      }
    }
  }

  override public var onError: OnError.Handler? {
    { [result] error, eventHandler, _ in
      eventHandler.send(.showErrorAlert(error) {
        result(.failure("Failed to initialize the editor. Please verify that the loaded license is correct."))
      })
    }
  }
}
