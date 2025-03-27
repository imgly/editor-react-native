package ly.img.editor.reactnative.module.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ly.img.editor.reactnative.module.IMGLYEditorModule
import ly.img.editor.reactnative.module.builder.Builder
import ly.img.editor.reactnative.module.builder.CustomBuilderScope
import ly.img.editor.reactnative.module.builder.EditorBuilder
import ly.img.editor.reactnative.module.helper.IntentHelper
import ly.img.editor.reactnative.module.model.EditorPreset
import ly.img.editor.reactnative.module.model.EditorSettings
import java.io.Serializable

/** The [Activity] used to display the editor. */
class EditorActivity : ComponentActivity() {
    companion object {
        /** The request code for this activity. */
        var INTENT_REQUEST_CODE = 29055

        /**
         * The result code for this activity in
         * case of an error.
         */
        var INTENT_RESULT_ERROR_CODE = 29056

        /** The identifier to the config intent extra. */
        const val INTENT_EXTRA_CONFIG = "intent_extra_config_cesdk"

        /** The identifier to the preset intent extra. */
        const val INTENT_EXTRA_PRESET = "intent_extra_preset_cesdk"

        /** The identifier to the metadata intent extra. */
        const val INTENT_EXTRA_METADATA = "intent_extra_metadata_cesdk"

        /** The identifier to the result intent extra. */
        const val INTENT_EXTRA_RESULT = "intent_extra_result_cesdk"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = intent.extras?.getParcelableCompat<EditorSettings>(INTENT_EXTRA_CONFIG) ?: return
        val preset = intent.extras?.getParcelableCompat<EditorPreset>(INTENT_EXTRA_PRESET) ?: EditorPreset.DESIGN
        val metadataBundle = intent.extras?.getParcelableCompat<Bundle>(INTENT_EXTRA_METADATA)
        val metadata = IntentHelper.bundleToMap(metadataBundle)
        val builder: Builder = IMGLYEditorModule.builderClosure?.invoke(preset, metadata) ?: builderForPreset(preset)

        setContent {
            builder(
                CustomBuilderScope(config, preset, metadata, { result ->
                    val (resolvedResult, resultCode) = result.fold(
                        onFailure = { error ->
                            Pair(error, INTENT_RESULT_ERROR_CODE)
                        },
                        onSuccess = { value ->
                            Pair(value, Activity.RESULT_OK)
                        },
                    )

                    val resultIntent = Intent().apply {
                        when (resultCode) {
                            INTENT_RESULT_ERROR_CODE -> {
                                putExtra(INTENT_EXTRA_RESULT, resolvedResult as Serializable)
                            }

                            Activity.RESULT_OK -> {
                                putExtra(INTENT_EXTRA_RESULT, resolvedResult as Parcelable)
                            }
                        }
                    }

                    setResult(resultCode, resultIntent)
                    finish()
                }, {
                    if (it != null) {
                        val resultIntent = Intent().apply {
                            putExtra(INTENT_EXTRA_RESULT, it)
                        }
                        setResult(INTENT_RESULT_ERROR_CODE, resultIntent)
                    }
                    finish()
                }),
            )
        }
    }

    private fun builderForPreset(preset: EditorPreset): Builder = when (preset) {
        EditorPreset.APPAREL -> EditorBuilder.apparel()
        EditorPreset.POSTCARD -> EditorBuilder.postcard()
        EditorPreset.PHOTO -> EditorBuilder.photo()
        EditorPreset.DESIGN -> EditorBuilder.design()
        EditorPreset.VIDEO -> EditorBuilder.video()
    }

    private inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? = if (Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.TIRAMISU
    ) {
        getParcelable(key, T::class.java)
    } else {
        getParcelable(key)
    }
}
