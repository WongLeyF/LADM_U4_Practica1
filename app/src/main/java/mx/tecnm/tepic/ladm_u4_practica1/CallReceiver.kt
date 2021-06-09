package mx.tecnm.tepic.ladm_u4_practica1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

private var ringgingState = false


class CallReceiver : BroadcastReceiver() {
    var db = FirebaseFirestore.getInstance()
    var numbers = ArrayList<String>()
    var numberNo = ArrayList<String>()
    var good = " "
    var bad = " "

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING)
            ringgingState = true
        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_OFFHOOK)
            ringgingState = false
        if (ringgingState && intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_IDLE) {
            val number = intent.extras?.getString("incoming_number")
            //intent.extras?.getString("incoming_number")?.let { showToastMsg(context!!, it) }
            getmessage()
            db.collection("CONTEST").addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                numbers.clear()
                numberNo.clear()
                for (doc in querySnapshot!!) {
                    val cad = doc.get("telefono")
                    if (doc.getBoolean("deseado") == true)
                        numbers.add(cad.toString())
                    else numberNo.add(cad.toString())
                }

                if (numbers.contains(number)) {
                    val sms = SmsManager.getDefault()
                    sms.sendTextMessage(number, null, good, null, null)
                    intent.extras?.getString("incoming_number")?.let { showToastMsg(context!!, it) }
                }
                if (numberNo.contains(number)) {
                    val sms = SmsManager.getDefault()
                    sms.sendTextMessage(number, null, bad, null, null)
                    intent.extras?.getString("incoming_number")?.let { showToastMsg(context!!, it) }
                }
            }
        }

    }

    fun getmessage() {
        db.collection("MESSAGE").addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            querySnapshot!!.documents
            for (doc in querySnapshot) {
                bad = "${doc.getString("badMessage")}"
                good = "${doc.getString("goodMessage")}"
            }
        }
    }

    fun showToastMsg(c: Context, msg: String) {
        val toast = Toast.makeText(c, msg, Toast.LENGTH_LONG)
        toast.show()
    }
}