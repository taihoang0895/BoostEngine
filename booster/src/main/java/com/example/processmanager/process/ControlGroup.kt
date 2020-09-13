package com.example.processmanager.process

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class ControlGroup : Parcelable {
    val id: Int
    val subsystems: String
    val group: String

    constructor(line: String) {
        val fields = line.split(":")
        id = fields[0].toInt()
        subsystems = fields[1]
        group = fields[2]
    }

    constructor(parcel: Parcel) {
        id = parcel.readInt()
        subsystems = parcel.readString() ?: ""
        group = parcel.readString() ?: ""
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(subsystems)
        dest.writeString(group)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return String.format(Locale.ENGLISH, "%d:%s:%s", id, subsystems, group)
    }

    companion object CREATOR : Parcelable.Creator<ControlGroup> {
        override fun createFromParcel(parcel: Parcel): ControlGroup {
            return ControlGroup(parcel)
        }

        override fun newArray(size: Int): Array<ControlGroup?> {
            return arrayOfNulls(size)
        }
    }
}