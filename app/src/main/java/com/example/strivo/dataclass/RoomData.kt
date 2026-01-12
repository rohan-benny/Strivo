package com.example.strivo.dataclass

data class RoomData( // <--- 'data' keyword and primary constructor (parentheses)
    val capacity: String? = null,
    val id: String? = null,
    val name: String? = null,
    val type: String? = null
)