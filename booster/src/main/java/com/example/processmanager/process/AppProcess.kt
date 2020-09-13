package com.example.processmanager.process

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import java.io.File
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class AppProcess : AndroidProcess {
    private val SYS_SUPPORTS_SCHEDGROUPS = File("/dev/cpuctl/tasks").exists()
    private val PROCESS_NAME_PATTERN = Pattern.compile("^([A-Za-z]{1}[A-Za-z0-9_]*[\\.|:])*[A-Za-z][A-Za-z0-9_]*$")

    private var isForeground = false
    private var uid: Int = -1

    @Throws(IOException::class, IllegalArgumentException::class)
    constructor(pId: Int) : super(pId) {
        if (name.isEmpty() || !PROCESS_NAME_PATTERN.matcher(name).matches()) {
            throw IllegalArgumentException("pId is invalid")
        }

        if (SYS_SUPPORTS_SCHEDGROUPS) {
            val cgroup = Cgroup.get(pId)
            val cpuacct = cgroup.getGroup("cpuacct")
            val cpu = cgroup.getGroup("cpu")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (cpu == null || cpuacct == null || !cpuacct.group.contains("pid_")) {
                    throw IllegalArgumentException("pId is invalid")
                }
                isForeground = !cpu.group.contains("bg_non_interactive")
                try {
                    uid = cpuacct.group.split("/")[1].replace("uid_", "").toInt()
                } catch (e: Exception) {
                    uid = Status.get(pId).getUid()
                }
            } else {
                if (cpu == null || cpuacct == null || !cpuacct.group.contains("pid_")) {
                    throw IllegalArgumentException("pId is invalid")
                }
                isForeground = !cpu.group.contains("bg_non_interactive")
                try {
                    uid = cpuacct.group.substring(cpuacct.group.lastIndexOf("/") + 1).toInt()
                } catch (e: Exception) {
                    uid = Status.get(pId).getUid()
                }
            }
        } else {
            val stat = Stat.get(pId)
            val status = Status.get(pId)

            isForeground = stat.policy() == 0;
            uid = status.getUid()
        }
    }

    constructor(parcel: Parcel) : super(parcel) {
        isForeground = parcel.readByte().toInt() != 0x00
        uid = parcel.readInt()
    }

    fun getPackageName(): String {
        return name.split(":")[0]
    }

    /**
     * Retrieve overall information about the application package.
     *
     *
     * Throws [PackageManager.NameNotFoundException] if a package with the given name can
     * not be found on the system.
     *
     * @param context the application context
     * @param flags   Additional option flags. Use any combination of
     * [PackageManager.GET_ACTIVITIES], [PackageManager.GET_GIDS],
     * [PackageManager.GET_CONFIGURATIONS], [PackageManager.GET_INSTRUMENTATION],
     * [PackageManager.GET_PERMISSIONS], [PackageManager.GET_PROVIDERS],
     * [PackageManager.GET_RECEIVERS], [PackageManager.GET_SERVICES],
     * [PackageManager.GET_SIGNATURES], [PackageManager.GET_UNINSTALLED_PACKAGES]
     * to modify the data returned.
     * @return a PackageInfo object containing information about the package.
     */
    @Throws(PackageManager.NameNotFoundException::class)
    fun getPackageInfo(context: Context, flags: Int): PackageInfo? {
        return context.packageManager.getPackageInfo(getPackageName(), flags)
    }



    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeByte((if (isForeground) 0x01 else 0x00).toByte())
        dest.writeInt(uid)
    }

    companion object CREATOR : Parcelable.Creator<AppProcess> {
        override fun createFromParcel(parcel: Parcel): AppProcess {
            return AppProcess(parcel)
        }

        override fun newArray(size: Int): Array<AppProcess?> {
            return arrayOfNulls(size)
        }
    }

}