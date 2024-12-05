package ly.img.editor.reactnative.module.helper

import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

/** A helper to parse Maps for intents */
class IntentHelper {
    companion object {
        /**
         * Converts a [Map] to a [Bundle].
         * @param map The map to convert.
         * @return The bundle.
         */
        fun mapToBundle(map: Map<*, *>?): Bundle {
            val bundle = Bundle()
            if (map != null) {
                for ((key, value) in map) {
                    if (key is String) {
                        when (value) {
                            is Int -> bundle.putInt(key, value)
                            is Long -> bundle.putLong(key, value)
                            is String -> bundle.putString(key, value)
                            is Float -> bundle.putFloat(key, value)
                            is Double -> bundle.putDouble(key, value)
                            is Boolean -> bundle.putBoolean(key, value)
                            is Bundle -> bundle.putBundle(key, value)
                            is Map<*, *> -> bundle.putBundle(key, mapToBundle(value))
                            is Parcelable -> bundle.putParcelable(key, value)
                            is Serializable -> bundle.putSerializable(key, value)
                            // Add other types as needed
                            else -> throw IllegalArgumentException("Unsupported value type: ${value?.javaClass}")
                        }
                    } else {
                        throw IllegalArgumentException("Unsupported key type: ${key?.javaClass}")
                    }
                }
            }
            return bundle
        }

        /**
         * Converts a [Bundle] to a *Map<String, Any>*
         * @param bundle The [Bundle] to convert.
         * @return The map.
         */
        fun bundleToMap(bundle: Bundle?): Map<String, Any>? {
            bundle ?: return null
            val map = mutableMapOf<String, Any>()
            for (key in bundle.keySet()) {
                val value = bundle.get(key)
                if (value is Bundle) {
                    map[key] = bundleToMap(value)!!
                } else if (value != null) {
                    map[key] = value
                }
            }
            return map
        }
    }
}
