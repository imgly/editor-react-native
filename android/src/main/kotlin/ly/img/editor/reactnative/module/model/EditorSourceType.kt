package ly.img.editor.reactnative.module.model

import android.os.Parcel
import android.os.Parcelable

/** The *EditorSourceType* defines the type of source that is loaded into the editor. */
enum class EditorSourceType(
    val value: String,
) : Parcelable {
    /** A `.scene` file. */
    SCENE("scene"),

    /** An image file, e.g. `.png`. */
    IMAGE("image"),

    /** A video file, e.g. `.mp4`. */
    VIDEO("video"),
    ;

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeString(value)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<EditorSourceType> {
        override fun createFromParcel(parcel: Parcel): EditorSourceType {
            val value = parcel.readString()
            return entries.find { it.value == value } ?: throw IllegalArgumentException("Invalid value for EditorSourceType")
        }

        override fun newArray(size: Int): Array<EditorSourceType?> = arrayOfNulls(size)

        fun fromValue(value: String): EditorSourceType? = entries.find { it.value == value }
    }
}
