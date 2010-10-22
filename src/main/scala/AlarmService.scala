
package org.papamitra.locationalarm

import android.app.{Service, KeyguardManager}
import android.content.{Context, Intent, IntentFilter, BroadcastReceiver}
import android.os.{Handler, IBinder, Bundle}
import android.location.{Location,LocationListener, LocationManager}
import android.util.Log

class AlarmService extends Service with LocationListener{
  import scala.math

  val MIN_TIME:Int = 0
  val MIN_DISTANCE:Int = 1

  private lazy val alertReceiver = new BroadcastReceiver(){
    override def onReceive(context:Context, intent:Intent){

      val km = context.getSystemService(Context.KEYGUARD_SERVICE).asInstanceOf[KeyguardManager]
      val alarmAlert = km.inKeyguardRestrictedInputMode match{
	case true =>
	  new Intent(context, classOf[AlarmAlertFullScreen])
	case false =>
	  new Intent(context, classOf[AlarmAlert])
      }

      alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
			  | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
      context.startActivity(alarmAlert)
      context.startService(new Intent(context, classOf[AlarmKlaxon]))
    }
  }

  override def onBind(intent:Intent):IBinder = null
  
  override def onCreate(){
    super.onCreate()
    val locationManager = getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this)
    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this)

    registerReceiver(alertReceiver, new IntentFilter(Alarms.ALERT_START))

    Log.i(TAG, "AlarmService.onCreate")
  }

  override def onStartCommand(intent:Intent, flags:Int, startId:Int):Int = {
    Log.i(TAG, "AlarmService.onStartCommand id = " + startId)

    val alarms = Alarms.getAllAlarm(getContentResolver)

    Alarms.disableAlert(this)

    if(!alarms.exists(_.enabled)){
      Log.i(TAG, "No Alarm Enabled")
      stopSelf()
      return Service.START_NOT_STICKY
    }

    val currentMillis = System.currentTimeMillis
    if(!alarms.exists(_.isActiveAt(currentMillis))){
      Log.i(TAG, "No Alarm Enabled")
      // 直近のアラーム起動時間を算出
      val minNextMillis = alarms.filter(_.enabled)
				  .map(Alarms.calculateNextMillis(_))
				  .reduceLeft(math.min(_,_))
      Alarms.enableAlert(this,minNextMillis)
      stopSelf()
      return Service.START_NOT_STICKY
    }
    
    return Service.START_STICKY
  }

  override def onDestroy(){
    Log.i(TAG, "Service Stop")

    val locationManager = getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]
    locationManager.removeUpdates(this)

    unregisterReceiver(alertReceiver)

    super.onDestroy()
  }

  private def checkAlert(location:Location){
    for(alarm <- Alarms.getAllAlarm(getContentResolver)
			.filter(_.isActiveAt(System.currentTimeMillis))){
      val result = Array[Float](1)
      Location.distanceBetween(location.getLatitude, location.getLongitude,
			       alarm.latitude, alarm.longitude, result)
      Log.i(TAG,"Distance:" + result(0))

      if(result(0) < 1500){ // TODO
	Log.i(TAG, "!!!!! Location Alert !!!!!")
	// アラート発動
	sendBroadcast(new Intent(Alarms.ALERT_START))

	disabledAlarm(alarm)

      }
    }
  }
    
  private def disabledAlarm(alarm:Alarm) {
    if(alarm.enabled)
      Alarms.updateAlarm(this, alarm, Alarms.calculateNextMillis(alarm))
  }

  override def onLocationChanged(location:Location){
    Log.i(TAG, format("onLocationChanged (%f, %f)", location.getLatitude, location.getLongitude))
    checkAlert(location)
    startService(new Intent(this, classOf[AlarmService]))
  }

  // TODO
  override def onProviderDisabled(provider:String){
    Log.i(TAG, "Provider Disable: " + provider)
  }

  override def onProviderEnabled(provider:String){
    Log.i(TAG, "Provider Enable: " + provider)
  }

  override def onStatusChanged(provider:String,status:Int,extras:Bundle){
    Log.i(TAG, "Provider Status Change: " + provider + ":" + status.toString)
  }
  
}
