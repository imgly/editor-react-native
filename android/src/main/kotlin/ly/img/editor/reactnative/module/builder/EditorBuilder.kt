package ly.img.editor.reactnative.module.builder

import android.net.Uri
import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ly.img.editor.ApparelEditor
import ly.img.editor.DesignEditor
import ly.img.editor.DismissVideoExportEvent
import ly.img.editor.EditorDefaults
import ly.img.editor.EngineConfiguration
import ly.img.editor.HideLoading
import ly.img.editor.PhotoEditor
import ly.img.editor.PostcardEditor
import ly.img.editor.ShowLoading
import ly.img.editor.ShowVideoExportProgressEvent
import ly.img.editor.VideoEditor
import ly.img.editor.core.event.EditorEventHandler
import ly.img.editor.core.library.data.TextAssetSource
import ly.img.editor.core.library.data.TypefaceProvider
import ly.img.editor.reactnative.module.model.EditorPreset
import ly.img.editor.reactnative.module.model.EditorResult
import ly.img.editor.reactnative.module.model.EditorSettings
import ly.img.editor.reactnative.module.model.EditorSourceType
import ly.img.engine.DemoAssetSource
import ly.img.engine.DesignBlock
import ly.img.engine.DesignBlockType
import ly.img.engine.Engine
import ly.img.engine.MimeType
import ly.img.engine.addDefaultAssetSources
import ly.img.engine.addDemoAssetSources
import java.io.File
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

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
        fun design(): Builder =
            {
                ModalDesignEditor(settings, result, onClose)
            }

        /** The default postcard editor. */
        fun postcard(): Builder =
            {
                ModalPostcardEditor(settings, result, onClose)
            }

        /** The default apparel editor. */
        fun apparel(): Builder =
            {
                ModalApparelEditor(settings, result, onClose)
            }

        /** The default photo editor. */
        fun photo(): Builder =
            {
                ModalPhotoEditor(settings, result, onClose)
            }

        /** The default video editor. */
        fun video(): Builder =
            {
                ModalVideoEditor(settings, result, onClose)
            }

        /**
         * A custom editor implementation.
         * @param contentProvider A closure to provide a custom view.
         * @return A [Builder] providing the custom view.
         */
        fun custom(contentProvider: @Composable CustomBuilderScope.() -> Unit): Builder =
            {
                val scope = CustomBuilderScope(settings, preset, metadata, result, onClose)
                scope.contentProvider()
            }

        @Composable
        private fun ModalPhotoEditor(
            settings: EditorSettings,
            result: EditorBuilderResult,
            onClose: (Throwable?) -> Unit,
        ) {
            val engineConfiguration =
                EngineConfiguration.remember(
                    license = settings.license,
                    baseUri = Uri.parse(settings.baseUri),
                    userId = settings.userId,
                    onCreate = {
                        EditorBuilderDefaults.onCreate(
                            engine = editorContext.engine,
                            eventHandler = editorContext.eventHandler,
                            settings = settings,
                            defaultScene = EditorBuilderDefaults.defaultPhotoUri,
                            sourceType = EditorSourceType.IMAGE,
                        )
                    },
                    onExport = {
                        val export =
                            EditorBuilderDefaults.onExport(
                                engine = editorContext.engine,
                                eventHandler = editorContext.eventHandler,
                                mimeType = MimeType.PNG,
                            )
                        result(Result.success(export))
                    },
                )
            PhotoEditor(engineConfiguration = engineConfiguration) {
                onClose(it)
            }
        }

        @Composable
        private fun ModalApparelEditor(
            settings: EditorSettings,
            result: EditorBuilderResult,
            onClose: (Throwable?) -> Unit,
        ) {
            val engineConfiguration =
                EngineConfiguration.remember(
                    license = settings.license,
                    baseUri = Uri.parse(settings.baseUri),
                    userId = settings.userId,
                    onCreate = {
                        EditorBuilderDefaults.onCreate(
                            engine = editorContext.engine,
                            eventHandler = editorContext.eventHandler,
                            settings = settings,
                            defaultScene = EngineConfiguration.defaultApparelSceneUri,
                        )
                    },
                    onExport = {
                        val export =
                            EditorBuilderDefaults.onExport(
                                engine = editorContext.engine,
                                eventHandler = editorContext.eventHandler,
                                mimeType = MimeType.PDF,
                            )
                        result(Result.success(export))
                    },
                )
            ApparelEditor(engineConfiguration = engineConfiguration) {
                onClose(it)
            }
        }

        @Composable
        private fun ModalDesignEditor(
            settings: EditorSettings,
            result: EditorBuilderResult,
            onClose: (Throwable?) -> Unit,
        ) {
            val engineConfiguration =
                EngineConfiguration.remember(
                    license = settings.license,
                    baseUri = Uri.parse(settings.baseUri),
                    userId = settings.userId,
                    onCreate = {
                        EditorBuilderDefaults.onCreate(
                            engine = editorContext.engine,
                            eventHandler = editorContext.eventHandler,
                            settings = settings,
                            defaultScene = EngineConfiguration.defaultDesignSceneUri,
                        )
                    },
                    onExport = {
                        val export =
                            EditorBuilderDefaults.onExport(
                                engine = editorContext.engine,
                                eventHandler = editorContext.eventHandler,
                                mimeType = MimeType.PDF,
                            )
                        result(Result.success(export))
                    },
                )

            DesignEditor(engineConfiguration = engineConfiguration) {
                onClose(it)
            }
        }

        @Composable
        private fun ModalPostcardEditor(
            settings: EditorSettings,
            result: EditorBuilderResult,
            onClose: (Throwable?) -> Unit,
        ) {
            val engineConfiguration =
                EngineConfiguration.remember(
                    license = settings.license,
                    baseUri = Uri.parse(settings.baseUri),
                    userId = settings.userId,
                    onCreate = {
                        EditorBuilderDefaults.onCreate(
                            engine = editorContext.engine,
                            eventHandler = editorContext.eventHandler,
                            settings = settings,
                            defaultScene = EngineConfiguration.defaultPostcardSceneUri,
                        )
                    },
                    onExport = {
                        val export =
                            EditorBuilderDefaults.onExport(
                                engine = editorContext.engine,
                                eventHandler = editorContext.eventHandler,
                                mimeType = MimeType.PDF,
                            )
                        result(Result.success(export))
                    },
                )

            PostcardEditor(engineConfiguration = engineConfiguration) {
                onClose(it)
            }
        }

        @Composable
        private fun ModalVideoEditor(
            settings: EditorSettings,
            result: EditorBuilderResult,
            onClose: (Throwable?) -> Unit,
        ) {
            val engineConfiguration =
                EngineConfiguration.remember(
                    license = settings.license,
                    baseUri = Uri.parse(settings.baseUri),
                    userId = settings.userId,
                    onCreate = {
                        EditorBuilderDefaults.onCreate(
                            engine = editorContext.engine,
                            eventHandler = editorContext.eventHandler,
                            settings = settings,
                            defaultScene = EngineConfiguration.defaultVideoSceneUri,
                        )
                    },
                    onExport = {
                        try {
                            val export =
                                EditorBuilderDefaults.onExportVideo(
                                    engine = editorContext.engine,
                                    eventHandler = editorContext.eventHandler,
                                    mimeType = MimeType.MP4,
                                )
                            result(Result.success(export))
                        } catch (e: Exception) {
                            if (e !is CancellationException) {
                                result(Result.failure(e))
                            } else {
                                editorContext.eventHandler.send(DismissVideoExportEvent)
                            }
                        }
                    },
                )
            VideoEditor(engineConfiguration = engineConfiguration) {
                onClose(it)
            }
        }
    }
}

/** Default implementations to ease the editor configuration process. */
object EditorBuilderDefaults {
    /** The default image uri for the photo editor. */
    val defaultPhotoUri: Uri = Uri.parse("file:///android_asset/photo-ui-empty.png")

    /**
     * Default plugin implementation for the *onCreate* callback.
     * @param engine The [Engine] that should be used.
     * @param eventHandler The [EditorEventHandler] that should be used.
     * @param settings The [EditorSettings] containing the relevant editor information.
     * @param defaultScene The [Uri] of the defaultScene if no [EditorSettings.source] is specified.
     * @param sourceType The [EditorSourceType] defining of which type the source is.
     */
    suspend fun onCreate(
        engine: Engine,
        eventHandler: EditorEventHandler,
        settings: EditorSettings,
        defaultScene: Uri,
        sourceType: EditorSourceType = EditorSourceType.SCENE,
    ) = coroutineScope {
        fun isValidUri(uri: Uri): Boolean =
            try {
                uri.scheme != null && uri.path != null
            } catch (e: Exception) {
                false
            }

        val uri =
            if (settings.source != null) {
                val sourceUri = Uri.parse(settings.source?.source)
                val isValid = isValidUri(sourceUri)
                if (!isValid) {
                    throw IllegalArgumentException("The specified source is not a valid Uri.")
                }
                sourceUri
            } else {
                defaultScene
            }

        when (settings.source?.type ?: sourceType) {
            EditorSourceType.IMAGE -> {
                engine.scene.createFromImage(uri)
                val graphicBlocks = engine.block.findByType(DesignBlockType.Graphic)
                require(graphicBlocks.size == 1) { "No image found." }
                val graphicBlock = graphicBlocks[0]
                val pages = engine.scene.getPages()
                require(pages.size == 1) { "No image found." }
                val page = pages[0]
                engine.block.setFill(page, engine.block.getFill(graphicBlock))
                engine.block.destroy(graphicBlock)
            }
            EditorSourceType.VIDEO -> {
                engine.scene.createFromVideo(uri)
            }

            else -> {
                engine.scene.load(uri)
            }
        }

        launch {
            val assetBaseUri = settings.assetBaseUri ?: "https://cdn.img.ly/assets/v3"
            val isValid = isValidUri(Uri.parse(assetBaseUri))
            val baseUri = Uri.parse(if (isValid) assetBaseUri else "https://cdn.img.ly/assets/v3")
            engine.addDefaultAssetSources(baseUri = baseUri)
            val excluded = setOf(DemoAssetSource.IMAGE, DemoAssetSource.VIDEO, DemoAssetSource.AUDIO)
            engine.addDemoAssetSources(sceneMode = engine.scene.getMode(), withUploadAssetSources = true, exclude = excluded)
            val defaultTypeface = TypefaceProvider().provideTypeface(engine, "Roboto")
            requireNotNull(defaultTypeface)
            engine.asset.addSource(TextAssetSource(engine, defaultTypeface))
        }
        coroutineContext[Job]?.invokeOnCompletion {
            eventHandler.send(HideLoading)
        }
    }

    /**
     * Default plugin implementation for the *onExport* callback.
     * @param engine The [Engine] that should be used.
     * @param eventHandler The [EditorEventHandler] that should be used.
     * @param mimeType The [MimeType] of the exported artifact.
     * @param thumbnailHeight The height of the generated thumbnail.
     */
    suspend fun onExport(
        engine: Engine,
        eventHandler: EditorEventHandler,
        mimeType: MimeType,
        thumbnailHeight: Int = 100,
    ): EditorResult {
        EditorBuilderDefaults.run {
            eventHandler.send(ShowLoading)
            val blob =
                engine.block.export(
                    block = requireNotNull(engine.scene.get()),
                    mimeType = mimeType,
                ) {
                    scene.getPages().forEach {
                        block.setScopeEnabled(it, key = "layer/visibility", enabled = true)
                        block.setVisible(it, visible = true)
                    }
                }
            val tempFile = EditorDefaults.writeToTempFile(blob, mimeType)
            val scene = engine.scene.get()
            val sceneString =
                scene?.let {
                    engine.scene.saveToString(it)
                }
            val sceneUri = sceneString?.let { saveScene(it) }
            val firstPage = engine.scene.getPages().first()
            val thumbnail = saveThumbnail(firstPage, engine, thumbnailHeight)
            eventHandler.send(HideLoading)
            return EditorResult(
                scene = sceneUri.toString(),
                artifact = Uri.fromFile(tempFile).toString(),
                thumbnail = thumbnail.toString(),
            )
        }
    }

    /**
     * Default plugin implementation for the *onExport* callback for the video editor.
     * @param engine The [Engine] that should be used.
     * @param eventHandler The [EditorEventHandler] that should be used.
     * @param mimeType The [MimeType] of the exported artifact.
     * @param thumbnailHeight The height of the generated thumbnail.
     */
    suspend fun onExportVideo(
        engine: Engine,
        eventHandler: EditorEventHandler,
        mimeType: MimeType,
        thumbnailHeight: Int = 100,
    ): EditorResult {
        EditorBuilderDefaults.run {
            eventHandler.send(ShowVideoExportProgressEvent(0f))

            // First create the thumbnail and save the scene
            // in order to finish before the video export sheet is done.
            val page = engine.scene.getCurrentPage() ?: engine.scene.getPages().first()
            val thumbnail = saveThumbnail(page, engine, thumbnailHeight)
            val scene = engine.scene.get()
            val sceneString =
                scene?.let {
                    engine.scene.saveToString(it)
                }

            val buffer =
                engine.block.exportVideo(
                    block = page,
                    timeOffset = 0.0,
                    duration = engine.block.getDuration(page),
                    mimeType = mimeType,
                    progressCallback = { progress ->
                        eventHandler.send(
                            ShowVideoExportProgressEvent(progress.encodedFrames.toFloat() / progress.totalFrames),
                        )
                    },
                )

            val tempFile = EditorDefaults.writeToTempFile(buffer, mimeType)
            eventHandler.send(DismissVideoExportEvent)

            return EditorResult(
                scene = sceneString,
                artifact = Uri.fromFile(tempFile).toString(),
                thumbnail = thumbnail.toString(),
            )
        }
    }

    private suspend fun saveScene(scene: String): Uri =
        withContext(Dispatchers.IO) {
            val file =
                File
                    .createTempFile("cesdk_export_scene_" + UUID.randomUUID().toString(), ".scene")
            file.writeText(scene)
            Uri.fromFile(file)
        }

    private suspend fun saveThumbnail(
        id: DesignBlock,
        engine: Engine,
        height: Int,
    ): Uri {
        val frames =
            engine.block.generateVideoThumbnailSequence(id, height, timeBegin = 0.0, timeEnd = 0.1, numberOfFrames = 1).toList()
        val firstFrame = frames.first()
        val buffer = firstFrame.imageData
        val tempFile = EditorDefaults.writeToTempFile(buffer, MimeType.PNG)
        return Uri.fromFile(tempFile)
    }
}
