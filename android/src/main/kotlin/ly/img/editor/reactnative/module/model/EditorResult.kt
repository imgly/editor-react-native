package ly.img.editor.reactnative.module.model

import android.os.Parcel
import android.os.Parcelable

/**
 * A class representing the result of an editor export.
 * @property scene The source of the exported scene.
 * @property artifact The source of the exported artifact, e.g. image, video.
 * @property thumbnail The source of the exported thumbnail.
 * @property metadata The associated metadata.
 */
data class EditorResult(
    val scene: String?,
    val artifact: String?,
    val thumbnail: String?,
    val metadata: Map<String, Any> = emptyMap(),
) : Parcelable {
    /**
     * Creates a new instance from a given [Parcel].
     * @param parcel The [Parcel].
     */
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        readMap(parcel),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeString(scene)
        parcel.writeString(artifact)
        parcel.writeString(thumbnail)
        writeMap(parcel, metadata, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<EditorResult> {
        override fun createFromParcel(parcel: Parcel): EditorResult = EditorResult(parcel)

        override fun newArray(size: Int): Array<EditorResult?> = arrayOfNulls(size)

        private fun writeMap(
            parcel: Parcel,
            map: Map<String, Any>,
            flags: Int,
        ) {
            parcel.writeInt(map.size)
            for ((key, value) in map) {
                parcel.writeString(key)
                parcel.writeValue(value)
            }
        }

        private fun readMap(parcel: Parcel): Map<String, Any> {
            val size = parcel.readInt()
            val map = mutableMapOf<String, Any>()
            repeat(size) {
                val key = parcel.readString()
                val value = parcel.readValue(this::class.java.classLoader)
                key ?: throw (IllegalStateException("Key must not be null."))
                value ?: throw (IllegalStateException("Value must not be null."))

                map[key] = value
            }
            return map
        }
    }
}
