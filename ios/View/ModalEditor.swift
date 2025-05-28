import IMGLYEditor
import SwiftUI

/// The `ModalEditor` contains an editor and adds the top navigation bar.
public struct ModalEditor<Editor: View, Label: View>: View {
  @ViewBuilder private let editor: () -> Editor
  @ViewBuilder private let dismissLabel: () -> Label
  private let onDismiss: (_ cancelled: Bool) -> Void

  /// Initializes a new `ModalEditor`.
  /// - Parameters:
  ///   - editor: The editor that is contained.
  ///   - dismissLabel: The label used for the dismiss action.
  ///   - onDismiss: The closure called when the editor is dismissed.
  ///   - cancelled: Indicates whether the editor has been dismissed by the user.
  ///                        If `false`, the editor has been closed due to an error.
  public init(@ViewBuilder editor: @escaping () -> Editor,
              @ViewBuilder dismissLabel: @escaping () -> Label = { SwiftUI.Label("Home", systemImage: "house") },
              onDismiss: @escaping (_ cancelled: Bool) -> Void) {
    self.editor = editor
    self.dismissLabel = dismissLabel
    self.onDismiss = onDismiss
  }

  /// The body.
  public var body: some View {
    NavigationView {
      editor()
        .imgly.modifyNavigationBarItems { _, items in
          items.replace(id: NavigationBar.Buttons.ID.closeEditor) {
            NavigationBar.Buttons.closeEditor(
              action: { _ in onDismiss(true) },
              label: { _ in dismissLabel() }
            )
          }
        }
        .onDisappear {
          onDismiss(false)
        }
    }
    .navigationViewStyle(.stack)
  }
}
