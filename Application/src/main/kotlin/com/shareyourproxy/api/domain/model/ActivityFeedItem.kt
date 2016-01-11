package com.shareyourproxy.api.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.util.BaseParcelable
import java.util.*

/**
 * Make an item for user activity feeds.
 */
internal data class ActivityFeedItem(val handle: String, val subtext: String, val channelType: ChannelType, val actionAddress: String, val timestamp: Date?, val isError: Boolean) : BaseParcelable {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(handle)
        dest.writeString(subtext)
        dest.writeValue(channelType)
        dest.writeString(actionAddress)
        dest.writeValue(timestamp)
        dest.writeValue(isError)
    }

    companion object {
        val CL = ActivityFeedItem::class.java.classLoader
        val CREATOR = object : Parcelable.Creator<ActivityFeedItem> {
            override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
            override fun newArray(size: Int): Array<ActivityFeedItem?> = arrayOfNulls(size)
        }
        private fun readParcel(parcel: Parcel) = ActivityFeedItem(parcel.readString(), parcel.readString(), parcel.readValue(CL) as ChannelType, parcel.readString(), parcel.readValue(CL) as Date, parcel.readValue(CL) as Boolean)
    }
}
