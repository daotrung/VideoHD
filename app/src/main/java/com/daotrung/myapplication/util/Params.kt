package com.daotrung.myapplication.util

import android.media.audiofx.Virtualizer
import android.net.Uri
import android.os.Looper
import com.daotrung.myapplication.BuildConfig
import com.simplemobiletools.commons.helpers.isQPlus

const val PLAYER_SORT_BY_TITLE = 1
const val PLAYER_SORT_BY_TRACK_COUNT = 2
const val PLAYER_SORT_BY_ALBUM_COUNT = 4
const val PLAYER_SORT_BY_YEAR = 8
const val PLAYER_SORT_BY_DURATION = 16
const val PLAYER_SORT_BY_ARTIST_TITLE = 32

const val SORT_DESCENDING = 1024

const val SHOW_FILENAME_NEVER = 1
const val SHOW_FILENAME_IF_UNAVAILABLE = 2
const val SHOW_FILENAME_ALWAYS = 3

const val FOLDER = "folder"
val artworkUri = Uri.parse("content://media/external/audio/albumart")

// shared preferences
const val SHUFFLE = "shuffle"
const val PLAYBACK_SETTING = "playback_setting"
const val AUTOPLAY = "autoplay"
const val CURRENT_PLAYLIST = "current_playlist"
const val SHOW_FILENAME = "show_filename"
const val SWAP_PREV_NEXT = "swap_prev_next"
const val LAST_SLEEP_TIMER_SECONDS = "last_sleep_timer_seconds"
const val SLEEP_IN_TS = "sleep_in_ts"
const val EQUALIZER_PRESET = "EQUALIZER_PRESET"
const val EQUALIZER_BANDS = "EQUALIZER_BANDS"
const val BASS_STRENGTH = "BASS_STRENGTH"
const val VIRTUALIZER_STRENGTH = "VIRTUALIZER_STRENGTH"
const val PLAYBACK_SPEED = "PLAYBACK_SPEED"
const val PLAYBACK_SPEED_PROGRESS = "PLAYBACK_SPEED_PROGRESS"
const val WERE_TRACK_FOLDERS_ADDED = "were_track_folders_added"
const val SHOW_TABS = "show_tabs"
const val WAS_ALL_TRACKS_PLAYLIST_CREATED = "was_all_tracks_playlist_created"

const val TAB_PLAYLISTS = 1
const val TAB_FOLDERS = 2
const val TAB_ARTISTS = 4
const val TAB_ALBUMS = 8
const val TAB_TRACKS = 16
const val ACTIVITY_PLAYLIST_FOLDER = 32


const val PLAYLIST_SORTING = "playlist_sorting"
const val PLAYLIST_TRACKS_SORTING = "playlist_tracks_sorting"
const val FOLDER_SORTING = "folder_sorting"
const val ARTIST_SORTING = "artist_sorting"
const val ALBUM_SORTING = "album_sorting"
const val TRACK_SORTING = "track_sorting"


val allTabsMask = if (isQPlus()) {
    TAB_PLAYLISTS or TAB_FOLDERS or TAB_ARTISTS or TAB_ALBUMS or TAB_TRACKS
} else {
    TAB_PLAYLISTS or TAB_ARTISTS or TAB_ALBUMS or TAB_TRACKS
}


private const val PATH = "com.daotrung.myapplication.action."

//services
const val INIT = PATH + "INIT"
const val INIT_PATH = PATH + "INIT_PATH"
const val INIT_QUEUE = PATH + "INIT_QUEUE"
const val FINISH = PATH + "FINISH"
const val FINISH_IF_NOT_PLAYING = PATH + "FINISH_IF_NOT_PLAYING"
const val PREVIOUS = PATH + "PREVIOUS"
const val PAUSE = PATH + "PAUSE"
const val PLAYPAUSE = PATH + "PLAYPAUSE"
const val NEXT = PATH + "NEXT"
const val EDIT = PATH + "EDIT"
const val PLAY_SONG = PATH + "PLAY_SONG"
const val REFRESH_LIST = PATH + "REFRESH_LIST"
const val UPDATE_NEXT_TRACK = PATH + "UPDATE_NEXT_TRACK"
const val SET_PROGRESS = PATH + "SET_PROGRESS"
const val SKIP_BACKWARD = PATH + "SKIP_BACKWARD"
const val SKIP_FORWARD = PATH + "SKIP_FORWARD"
const val SEEKBAR = PATH + "SEEKBAR"
const val BROADCAST_STATUS = PATH + "BROADCAST_STATUS"
const val NOTIFICATION_DISMISSED = PATH + "NOTIFICATION_DISMISSED"
const val SET_SONG_SPEED = PATH + "SET_SONG_SPEED"
const val UPDATE_QUEUE_SIZE = PATH + "UPDATE_QUEUE_SIZE"


const val PROGRESS = "progress"
const val EDITED_TRACK = "edited_track"
const val ALL_TRACKS_PLAYLIST_ID = 1
const val START_SLEEP_TIMER = "start_sleep_timer"
const val STOP_SLEEP_TIMER = "stop_sleep_timer"
const val TRACK_ID = "track_id"
const val RESTART_PLAYER = "RESTART_PLAYER"
const val EQUALIZER_PRESET_CUSTOM = -1
const val CURRENT_SONG = "current_song"


const val CHECK_EQUALIZER = "check_equalizer"
const val CHECK_BASS = "check_bass"
const val CHECK_TREBLE = "check_treble"


// trung intent
// intent video folder
const val POSITION_FOLDER_VIDEO = "pos"
const val POSITION_FOLDER_VIDEO_EDIT = "pos_edit"
const val CLASS_ITEM_VIDEO_PLAY = "class"

// intent set pin
const val PASS_CODE = "pass_code"
const val SET_QUESTION_PIN = "question_code"
// prefences passCode
const val KEY_PASS_CODE = "key_pass_code"
const val PREFS_NAME_PIN = "kotlincodes"
const val PREFS_QUESTION_PIN = "prefs_question_pin"

// prefences setting video
const val PREFS_SHOW_LAST_PLAY_VIDEO = "prefs_show_last_play_video"
const val KEY_SHOW_LAST_PVIDEO = "keyshowlastvideo"
const val PREFS_SHOW_RESUME_STATUS = "prefs_show_resume_status"
const val KEY_SHOW_RESUME_STATUS = "keyshowresumestatus"

// prefences now time next prev video
const val PREFS_TIME_VIDEO = "prefs_time_video"
const val KEY_TIME_VIDEO = "key_time_video"

// prefences remember brightness
const val PREF_BRIGHTNESS = "prefs_brightness"
const val PREF_BRIGHTNESS_VALUE = "prefs_br_value"
const val KEY_BRIGHTNESS_VALUE = "prefs_br_value"
const val KEY_BRIGHTNESS = "key_brightness"

// prefences video size filter
const val PREF_SIZE_FILTER = "prefs_size_filter"
const val KEY_SIZE_FILTER = "key_size_filter"
