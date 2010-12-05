package org.papamitra.locationalarm

import android.content.{Context, Intent}
import android.app.Activity
import android.os.Bundle
import android.view.{View, ViewGroup, LayoutInflater}
import android.view.View.OnClickListener
import android.widget.{ArrayAdapter, TextView, CheckBox, ListView, AdapterView, CursorAdapter}
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast

import android.database.Cursor
import android.content.res.Configuration

import android.util.Log

object LocationAlarms {
  val ALARM_ID = "alarm_id"
}

class LocationAlarms extends Activity with TypedActivity{
  import org.scalaandroid.AndroidHelper._
  import TypedResource._

  lazy val alarmTable = new AlarmTable(this)

  private class AlarmAdapter(val context:Context, val cursor:Cursor) extends CursorAdapter(context, cursor){
    override def newView(context:Context, cursor:Cursor, parent:ViewGroup ):View = {
      val inflater = LayoutInflater.from(context)
      val v = inflater.inflate(R.layout.list_row, parent, false)
      bindView(v, context, cursor)
      return v
    }

    override def bindView(view:View, context:Context, cursor:Cursor){
      val alarm = alarmTable.cursorToMapper(cursor)
      view.findView(TR.list_enabled) match {
	case null =>
	case cb =>
	  cb.setChecked(alarm.enabled.is)
	  cb.setOnClickListener(new OnClickListener(){
	    override def onClick(v:View){
//	      Alarms.enabledAlarm(LocationAlarms.this, alarm.id,
//				  v.asInstanceOf[CheckBox].isChecked)
	      alarm.enabled(v.asInstanceOf[CheckBox].isChecked)
	      alarmTable.update(alarm)
	      startService(new Intent(LocationAlarms.this, classOf[AlarmService]))
	    }
	  })
      }

      view.findView(TR.list_address) match{
	  case null =>
	  case tv => 
	    tv.setText( alarm.address.is)
      }

      view.findView(TR.list_label) match{
	case null =>
	case tv =>
	  tv.setText( alarm.label.is)
      }
    }
  }

  override def onCreate(savedInstanceState:Bundle) {
    super.onCreate(savedInstanceState)
    updateLayout
  }

  override def onConfigurationChanged(newConfig:Configuration) {
    super.onConfigurationChanged(newConfig)
    updateLayout()
  }

  def updateLayout(){
    Log.i(TAG, "LocationAlarms.updateLayout")
    setContentView(R.layout.main)

    var listview = findView(TR.list)
    listview.setAdapter(new AlarmAdapter(this, alarmTable.findAllCursor()))
    listview.setEmptyView(findView(TR.empty))

    listview.setOnItemClickListener(
      new OnItemClickListener(){
	override def onItemClick(av:AdapterView[_] , view:View, pos:Int, id:Long){
	  startActivity((new Intent(LocationAlarms.this, classOf[SetAlarm])
			 .putExtra(LocationAlarms.ALARM_ID, id)))
	}
      })
  }
}
