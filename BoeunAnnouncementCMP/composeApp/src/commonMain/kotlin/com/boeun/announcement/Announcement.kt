package com.boeun.announcement

import kotlinx.serialization.Serializable

@Serializable
data class Announcement(
    val id: String,
    val title: String,
    val content: String? = null,
    val publishDate: String,
    val deadlineDate: String? = null,
    val url: String,
    val category: String? = null,
    val bbsNo: Int = 68,
    val isNew: Boolean = false
)
