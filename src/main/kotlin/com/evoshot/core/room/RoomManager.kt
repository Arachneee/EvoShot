package com.evoshot.core.room

class RoomManager {

    private val globalRoom = Room(id = "global", maxPlayers = 2000)

    fun getGlobalRoom(): Room = globalRoom
}

