#ifdef RCT_NEW_ARCH_ENABLED
#import <IMGLYEditorModuleSpec/IMGLYEditorModuleSpec.h>

@interface IMGLYEditorModule : NSObject <NativeIMGLYEditorSpec>
#else
#import <React/RCTBridgeModule.h>

@interface IMGLYEditorModule : NSObject <RCTBridgeModule>
#endif

@end
