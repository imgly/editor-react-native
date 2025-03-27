package ly.img.editor.reactnative.module

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import ly.img.editor.reactnative.module.activity.EditorActivity
import ly.img.editor.reactnative.module.builder.Builder
import ly.img.editor.reactnative.module.helper.AssetHelper
import ly.img.editor.reactnative.module.helper.IntentHelper
import ly.img.editor.reactnative.module.model.EditorPreset
import ly.img.editor.reactnative.module.model.EditorResult
import ly.img.editor.reactnative.module.model.EditorSettings
import java.io.Serializable

/** A closure to specify a [Builder] based on a given *preset* and *metadata*. */
typealias IMGLYBuilderClosure = (preset: EditorPreset?, metadata: Map<String, Any>?) -> Builder

class IMGLYEditorModule(
    reactContext: ReactApplicationContext,
) : IMGLYEditorModuleSpec(reactContext),
    ActivityEventListener {
    companion object {
        /** A closure to specify a [Builder] based on a given *preset* and *metadata*. */
        var builderClosure: IMGLYBuilderClosure? = null

        /** The name of the module. */
        const val NAME = "IMGLYEditor"
    }

    init {
        reactContext.addActivityEventListener(this)
    }

    /** The completion handler used by the activity result listener. */
    private var completion: ((Result<EditorResult?>) -> Unit)? = null

    /** IMGLY constants for the plugin use. */
    private object IMGLYConstants {
        const val ERROR_EXPORT_FAILED = "E_EXPORT_FAILED"
        const val ERROR_MISSING_ARGUMENTS = "E_MISSING_ARGUMENTS"
        const val ERROR_PARSING = "E_PARSING"
        const val ERROR_EXPORT_FAILED_MESSAGE = "Failed to export the artifact due to: "
        const val ERROR_PARSING_MESSAGE = "Unable to parse the argument(s): "
        const val ERROR_MISSING_ARGUMENTS_MESSAGE = "Unable to find required argument(s): "
    }

    /** The name of the module. */
    override fun getName() = NAME

    /** Opens the creative editor.  */
    @ReactMethod
    override fun openEditor(
        settings: ReadableMap?,
        source: ReadableMap?,
        preset: String?,
        metadata: ReadableMap?,
        promise: Promise?,
    ) {
        if (settings == null) {
            promise?.reject(IMGLYConstants.ERROR_MISSING_ARGUMENTS, IMGLYConstants.ERROR_MISSING_ARGUMENTS_MESSAGE)
        } else {
            val settingsHashMap = settings.toHashMap()
            val sourceHashMap = source?.toHashMap()
            if (sourceHashMap != null) {
                settingsHashMap["source"] = sourceHashMap
            }
            val editorSettings = EditorSettings.createFromMap(settingsHashMap)
            val editorPreset = preset?.let { EditorPreset.fromValue(it) } ?: EditorPreset.DESIGN
            val editorSource = editorSettings?.source
            val metadataHashMap = metadata?.toHashMap()

            if (editorSource != null) {
                val resolvedUri = AssetHelper.validateAssetSource(editorSource.source)
                if (resolvedUri == null) {
                    promise?.reject(IMGLYConstants.ERROR_PARSING, IMGLYConstants.ERROR_PARSING_MESSAGE)
                    return
                }
                editorSettings.source?.source = resolvedUri
            }

            if (editorSettings != null) {
                this.openEditor(editorPreset, editorSettings, metadataHashMap) {
                    it.fold(
                        onSuccess = { value ->
                            if (value == null) {
                                promise?.resolve(null)
                            } else {
                                promise?.resolve(
                                    reactMap(
                                        "scene" to value.scene,
                                        "artifact" to value.artifact,
                                        "thumbnail" to value.thumbnail,
                                        "metadata" to convertToWritableMap(value.metadata),
                                    ),
                                )
                            }
                        },
                        onFailure = {
                            promise?.reject(
                                IMGLYConstants.ERROR_EXPORT_FAILED,
                                IMGLYConstants.ERROR_EXPORT_FAILED_MESSAGE + it.localizedMessage,
                            )
                        },
                    )
                }
            } else {
                promise?.reject(IMGLYConstants.ERROR_PARSING, IMGLYConstants.ERROR_PARSING_MESSAGE)
            }
        }
    }

    /**
     * Opens the creative editor.
     * @param preset The [EditorPreset] used to determine which UI preset to use.
     * @param config The [EditorSettings] containing all relevant information for the editor.
     * @param metadata Any custom metadata used for the [Builder].
     * @param completion The completion handler to execute once the editor failed, cancelled or exported.
     */
    private fun openEditor(
        preset: EditorPreset?,
        config: EditorSettings,
        metadata: Map<String, Any>?,
        completion: (Result<EditorResult?>) -> Unit,
    ) {
        val activity = this.currentActivity ?: return
        this.completion = completion

        val intent = Intent(activity, EditorActivity::class.java).apply {
            putExtra(EditorActivity.INTENT_EXTRA_CONFIG, config)
            putExtra(EditorActivity.INTENT_EXTRA_PRESET, preset as Parcelable)
            putExtra(EditorActivity.INTENT_EXTRA_METADATA, IntentHelper.mapToBundle(metadata))
        }
        activity.startActivityForResult(intent, EditorActivity.INTENT_REQUEST_CODE)
    }

    override fun onActivityResult(
        activity: Activity?,
        requestCode: Int,
        resultCode: Int,
        intent: Intent?,
    ) {
        if (requestCode == EditorActivity.INTENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val result = intent?.getParcelableExtra<EditorResult>(EditorActivity.INTENT_EXTRA_RESULT)
                result?.let {
                    this.completion?.invoke(Result.success(it))
                    this.completion = null
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                this.completion?.invoke(Result.success(null))
            } else if (resultCode == EditorActivity.INTENT_RESULT_ERROR_CODE) {
                val error = intent?.getSerializableExtraCompat<Throwable>(EditorActivity.INTENT_EXTRA_RESULT)
                this.completion?.invoke(
                    Result.failure(Exception("Failed to initialize the editor with error: ${error?.localizedMessage}")),
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {}

    operator fun WritableMap.set(
        id: String,
        value: Boolean,
    ) = this.putBoolean(id, value)

    operator fun WritableMap.set(
        id: String,
        value: String?,
    ) = this.putString(id, value)

    operator fun WritableMap.set(
        id: String,
        value: Double,
    ) = this.putDouble(id, value)

    operator fun WritableMap.set(
        id: String,
        value: Float,
    ) = this.putDouble(id, value.toDouble())

    operator fun WritableMap.set(
        id: String,
        value: WritableArray?,
    ) = this.putArray(id, value)

    operator fun WritableMap.set(
        id: String,
        value: Int,
    ) = this.putInt(id, value)

    operator fun WritableMap.set(
        id: String,
        value: WritableMap?,
    ) = this.putMap(id, value)

    private fun reactMap(vararg pairs: Pair<String, Any?>) = Arguments.createMap().apply {
        pairs.forEach { (id, value) ->
            when (value) {
                null -> putNull(id)
                is String -> putString(id, value)
                is Boolean -> putBoolean(id, value)
                is Double -> putDouble(id, value)
                is Float -> putDouble(id, value.toDouble())
                is Int -> putInt(id, value)
                is WritableMap -> putMap(id, value)
                is WritableArray -> putArray(id, value)
                else -> error("Unsupported type for WritableMap: ${value::class.simpleName}")
            }
        }
    }

    private fun convertToWritableMap(map: Map<*, *>): WritableMap {
        val writableMap = Arguments.createMap()
        for ((key, value) in map) {
            if (key is String) {
                when (value) {
                    null -> writableMap.putNull(key)
                    is String -> writableMap.putString(key, value)
                    is Boolean -> writableMap.putBoolean(key, value)
                    is Double -> writableMap.putDouble(key, value)
                    is Int -> writableMap.putInt(key, value)
                    is Float -> writableMap.putDouble(key, value.toDouble())
                    is Map<*, *> -> writableMap.putMap(key, convertToWritableMap(value))
                    is WritableArray -> writableMap.putArray(key, value)
                    else -> {
                        throw RuntimeException("Type not supported in WritableMap")
                    }
                }
            }
        }
        return writableMap
    }

    private inline fun <reified T : Serializable> Intent.getSerializableExtraCompat(key: String): T? = if (Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.TIRAMISU
    ) {
        getSerializableExtra(key, T::class.java)
    } else {
        getSerializableExtra(key) as T
    }
}
