package ly.img.editor.reactnative.module.builder

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import ly.img.editor.Editor
import ly.img.editor.configuration.apparel.ApparelConfigurationBuilder
import ly.img.editor.configuration.apparel.callback.onCreate
import ly.img.editor.configuration.apparel.callback.onExport
import ly.img.editor.configuration.design.DesignConfigurationBuilder
import ly.img.editor.configuration.design.callback.onCreate
import ly.img.editor.configuration.design.callback.onExport
import ly.img.editor.configuration.photo.PhotoConfigurationBuilder
import ly.img.editor.configuration.photo.callback.onCreate
import ly.img.editor.configuration.photo.callback.onExport
import ly.img.editor.configuration.postcard.PostcardConfigurationBuilder
import ly.img.editor.configuration.postcard.callback.onCreate
import ly.img.editor.configuration.postcard.callback.onExport
import ly.img.editor.configuration.video.VideoConfigurationBuilder
import ly.img.editor.configuration.video.callback.onCreate
import ly.img.editor.configuration.video.callback.onExport
import ly.img.editor.core.EditorScope
import ly.img.editor.core.configuration.EditorConfiguration
import ly.img.editor.core.configuration.remember
import ly.img.editor.reactnative.module.model.EditorPreset
import ly.img.editor.reactnative.module.model.EditorResult
import ly.img.editor.reactnative.module.model.EditorSettings
import ly.img.editor.reactnative.module.model.EditorSourceType
import ly.img.engine.DesignBlock
import ly.img.engine.DesignBlockType
import ly.img.engine.Engine
import ly.img.engine.FillType
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.UUID

/** A closure used to return a [Result] with type [EditorResult] from the editor. */
typealias EditorBuilderResult = (Result<EditorResult?>) -> Unit

/** Closure allowing to return a custom UI based on various input. */
typealias Builder = @Composable CustomBuilderScope.() -> Unit

/**
 * @property settings The [EditorSettings] to configure the editor.
 * @property preset The [EditorPreset] to open.
 * @property metadata The custom metadata.
 * @property result The [EditorBuilderResult] handling the result.
 * @property onClose The closure to handle editor closing.
 */
data class CustomBuilderScope(
    val settings: EditorSettings,
    val preset: EditorPreset,
    val metadata: Map<String, Any>?,
    val result: EditorBuilderResult,
    val onClose: (Throwable?) -> Unit,
)

/** The [EditorBuilder] is used to build different editor configurations */
class EditorBuilder {
    companion object {
        /** The default design editor. */
        fun design(): Builder = {
            ModalDesignEditor(settings, result, onClose)
        }

        /** The default postcard editor. */
        fun postcard(): Builder = {
            ModalPostcardEditor(settings, result, onClose)
        }

        /** The default apparel editor. */
        fun apparel(): Builder = {
            ModalApparelEditor(settings, result, onClose)
        }

        /** The default photo editor. */
        fun photo(): Builder = {
            ModalPhotoEditor(settings, result, onClose)
        }

        /** The default video editor. */
        fun video(): Builder = {
            ModalVideoEditor(settings, result, onClose)
        }

        /**
         * A custom editor implementation.
         * @param contentProvider A closure to provide a custom view.
         * @return A [Builder] providing the custom view.
         */
        fun custom(contentProvider: @Composable CustomBuilderScope.() -> Unit): Builder = {
            val scope = CustomBuilderScope(settings, preset, metadata, result, onClose)
            scope.contentProvider()
        }

        @Composable
        private fun ModalPhotoEditor(
            settings: EditorSettings,
            result: EditorBuilderResult,
            onClose: (Throwable?) -> Unit,
        ) {
            Editor(
                license = settings.license,
                baseUri = settings.baseUri.toUri(),
                userId = settings.userId,
                configuration = {
                    EditorConfiguration.remember(::PhotoConfigurationBuilder) {
                        onCreate = {
                            onCreate(
                                createScene = {
                                    EditorBuilderDefaults.onCreateScene(
                                        scope = this@Editor,
                                        settings = settings,
                                        defaultUri = EditorBuilderDefaults.defaultPhotoUri,
                                        sourceType = EditorSourceType.IMAGE,
                                    )
                                },
                            )
                        }
                        onExport = {
                            onExport(
                                postExport = {
                                    val result = EditorBuilderDefaults.getExportResult(
                                        scope = this@Editor,
                                        byteBuffer = it,
                                    )
                                    result(Result.success(result))
                                },
                                error = {
                                    result(Result.failure(it))
                                },
                            )
                        }
                    }
                },
            ) {
                onClose(it)
            }
        }

        @Composable
        private fun ModalApparelEditor(
            settings: EditorSettings,
            result: EditorBuilderResult,
            onClose: (Throwable?) -> Unit,
        ) {
            Editor(
                license = settings.license,
                baseUri = settings.baseUri.toUri(),
                userId = settings.userId,
                configuration = {
                    EditorConfiguration.remember(::ApparelConfigurationBuilder) {
                        onCreate = {
                            onCreate(
                                createScene = {
                                    EditorBuilderDefaults.onCreateScene(
                                        scope = this@Editor,
                                        settings = settings,
                                        defaultUri = "file:///android_asset/scene/apparel.scene".toUri(),
                                    )
                                },
                            )
                        }
                        onExport = {
                            onExport(
                                postExport = {
                                    val result = EditorBuilderDefaults.getExportResult(
                                        scope = this@Editor,
                                        byteBuffer = it,
                                    )
                                    result(Result.success(result))
                                },
                                error = {
                                    result(Result.failure(it))
                                },
                            )
                        }
                    }
                },
            ) {
                onClose(it)
            }
        }

        @Composable
        private fun ModalDesignEditor(
            settings: EditorSettings,
            result: EditorBuilderResult,
            onClose: (Throwable?) -> Unit,
        ) {
            Editor(
                license = settings.license,
                baseUri = settings.baseUri.toUri(),
                userId = settings.userId,
                configuration = {
                    EditorConfiguration.remember(::DesignConfigurationBuilder) {
                        onCreate = {
                            onCreate(
                                createScene = {
                                    EditorBuilderDefaults.onCreateScene(
                                        scope = this@Editor,
                                        settings = settings,
                                        defaultUri = "file:///android_asset/scene/design.scene".toUri(),
                                    )
                                },
                            )
                        }
                        onExport = {
                            onExport(
                                postExport = {
                                    val result = EditorBuilderDefaults.getExportResult(
                                        scope = this@Editor,
                                        byteBuffer = it,
                                    )
                                    result(Result.success(result))
                                },
                                error = {
                                    result(Result.failure(it))
                                },
                            )
                        }
                    }
                },
            ) {
                onClose(it)
            }
        }

        @Composable
        private fun ModalPostcardEditor(
            settings: EditorSettings,
            result: EditorBuilderResult,
            onClose: (Throwable?) -> Unit,
        ) {
            Editor(
                license = settings.license,
                baseUri = settings.baseUri.toUri(),
                userId = settings.userId,
                configuration = {
                    EditorConfiguration.remember(::PostcardConfigurationBuilder) {
                        onCreate = {
                            onCreate(
                                createScene = {
                                    EditorBuilderDefaults.onCreateScene(
                                        scope = this@Editor,
                                        settings = settings,
                                        defaultUri = "file:///android_asset/scene/postcard.scene".toUri(),
                                    )
                                },
                            )
                        }
                        onExport = {
                            onExport(
                                postExport = {
                                    val result = EditorBuilderDefaults.getExportResult(
                                        scope = this@Editor,
                                        byteBuffer = it,
                                    )
                                    result(Result.success(result))
                                },
                                error = {
                                    result(Result.failure(it))
                                },
                            )
                        }
                    }
                },
            ) {
                onClose(it)
            }
        }

        @Composable
        private fun ModalVideoEditor(
            settings: EditorSettings,
            result: EditorBuilderResult,
            onClose: (Throwable?) -> Unit,
        ) {
            Editor(
                license = settings.license,
                baseUri = settings.baseUri.toUri(),
                userId = settings.userId,
                configuration = {
                    EditorConfiguration.remember(::VideoConfigurationBuilder) {
                        onCreate = {
                            onCreate(
                                createScene = {
                                    EditorBuilderDefaults.onCreateScene(
                                        scope = this@Editor,
                                        settings = settings,
                                        defaultUri = "file:///android_asset/scene/video.scene".toUri(),
                                    )
                                },
                            )
                        }
                        onExport = {
                            onExport(
                                postExport = {
                                    val result = EditorBuilderDefaults.getExportResult(
                                        scope = this@Editor,
                                        byteBuffer = it,
                                    )
                                    exportStatus = null
                                    result(Result.success(result))
                                },
                                error = {
                                    exportStatus = null
                                    if (it !is CancellationException) {
                                        result(Result.failure(it))
                                    }
                                },
                            )
                        }
                    }
                },
            ) {
                onClose(it)
            }
        }
    }
}

/** Default implementations to ease the editor configuration process. */
object EditorBuilderDefaults {
    /** The default image uri for the photo editor. */
    val defaultPhotoUri: Uri = "file:///android_asset/photo-ui-empty.png".toUri()

    /**
     * Default plugin implementation for the scene creation part of the *onCreate* callback.
     *
     * @param scope the [EditorScope] of the editor.
     * @param settings the [EditorSettings] containing the relevant editor information.
     * @param defaultUri the default source [Uri] if no [EditorSettings.source] is specified.
     * @param sourceType the [EditorSourceType] defining of which type the source is.
     */
    suspend fun onCreateScene(
        scope: EditorScope,
        settings: EditorSettings,
        defaultUri: Uri,
        sourceType: EditorSourceType = EditorSourceType.SCENE,
    ) = scope.run {
        editorContext.engine.scene.get()?.let { return@run it }

        fun isValidUri(uri: Uri): Boolean = try {
            uri.scheme != null && uri.path != null
        } catch (_: Exception) {
            false
        }

        val source = settings.source
        val uri = if (source != null) {
            val sourceUri = source.source.toUri()
            val isValid = isValidUri(sourceUri)
            if (!isValid) {
                throw IllegalArgumentException("The specified source is not a valid Uri.")
            }
            sourceUri
        } else {
            defaultUri
        }

        when (settings.source?.type ?: sourceType) {
            EditorSourceType.IMAGE -> {
                editorContext.engine.scene.createFromImage(uri)
            }
            EditorSourceType.VIDEO -> {
                editorContext.engine.scene.createFromVideo(uri)
            }
            else -> {
                editorContext.engine.scene.load(uri)
            }
        }
        requireNotNull(editorContext.engine.scene.get())
    }

    /**
     * Default plugin implementation that returns [EditorResult] based on the exported [byteBuffer]
     *
     * @param scope the [EditorScope] of the editor.
     * @param byteBuffer the buffer that contains the exported data.
     * @param thumbnailHeight the height of the generated thumbnail.
     * @return a new [EditorResult] that contains a uri to temporary file with [byteBuffer] content,
     * thumbnail and an exported scene string.
     */
    suspend fun getExportResult(
        scope: EditorScope,
        byteBuffer: ByteBuffer,
        thumbnailHeight: Int = 100,
    ) = scope.run {
        val scene = editorContext.engine.scene.get()
        val sceneString = scene?.let {
            checkForContentUris(editorContext.engine)
            editorContext.engine.scene.saveToString(
                scene = it,
                allowedResourceSchemes = listOf("blob", "bundle", "file", "http", "https", "content"),
            )
        }
        val sceneUri = sceneString?.let { saveScene(it) }
        val firstPage = editorContext.engine.scene.getPages().first()
        editorContext.engine.block.setVisible(firstPage, visible = true)
        val thumbnail = saveThumbnail(firstPage, editorContext.engine, thumbnailHeight)
        EditorResult(
            scene = sceneUri.toString(),
            artifact = Uri.fromFile(byteBuffer.writeToFile()).toString(),
            thumbnail = thumbnail.toString(),
        )
    }

    private suspend fun ByteBuffer.writeToFile(): File = withContext(Dispatchers.IO) {
        File.createTempFile(UUID.randomUUID().toString(), ".tmp").apply {
            outputStream().channel.write(this@writeToFile)
        }
    }

    private suspend fun saveScene(scene: String): Uri = withContext(Dispatchers.IO) {
        val file = File.createTempFile("cesdk_export_scene_" + UUID.randomUUID().toString(), ".scene")
        file.writeText(scene)
        Uri.fromFile(file)
    }

    private suspend fun saveThumbnail(
        id: DesignBlock,
        engine: Engine,
        height: Int,
    ): Uri {
        val frames = engine.block.generateVideoThumbnailSequence(
            block = id,
            thumbnailHeight = height,
            timeBegin = 0.0,
            timeEnd = 0.1,
            numberOfFrames = 1,
        ).toList()
        val firstFrame = frames.first()
        return withContext(Dispatchers.IO) {
            val buffer = firstFrame.imageData
            val bitmap = createBitmap(firstFrame.width, firstFrame.height)
            buffer.rewind()
            bitmap.copyPixelsFromBuffer(buffer)
            val file = File.createTempFile(UUID.randomUUID().toString(), ".png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Uri.fromFile(file)
        }
    }

    private fun checkForContentUris(engine: Engine) = runCatching {
        fun isContentUri(uri: String) = uri.startsWith("content://")

        val graphicBlocks = engine.block.findByType(DesignBlockType.Graphic)
        for (block in graphicBlocks) {
            val fill = if (engine.block.supportsFill(block)) {
                engine.block.getFill(block).takeIf { engine.block.isValid(it) }
            } else {
                null
            }
            fill ?: continue
            val fillType = FillType.get(engine.block.getType(fill))
            val contentUriFound = when (fillType) {
                FillType.Image -> {
                    isContentUri(engine.block.getString(fill, "fill/image/imageFileURI")) ||
                        isContentUri(engine.block.getString(fill, "fill/image/previewFileURI"))
                }
                FillType.Video -> {
                    isContentUri(engine.block.getString(fill, "fill/video/fileURI"))
                }
                else -> continue
            }
            if (contentUriFound) {
                Log.w(
                    "IMG.LY",
                    """
                    This scene contains block(s) that reference `content://` Uri(s) 
                    (e.g. when media is picked from gallery or camera). These may not be 
                    resolvable when loading the scene again, especially outside the 
                    original app context. If you need to support reloading such scenes, 
                    consider using a custom implementation with 
                    `EngineConfiguration.onUpload` and `engine.editor.setUriResolver` 
                    to handle these Uri(s) appropriately.
                    """.trimIndent(),
                )
                break
            }
        }
    }
}
