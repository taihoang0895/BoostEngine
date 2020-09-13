package com.example.processmanager.process

import android.os.Parcel
import android.os.Parcelable
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 * <p>/proc/[pid]/cgroup (since Linux 2.6.24)</p>
 *
 * <p>This file describes control groups to which the process/task belongs. For each cgroup
 * hierarchy there is one entry containing colon-separated fields of the form:</p>
 *
 * <p>5:cpuacct,cpu,cpuset:/daemons</p>
 *
 * <p>The colon-separated fields are, from left to right:</p>
 *
 * <ol>
 * <li>hierarchy ID number</li>
 * <li>set of subsystems bound to the hierarchy</li>
 * <li>control group in the hierarchy to which the process belongs</li>
 * </ol>
 *
 * <p>This file is present only if the CONFIG_CGROUPS kernel configuration option is enabled.</p>
 *
 * @see ControlGroup
 */
class Cgroup : ProcFile {
    companion object CREATOR : Parcelable.Creator<Cgroup> {
        @Throws(IOException::class)
        fun get(pId: Int): Cgroup {
            return Cgroup(String.format(Locale.ENGLISH, "/proc/%d/cgroup", pId))
        }

        override fun createFromParcel(parcel: Parcel): Cgroup {
            return Cgroup(parcel)
        }

        override fun newArray(size: Int): Array<Cgroup?> {
            return arrayOfNulls(size)
        }
    }

    private val groups = ArrayList<ControlGroup>()

    private constructor(path: String) : super(path) {
        val lines = content.split("\n")
        lines.forEach {
            try {
                groups.add(ControlGroup(it))
            } catch (e: Exception) {

            }

        }
    }

    private constructor(parcel: Parcel) : super(parcel) {
        parcel.createTypedArrayList(ControlGroup.CREATOR)?.forEach {
            groups.add(it)
        }
    }

    fun getGroup(subsystem: String): ControlGroup? {
        groups.forEach {
            val systems = it.subsystems.split(",")
            systems.forEach { name ->
                if (name.equals(subsystem)) {
                    return it
                }
            }
        }
        return null
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeTypedList(groups)
    }
}