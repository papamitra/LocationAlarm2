
package org.papamitra.locationalarm

import android.content.{ ContentProvider, ContentUris, ContentValues, Context, UriMatcher }
import android.database.{ Cursor, SQLException }
import android.database.sqlite.{ SQLiteDatabase, SQLiteOpenHelper, SQLiteQueryBuilder }
import android.net.Uri
import android.text.TextUtils

import android.util.Log

import org.papamitra.android.dbhelper._

case class TTL(val shour:Int, val sminute:Int, val min:Int){
  import java.util.Calendar
  
  def isInTimeRange(nowMillis:Long):Boolean = {
    val smin = shour * 60 + sminute

    val c = Calendar.getInstance
    c.setTimeInMillis(nowMillis)
    val nowmin = (c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE))

    if(nowmin < smin){
      (nowmin + (24 * 60) - smin) < min
    }else{
      (nowmin - smin) < min
    }
  }
}

class Alarm extends Mapper[Alarm] {
  def primaryKeyField = _id

  object _id extends MappedInt(this)
  object label extends MappedString(this)
  object address extends MappedString(this)
  object enabled extends MappedBoolean(this)
  object longitude extends MappedDouble(this)
  object latitude extends MappedDouble(this)
  object initialized extends MappedBoolean(this)
  object shour extends MappedInt(this)
  object sminute extends MappedInt(this)
  object min extends MappedInt(this)
  object nextmillis extends MappedInt(this)

  def ttl = TTL(shour.is,sminute.is,min.is)

  def isActiveAt(nowMillis:Long):Boolean = enabled.is match {
    case false => false
    case true if nextmillis.is > nowMillis => false
    case true => ttl.isInTimeRange(nowMillis)
  }

}

class AlarmTable(context:Context) extends Alarm with MetaMapper[Alarm] with SingleTableDBHelper[Alarm] {
  def dbTableName = "alarms"
  def dbContext = context
  val dbFileName = "alarms.db"
  val dbVersion = 5

  override def onCreate(db: SQLiteDatabase) {
    super.onCreate(db)
    val initRecord = new Alarm
    initRecord.label("Alarm").address("").enabled(false).initialized(true).min(5)

    for(i <- 0 until 3)insert(initRecord,db)

  }
}
