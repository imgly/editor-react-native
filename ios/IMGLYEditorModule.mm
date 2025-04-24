#import "IMGLYEditorModule.h"
#import "IMGLYEditorModule/IMGLYEditorModule-Swift.h"
#import "IMGLYConstants.h"

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
    @"license" : settings.license(),
    @"sceneBaseUri" : settings.sceneBaseUri(),
  } mutableCopy];
  if (settings.assetBaseUri() != nil) {
    mergedSettings[@"assetBaseUri"] = settings.assetBaseUri();
  }
  if (settings.userId() != nil) {
    mergedSettings[@"userId"] = settings.userId();
  }

  JS::NativeIMGLYEditor::Source* sourcePtr = &source;
  if (sourcePtr != nullptr) {
    mergedSettings[@"source"] = @{@"source" : source.source(), @"type" : source.type()};
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
  EditorSettings* convertedSettings = [EditorSettings fromDictionary:settings];
  if (convertedSettings == nil) {
    reject(IMGLYErrorParsing, IMGLYErrorParsingMessage, nil);
    return;
  }
  EditorPreset convertedPreset = [EditorPresetParser fromString:preset];
  dispatch_async(dispatch_get_main_queue(), ^{
    [[IMGLYEditorModuleSwiftAdapter shared] openEditor:convertedPreset
                                              settings:convertedSettings
                                              metadata:metadata
                                            completion:^(EditorResult* _Nullable result, NSError* _Nullable error) {
                                              if (result != nil) {
                                                resolve(@ {
                                                  @"artifact" : result.artifact,
                                                  @"scene" : result.scene,
                                                  @"thumbnail" : result.thumbnail,
                                                  @"metadata" : result.metadata
                                                });
                                              } else if (error != nil) {
                                                reject(IMGLYErrorExportFailed, IMGLYErrorExportFailedMessage, error);
                                              } else {
                                                resolve([NSNull null]);
                                              }
                                            }];
  });
}

@end
