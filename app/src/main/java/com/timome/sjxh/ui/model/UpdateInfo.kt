package com.timome.sjxh.ui.model

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val fileSize: Long
)