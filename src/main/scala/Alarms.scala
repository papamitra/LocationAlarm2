
package org.papamitra.locationalarm

import android.content.{Context, ContentValues, ContentUris, ContentResolver, Intent}
import android.app.{PendingIntent, AlarmManager}

import android.util.Log

import java.text.SimpleDateFormat
import java.util.Calendar

object Alarms{
  import org.scalaandroid.AndroidHelper._

  val ALERT_START = "alert_start"

  val ALARM_ACTIVE = "org.papamitra.locationalarm.ALARM_ACTIVE"

  val ALARM_ALERT_ACTION = "org.papamitra.locationalarm.ALARM_ALERT"

  val ALARM_INTENT_INITIALIZED = "org.papamitra.locationalarm.intent.extra.initialized"
  val ALARM_INTENT_LATITUDE = "org.papamitra.locationalarm.intent.extra.latitude"
  val ALARM_INTENT_LONGITUDE = "org.papamitra.locationalarm.intent.extra.longitude"

  val ALARM_KILLED = "alarm_killed"

  def calculateNextMillis(alarm:Alarm):Long = calculateAlarm(alarm.ttl.shour, alarm.ttl.sminute).getTimeInMillis

  def calculateAlarm(hour:Int, minute:Int):Calendar = {
    val c = Calendar.getInstance withAction(
      _ setTimeInMillis(System.currentTimeMillis))
    
    val nowHour = c.get(Calendar.HOUR_OF_DAY)
    val nowMinute = c.get(Calendar.MINUTE)
    
    if((hour < nowHour) || (hour == nowHour && minute <= nowMinute)){
      c.add(Calendar.DAY_OF_YEAR, 1)
    }
    
    c withActions(
      _ set(Calendar.HOUR_OF_DAY, hour),
      _ set(Calendar.MINUTE, minute),
      _ set(Calendar.SECOND, 0),
      _ set(Calendar.MILLISECOND, 0))
  }

  def getCalendar(hour:Int, minute:Int, nowMillis:Long):Calendar = 
    Calendar.getInstance withActions(
      _ setTimeInMillis(nowMillis),
      _ set(Calendar.HOUR_OF_DAY, hour),
      _ set(Calendar.MINUTE, minute),
      _ set(Calendar.SECOND, 0),
      _ set(Calendar.MILLISECOND, 0))

  def enableAlert(context:Context, atTimeInMillis:Long){
    Log.i(TAG, "set alert:" + formatDate(Calendar.getInstance.withAction(_ setTimeInMillis(atTimeInMillis))))

    val am = context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
    val intent = new Intent(ALARM_ACTIVE)
    val sender:PendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

    am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender)
  }

  def disableAlert(context:Context){
    val am = context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
    val sender = PendingIntent.getBroadcast(
      context, 0, new Intent(ALARM_ACTIVE),
      PendingIntent.FLAG_CANCEL_CURRENT)

    am.cancel(sender);
    // TODO:setStatusBarIcon(context, false);
  }

  private def formatDate(cal:Calendar) = 
    (new SimpleDateFormat("yyyy/MM/dd/ HH:mm")).format(cal.getTime())

}
