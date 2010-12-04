
package org.papamitra.locationalarm

import android.provider.BaseColumns
import android.net.Uri
import android.database.Cursor

import org.scalaandroid.AndroidHelper._

object AlarmOld{

  object Columns extends BaseColumns{
    val CONTENT_URI = Uri.parse("content://org.papamitra.locationalarm/alarm")
    val LABEL = "label"
    val ADDRESS = "address"
    val ENABLED = "enabled"
    val LONGITUDE = "longitude"
    val LATITUDE = "latitude"
    val INITIALIZED = "initialized"

    val SHOUR = "shour"
    val SMINUTE = "sminute"
    val MIN = "min"
    val NEXTMILLIS = "nextmillis"

    val ALARM_QUERY_COLUMNS = Array[String]( BaseColumns._ID, LABEL,
					    ADDRESS, ENABLED,
					    LONGITUDE, LATITUDE, INITIALIZED,
					    SHOUR, SMINUTE, MIN,
					    NEXTMILLIS)
    val ALARM_ID_INDEX = 0
    val ALARM_LABEL_INDEX = 1
    val ALARM_ADDRESS_INDEX = 2
    val ALARM_ENABLED_INDEX = 3
    val ALARM_LONGITUDE_INDEX = 4
    val ALARM_LATITUDE_INDEX = 5
    val ALARM_INITIALIZED_INDEX = 6

    val ALARM_SHOUR_INDEX = 7
    val ALARM_SMINUTE_INDEX = 8
    val ALARM_MIN_INDEX = 9
    val ALARM_NEXTMILLIS_INDEX = 10

  }
}
