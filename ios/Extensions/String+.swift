import Foundation

/// An extension to allow throwing strings.
extension String: LocalizedError {
  /// The error description.
  public var errorDescription: String? { self }
}
