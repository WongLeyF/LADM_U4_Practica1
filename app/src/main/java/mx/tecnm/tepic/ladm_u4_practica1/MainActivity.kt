package mx.tecnm.tepic.ladm_u4_practica1

import android.Manifest
import android.R
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import mx.tecnm.tepic.ladm_u4_practica1.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var listCalls =
        listOf<String>(CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.TYPE).toTypedArray()
    private var dataArray = ArrayList<String>()
    var idArray = ArrayList<String>()
    var dB = dB(this, "dbMessages", null, 1)
    var db = FirebaseFirestore.getInstance()
    var numbers = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        checkPerm()
        getnumber()
        loadMissedCalls()


        binding.saveMessage.setOnClickListener {
            saveMessages()
        }
        binding.lostCalls.setOnClickListener {
            loadMissedCalls()
            binding.missedCallsList.adapter =
                ArrayAdapter<String>(this, R.layout.simple_list_item_1, dataArray)
            this.registerForContextMenu(binding.missedCallsList)
        }
        binding.addContact.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }


    }

    private fun checkPerm() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                369
            )
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                1
            )
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) ActivityCompat.requestPermissions(
            this,
            Array(1) { Manifest.permission.READ_CALL_LOG }, 101
        ) else displayLog()
    }

    private fun saveMessages() {
        val trans = dB.writableDatabase
        try {
            val data = ContentValues()
            data.put("goodmessage", binding.editTextTextPersonName.text.toString())
            data.put("badmessage", binding.editTextTextPersonName2.text.toString())
            val r = trans.update("Messages", data, "idMessage=?", arrayOf("1"))
            db.collection("MESSAGE").document("1").update("goodMessage", binding.editTextTextPersonName.text.toString())
            db.collection("MESSAGE").document("1").update("badMessage", binding.editTextTextPersonName2.text.toString())
            if (r > 0) Toast.makeText(this, data.toString(), Toast.LENGTH_SHORT)
                .show() else Toast.makeText(this, "No se actualizo", Toast.LENGTH_SHORT).show()
        } catch (e: SQLiteException) {
            Log.w("Error", e.message!!)
        } finally {
            trans.close()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) displayLog()
    }

    private fun displayLog() {
        val transR = dB.readableDatabase
        try {
            val cursor = transR.query(
                "Messages",
                arrayOf("*"),
                "idMessage=?",
                arrayOf("1"),
                null,
                null,
                null
            )
            if (cursor.moveToFirst()) {
                binding.editTextTextPersonName.setText(cursor.getString(1))
                binding.editTextTextPersonName2.setText(cursor.getString(2))
            }
        } catch (e: SQLiteException) {

        } finally {
            transR.close()
        }
    }

    private fun getnumber() {
        try {
            db.collection("CONTEST").addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                for (doc in querySnapshot!!) {
                    val cad = doc.get("telefono")
                    numbers.add(cad.toString())
                }
            }
        } catch (e: Exception) {
            Log.w("Error", e.message!!)
        }
    }

    private fun loadMissedCalls() {
        dataArray.clear()
        val test = contentResolver.query(CallLog.Calls.CONTENT_URI, listCalls, null, null, null)
        if (test != null) {
            if (test.moveToLast()) {
                do {
                    if (test.getInt(2) == 3) {

                        if (numbers.contains(test.getString(1))) {
                            var cad ="#"+ test.getString(0)
                            cad += "\nTelefono: " + test.getString(1)
                            dataArray.add(cad)

                        }
                    }
                } while (test.moveToPrevious())
            }
        }
    }

}
