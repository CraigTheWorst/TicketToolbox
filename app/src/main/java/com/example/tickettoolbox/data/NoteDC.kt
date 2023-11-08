package com.example.tickettoolbox.data

data class NoteDC(
    var id: Long = 0,
    var title: String = "",
    var content: String = "",
    var creationTime: Long = System.currentTimeMillis(),
    var modifiedTime: Long = System.currentTimeMillis()
) : Comparable<NoteDC> {

    override fun compareTo(other: NoteDC): Int {
        return when {
            modifiedTime > other.modifiedTime -> -1
            modifiedTime < other.modifiedTime -> 1
            else -> 0
        }
    }

}

