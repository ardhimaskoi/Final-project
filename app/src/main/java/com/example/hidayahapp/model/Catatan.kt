package com.example.hidayahapp.model


data class Catatan (
        val uid: String = "",
        val judul: String = "",
        val isi: String = "",
        var key: String? = null,
        val gambarList: List<String>? = null
)


