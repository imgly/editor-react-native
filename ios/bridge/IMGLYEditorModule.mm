#import "IMGLYEditorModule.h"
#import "IMGLYConstants.h"
#import "IMGLYEditorModuleSupport.h"

@implementation IMGLYEditorModule

RCT_EXPORT_MODULE(IMGLYEditor)

#ifdef RCT_NEW_ARCH_ENABLED
- (void)openEditor:(JS::NativeIMGLYEditor::EditorSettings&)settings
            source:(JS::NativeIMGLYEditor::Source&)source
            preset:(NSString*)preset
          metadata:(NSDictionary*)metadata
           resolve:(RCTPromiseResolveBlock)resolve
            reject:(RCTPromiseRejectBlock)reject {
  NSMutableDictionary* mergedSettings = [@{
    @"baseUri" : settings.baseUri(),
  } mutableCopy];
  if (settings.license() != nil) {
    mergedSettings[@"license"] = settings.license();
  }
  if (settings.userId() != nil) {
    mergedSettings[@"userId"] = settings.userId();
  }

  NSString* sourceValue = source.source();
  NSString* typeValue = source.type();
  if (sourceValue != nil && typeValue != nil) {
    mergedSettings[@"source"] = @{@"source" : sourceValue, @"type" : typeValue};
  }

  [self open:mergedSettings preset:preset metadata:metadata resolve:resolve reject:reject];
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
  (const facebook::react::ObjCTurboModule::InitParams&)params {
  return std::make_shared<facebook::react::NativeIMGLYEditorSpecJSI>(params);
}
#else
RCT_EXPORT_METHOD(openEditor : (nonnull NSDictionary*)settings source : (nullable NSDictionary*)
                    source preset : (NSString*)preset metadata : (NSDictionary*)
                      metadata resolve : (RCTPromiseResolveBlock)resolve reject : (RCTPromiseRejectBlock)reject) {
  NSMutableDictionary* mergedSettings = [settings mutableCopy];
  mergedSettings[@"source"] = source;
  [self open:mergedSettings preset:preset metadata:metadata resolve:resolve reject:reject];
}

#endif

- (void)open:(nonnull NSDictionary*)settings
      preset:(NSString*)preset
    metadata:(NSDictionary*)metadata
     resolve:(RCTPromiseResolveBlock)resolve
      reject:(RCTPromiseRejectBlock)reject {
  id<IMGLYEditorModuleImplementation> implementation = IMGLYEditorModuleImplementationProvider.implementation;
  if (implementation == nil) {
    reject(IMGLYErrorImplementationMissing, IMGLYErrorImplementationMissingMessage, nil);
    return;
  }
  [implementation openEditorWithSettings:settings preset:preset metadata:metadata resolve:resolve reject:reject];
}

@end
