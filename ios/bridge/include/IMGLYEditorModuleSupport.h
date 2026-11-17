#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// Resolves a fulfilled result. Mirrors React Native's `RCTPromiseResolveBlock`
/// without importing React, so the Swift implementation stays React-free.
typedef void (^IMGLYEditorResolveBlock)(id _Nullable result);

/// Rejects with an error. Mirrors React Native's `RCTPromiseRejectBlock`.
typedef void (^IMGLYEditorRejectBlock)(NSString* code, NSString* message, NSError* _Nullable error);

/// The seam between the Objective-C++ React Native bridge and the Swift
/// implementation of the editor module.
///
/// The bridge resolves the implementation at runtime through
/// `IMGLYEditorModuleImplementationProvider` instead of importing the generated
/// `-Swift.h` header. Swift Package Manager cannot compile mixed
/// Swift/Objective-C++ targets (SE-0403) and never emits that header, so the
/// Objective-C target must not depend on Swift at compile time.
@protocol IMGLYEditorModuleImplementation <NSObject>

/// Opens the creative editor.
/// - Parameters:
///   - settings: The raw editor settings as received from JavaScript.
///   - preset: The raw editor preset identifier as received from JavaScript.
///   - metadata: Any custom metadata as received from JavaScript.
///   - resolve: The block to invoke with the export result, or `NSNull` on cancellation.
///   - reject: The block to invoke when parsing or the export fails.
- (void)openEditorWithSettings:(NSDictionary*)settings
                        preset:(nullable NSString*)preset
                      metadata:(nullable NSDictionary*)metadata
                       resolve:(IMGLYEditorResolveBlock)resolve
                        reject:(IMGLYEditorRejectBlock)reject;

@end

/// Resolves the Swift implementation of the editor module at runtime.
@interface IMGLYEditorModuleImplementationProvider : NSObject

/// The resolved implementation, or `nil` when the Swift part of this module
/// was not linked into the app.
+ (nullable id<IMGLYEditorModuleImplementation>)implementation;

@end

NS_ASSUME_NONNULL_END
