package com.daotrung.myapplication.model

import java.util.ArrayList

class Events {
    class SongChanged(val song: MusicLocal?)
    class NextSongChanged(val song: MusicLocal?)
    class SongStateChanged(val isPlaying: Boolean)
    class QueueUpdated(val tracks: ArrayList<MusicLocal>)
    class ProgressUpdated(val progress: Int)
    class SleepTimerChanged(val seconds: Int)
    class NoStoragePermission
    class Event(val keyCode : Int)
    class PlaylistsUpdated
    class TrackDeleted
    class RefreshTracks
}