package com.ashomok.chatoflegends.model.realms

import android.os.Parcelable
import io.realm.RealmObject
import io.realm.internal.Keep
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
open class TextStatus(
    var text: String = ""
) : RealmObject(), Parcelable
