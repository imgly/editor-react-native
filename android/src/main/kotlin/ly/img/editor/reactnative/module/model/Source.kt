package ly.img.editor.reactnative.module.model

import android.os.Parcel
import android.os.Parcelable

data class Source(
    var source: String,
    val type: EditorSourceType,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: throw (Exception("Missing value for key 'source'.")),
        parcel.readParcelable(EditorSourceType::class.java.classLoader) ?: throw (Exception("Missing value for key 'type'.")),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeString(source)
        parcel.writeParcelable(type, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Source> {
        override fun createFromParcel(parcel: Parcel): Source = Source(parcel)

        override fun newArray(size: Int): Array<Source?> = arrayOfNulls(size)

        fun createFromMap(map: Map<String, Any>): Source? =
            runCatching {
                val source = map["source"] as? String
                val typeRaw = map["type"] as? String

                source?.let { s ->
                    typeRaw?.let { t ->
                        EditorSourceType.fromValue(t)?.let { type ->
                            Source(s, type)
                        }
                    }
                }
            }.getOrNull()
    }
}
