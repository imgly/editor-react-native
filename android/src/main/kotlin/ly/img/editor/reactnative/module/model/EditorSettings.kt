package ly.img.editor.reactnative.module.model

import android.os.Parcel
import android.os.Parcelable

/**
 * A class containing all necessary information to configure an editor.
 * @property license The license key.
 * @property baseUri The base uri for the assets included in the scene that have a relative source.
 * @property assetBaseUri The base uri of the default assets in the asset library.
 * @property userId The id of the current user.
 * @property source The source to load into the editor.
 */
data class EditorSettings(
    val license: String,
    val baseUri: String,
    val assetBaseUri: String?,
    val userId: String?,
    var source: Source?,
) : Parcelable {
    /**
     * Creates a new instance from a given [Parcel].
     * @param parcel The [Parcel].
     */
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: throw (Exception("Missing value for key 'license.'")),
        parcel.readString() ?: throw (Exception("Missing value for key 'baseUri.'")),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Source::class.java.classLoader),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeString(license)
        parcel.writeString(baseUri)
        parcel.writeString(assetBaseUri)
        parcel.writeString(userId)
        parcel.writeParcelable(source, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<EditorSettings> {
        override fun createFromParcel(parcel: Parcel): EditorSettings = EditorSettings(parcel)

        override fun newArray(size: Int): Array<EditorSettings?> = arrayOfNulls(size)

        fun createFromMap(map: Map<String, Any>): EditorSettings? =
            try {
                val license = map["license"] as? String ?: throw (Exception("Missing value for key 'license.'"))
                val sceneBaseUri = map["sceneBaseUri"] as? String ?: throw (Exception("Missing value for key 'sceneBaseUri.'"))
                val assetBaseUri = map["assetBaseUri"] as? String
                val userId = map["userId"] as? String
                val source = (map["source"] as? Map<String, Any>)?.let { Source.createFromMap(it) }
                EditorSettings(license, sceneBaseUri, assetBaseUri, userId, source)
            } catch (e: Exception) {
                null
            }
    }
}
