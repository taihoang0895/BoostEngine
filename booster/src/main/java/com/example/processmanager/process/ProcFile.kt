package com.example.processmanager.process

import android.os.Parcel
import android.os.Parcelable
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.lang.StringBuilder

open class ProcFile : File, Parcelable {
     var content: String = ""

    constructor(path: String) : super(path) {
        content = readFile(path)
    }

    constructor(parcel: Parcel) : this(parcel.readString() ?: "") {
        content = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(absolutePath)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun length(): Long {
        return content.length.toLong()
    }

    companion object CREATOR : Parcelable.Creator<ProcFile> {
        @Throws(IOException::class)
        fun readFile(path: String): String {
            val output = StringBuilder()
            val reader = BufferedReader(FileReader(path))
            try {
                var line = reader.readLine()
                var newLine = ""
                while (line != null) {
                    output.append(newLine).append(line)
                    line = reader.readLine()
                    newLine = "\n"
                }
                return output.toString()
            } finally {
                reader.close()
            }
        }

        override fun createFromParcel(parcel: Parcel): ProcFile {
            return ProcFile(parcel)
        }

        override fun newArray(size: Int): Array<ProcFile?> {
            return arrayOfNulls(size)
        }
    }
}