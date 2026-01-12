package com.example.strivo.dataclass

data class userData(
    val cabinNo:String?=null,
    val email:String?=null,
    val name:String?=null,
    val photoBase64:String?=null,
    val phoneNo:String?=null,
    val role:String?=null,
    val staffId:String?=null,
    val staffRoomId:String?=null,
    val updatedAt:String?=null,
    val timetable: Map<String, TimetableEntry>? = null
)
