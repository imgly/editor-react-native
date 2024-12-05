package ly.img.editor.reactnative.module

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReadableMap

abstract class IMGLYEditorModuleSpec internal constructor(
    context: ReactApplicationContext,
) : ReactContextBaseJavaModule(context) {
    abstract fun openEditor(
        settings: ReadableMap?,
        source: ReadableMap?,
        preset: String?,
        metadata: ReadableMap?,
        promise: Promise?,
    )
}
