package mx.tecnm.tepic.ladm_u4_practica1_autocontestadora

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.CallLog
import android.telephony.SmsManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_lista_blanca.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    val siPermisoLecturaLlamadas = 1
    val siPermisoEnviarMensajes = 2
    val listaMensajesEnviados = ArrayList<String>()
    var llamadasPerdidas = ArrayList<String>()
    var mensajeListaBlanca = "En este momento no puedo contestar, porfavor espera mi llamada"
    var mensajeListaNegra = "No contestare tus llamadas, favor de no insistir"

    var timer = object : CountDownTimer(5000,5000){
        override fun onTick(p0: Long) {
            llamadas()
            Toast.makeText(this@MainActivity, "Buscando llamadas perdidas...", Toast.LENGTH_SHORT).show()
        }
        override fun onFinish() {
            envioSMS()
            start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Permiso para ver el historial de llamadas
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CALL_LOG), siPermisoLecturaLlamadas)
        }

        //Permiso para enviar SMS
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS), siPermisoEnviarMensajes)
        }

        button.setOnClickListener {
            timer.start()
        }

        button2.setOnClickListener {
            val listaB = Intent(this, ListaBlanca::class.java)
            startActivity(listaB)
        }

        button3.setOnClickListener {
            val listaN = Intent(this, ListaNegra::class.java)
            startActivity(listaN)
        }
    }

    private fun envioSMS() {
        listaMensajesEnviados.clear()
        if (llamadasPerdidas.isNotEmpty()){
            var telefono = ""
            llamadasPerdidas.forEach {
                ListaBlanca().baseRemota.collection("listaBlanca").addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        ListaBlanca().mensaje(error.message!!)
                        return@addSnapshotListener
                    }
                    for (document in querySnapshot!!) {
                        telefono = document.get("telefono").toString()
                        if (listaMensajesEnviados.contains(telefono)) {
                        } else {
                            if (it.equals(document.getString("telefono"))){
                                SmsManager.getDefault().sendTextMessage(
                                    telefono,null,mensajeListaBlanca,null,null)
                                listaMensajesEnviados.add(telefono)
                                Toast.makeText(this, "SE ENVIARON LOS SMS A LOS CONTACTOS DE LISTA BLANCA", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }//for
                }//ListaBlanca
                ListaNegra().baseRemota.collection("listaNegra").addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        ListaNegra().mensaje(error.message!!)
                        return@addSnapshotListener
                    }
                    for (document in querySnapshot!!) {
                        telefono = document.get("telefono").toString()
                        if (listaMensajesEnviados.contains(telefono)) {
                        } else {
                            if (it.equals(document.getString("telefono"))){
                                SmsManager.getDefault().sendTextMessage(
                                    telefono,null,mensajeListaNegra,null,null)
                                listaMensajesEnviados.add(telefono)
                                Toast.makeText(this, "SE ENVIARON LOS SMS A LOS CONTACTOS DE LISTA NEGRA", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }//for
                }//ListaNegra
            }//foreach
        }//if
    }

    @SuppressLint("Range")
    private fun llamadas() {
        var registroLlamadasPerdidas = ArrayList<String>()
        var call = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE
        var cursor = contentResolver.query(Uri.parse("content://call_log/calls"),null, call, null, null)
        llamadasPerdidas.clear()
        var registro = ""
        while (cursor!!.moveToNext()){
            var nombre = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
            var telefono = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
            telefono = telefono.replace(" ".toRegex(), "")
            var fecha = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE))
            var seconds: Long = fecha.toLong()
            var formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SimpleDateFormat("DD-MM-YY HH:mm")
            } else {
                TODO("VERSION.SDK_INT < N")
            }
            var dateString: String = formatter.format(Date(seconds))
            registro = "NOMBRE: ${nombre} \nTEL: ${telefono} \nFECHA: ${dateString}"
            registroLlamadasPerdidas.add(registro)
            llamadasPerdidas.add(telefono)
        }
        listaLlamadasPerdidas.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, registroLlamadasPerdidas)
        cursor.close()
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == siPermisoLecturaLlamadas){
            llamadas()
        }
    }
}