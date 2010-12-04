
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
  def dbTableName = "alrams"
  def dbContext = context
  val dbFileName = "alarms.db"
  val dbVersion = 5

  override def onCreate(db: SQLiteDatabase) {
    super.onCreate(db)
    val initRecord = new Alarm
    initRecord.label("Alarm").address("").enabled(false).initialized(true).min(5)

    for(i <- 0 until 3)insert(initRecord)

  }
}

object LocationAlarmProvider {
  import android.content.UriMatcher
  private val sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH)

  private val ALARMS: Int = 1
  private val ALARMS_ID: Int = 2

  sURLMatcher.addURI("org.papamitra.locationalarm", "alarm", ALARMS);
  sURLMatcher.addURI("org.papamitra.locationalarm", "alarm/#", ALARMS_ID);
}

class LocationAlarmProvider extends ContentProvider {
  import LocationAlarmProvider._

  private val DATABASE_NAME = "alarms.db"
  private val DATABASE_VERSION = 5

  private lazy val mOpenHelper = new DBHelper(getContext())

  class DBHelper(context: Context) extends SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override def onCreate(db: SQLiteDatabase) {
      Log.i(TAG, "DBHelper#onCreate")
      db.execSQL("CREATE TABLE alarms (" +
        "_id INTEGER PRIMARY KEY," +
        "label TEXT, " +
        "address TEXT, " +
        "enabled INTERGER, " +
        "longitude REAL, " +
        "latitude REAL, " +
        "initialized INTEGER, " +
        "shour INTEGER, " +
        "sminute INTEGER, " +
        "min INTEGER," +
        "nextmillis INTEGER);")

      val insertMe = "INSERT INTO alarms " +
        "(label, address, enabled, latitude, longitude, initialized, shour, sminute, min, nextmillis) " +
        "VALUES "

      db.execSQL(insertMe + "('Alarm1','',0, 0.0, 0.0, 1, 0,0,5,0);")
      db.execSQL(insertMe + "('Alarm2','',0, 0.0, 0.0, 1, 0,0,5,0);")
      db.execSQL(insertMe + "('Alarm3','',0, 0.0, 0.0, 1, 0,0,5,0);")
    }

    override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, currentVersion: Int) {
      Log.i(TAG, "onUpgrade")
      db.execSQL("DROP TABLE IF EXISTS alarms")
      onCreate(db)
    }
  }

  override def onCreate() = true

  override def query(url: Uri, projectionIn: Array[String], selection: String, selectionArgs: Array[String], sort: String): Cursor = {
    val qb = new SQLiteQueryBuilder()

    sURLMatcher.`match`(url) match {
      case ALARMS =>
        qb.setTables("alarms")
      case ALARMS_ID =>
        qb.setTables("alarms")
        qb.appendWhere("_id=")
        qb.appendWhere(url.getPathSegments().get(1))
    }

    val db = mOpenHelper.getReadableDatabase

    val ret = qb.query(db, projectionIn, selection, selectionArgs, null, null, sort)

    ret match {
      case null =>
        Log.v(TAG, "Alarms.query: failed")
      case _ =>
        ret.setNotificationUri(getContext.getContentResolver, url)
    }

    return ret
  }

  override def getType(url: Uri): String = "vnd.android.cursor.dir/alarms"

  override def update(url: Uri, values: ContentValues, where: String, whereArgs: Array[String]): Int = {
    val db = mOpenHelper.getWritableDatabase

    val segment = url.getPathSegments().get(1)
    val rowId = java.lang.Long.parseLong(segment)
    val count = db.update("alarms", values, "_id= " + rowId, null)

    Log.i(TAG, "update")
    getContext.getContentResolver.notifyChange(url, null)
    return count
  }

  // TODO
  override def insert(url: Uri, initialValues: ContentValues): Uri = url
  override def delete(url: Uri, where: String, whereArgs: Array[String]): Int = 0

}
