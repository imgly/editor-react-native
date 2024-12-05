package ly.img.editor.reactnative.module.model

import android.os.Parcel
import android.os.Parcelable

/** An enum containing all prebuilt editor configurations. */
enum class EditorPreset(
    val value: String,
) : Parcelable {
    /** The apparel editor. */
    APPAREL("apparel"),

    /** The postcard editor. */
    POSTCARD("postcard"),

    /** The photo editor. */
    PHOTO("photo"),

    /** The design editor. */
    DESIGN("design"),

    /** The video editor. */
    VIDEO("video"),
    ;

    override fun toString(): String = value

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeString(value)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<EditorPreset> {
        override fun createFromParcel(parcel: Parcel): EditorPreset {
            val value = parcel.readString()
            return entries.find { it.value == value } ?: throw IllegalArgumentException("Invalid value for EditorPreset")
        }

        override fun newArray(size: Int): Array<EditorPreset?> = arrayOfNulls(size)

        fun fromValue(value: String): EditorPreset? = entries.find { it.value == value }
    }
}
