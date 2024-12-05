package ly.img.editor.reactnative.module.helper

import android.net.Uri

/** Helper to use resolve assets. */
class AssetHelper {
    companion object {
        /**
         * Validates the source of a given asset and returns the Uri if succeeded.
         * @param source The source of the asset.
         * @return The resolved [Uri] as a [String].
         */
        fun validateAssetSource(source: String): String? {
            fun uriOrNull(uri: Uri?): String? = uri.takeIf { uri?.scheme != null && uri.path != null }?.toString()
            return uriOrNull(Uri.parse(source))
        }
    }
}
