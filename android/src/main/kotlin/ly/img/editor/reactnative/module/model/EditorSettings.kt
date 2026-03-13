package ly.img.editor.reactnative.module.model

import android.os.Parcel
import android.os.Parcelable

/**
 * A class containing all necessary information to configure an editor.
 * @property license The license key. Pass `null` to run the SDK in evaluation mode with a watermark.
 * @property baseUri The base URI used by the engine for built-in assets like emoji and fallback
 *   fonts, and by the editor for its default and demo asset sources (stickers, filters, and more).
 * @property userId The id of the current user.
 * @property source The source to load into the editor.
 */
data class EditorSettings(
    val license: String? = null,
    val baseUri: String,
    val userId: String?,
    var source: Source?,
) : Parcelable {
    /**
     * Creates a new instance from a given [Parcel].
     * @param parcel The [Parcel].
     */
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString() ?: throw (Exception("Missing value for key 'baseUri.'")),
        parcel.readString(),
        parcel.readParcelable(Source::class.java.classLoader),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeString(license)
        parcel.writeString(baseUri)
        parcel.writeString(userId)
        parcel.writeParcelable(source, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<EditorSettings> {
        override fun createFromParcel(parcel: Parcel): EditorSettings = EditorSettings(parcel)

        override fun newArray(size: Int): Array<EditorSettings?> = arrayOfNulls(size)

        fun createFromMap(map: Map<String, Any?>): EditorSettings? = try {
            val license = map["license"] as? String
            val baseUri = map["baseUri"] as? String ?: throw (Exception("Missing value for key 'baseUri.'"))
            val userId = map["userId"] as? String
            val source = (map["source"] as? Map<String, Any?>)?.let { Source.createFromMap(it) }
            EditorSettings(license, baseUri, userId, source)
        } catch (e: Exception) {
            null
        }
    }
}
