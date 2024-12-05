package ly.img.editor.reactnative.module

import com.facebook.react.TurboReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider

class IMGLYEditorPackage : TurboReactPackage() {
    override fun getModule(
        name: String,
        reactContext: ReactApplicationContext,
    ): NativeModule? =
        if (name == IMGLYEditorModule.NAME) {
            IMGLYEditorModule(reactContext)
        } else {
            null
        }

    override fun getReactModuleInfoProvider() =
        ReactModuleInfoProvider {
            mapOf(
                IMGLYEditorModule.NAME to
                    ReactModuleInfo(
                        IMGLYEditorModule.NAME,
                        IMGLYEditorModule.NAME,
                        false, // canOverrideExistingModule
                        false, // needsEagerInit
                        true, // hasConstants
                        false, // isCxxModule
                        true, // isTurboModule
                    ),
            )
        }
}
