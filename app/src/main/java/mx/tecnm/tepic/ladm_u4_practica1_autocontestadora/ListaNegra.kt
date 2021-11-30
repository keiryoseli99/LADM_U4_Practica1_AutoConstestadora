package mx.tecnm.tepic.ladm_u4_practica1_autocontestadora

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_lista_negra.*

class ListaNegra : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()
    var dataListaNegra = ArrayList<String>()
    var listaID = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_negra)

        baseRemota.collection("listaNegra")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    mensaje(error.message!!)
                    return@addSnapshotListener
                }
                dataListaNegra.clear()
                listaID.clear()
                for (document in querySnapshot!!){
                    var cadena = "NOMBRE: ${document.getString("nombre")} \nTEL: ${document.get("telefono")}"
                    dataListaNegra.add(cadena)
                    listaID.add(document.id.toString())
                }//for
                listaNegra.adapter = ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, dataListaNegra)
                listaNegra.setOnItemClickListener { adapterView, view, posicion, l ->
                    eliminarActualizar(posicion)
                }
            }//collection

        buttonLN.setOnClickListener {
            insertar()
        }
    }

    fun eliminarActualizar(posicion: Int) {
        var id = listaID.get(posicion)

        AlertDialog.Builder(this)
            .setTitle("ATENCION")
            .setMessage("¿QUE DESEA HACER CON\n${dataListaNegra.get(posicion)}?")
            .setPositiveButton("ACTUALIZAR"){d,i-> leerDatos(id)}
            .setNeutralButton("CANCELAR"){d,i-> d.cancel()}
            .setNegativeButton("ELIMINAR"){d,i -> eliminar(id)}
            .show()
    }

    fun leerDatos(id: String) {
        val docRef = baseRemota.collection("listaNegra").document(id)
        docRef.get()
            .addOnSuccessListener { document ->
                val nombre = document.getString("nombre")
                val tel = document.getString("telefono")
                nombreLN.setText(nombre)
                telLN.setText(tel)
            }
        actualizarLN.setOnClickListener {
            actualizar(id)
        }
    }
    fun actualizar(id: String) {
        baseRemota.collection("listaNegra")
            .document(id)
            .update(
                mapOf(
                    "nombre" to nombreLN.text.toString(),
                    "telefono" to telLN.text.toString()
                )
            )
            .addOnSuccessListener {
                alerta("SE ACTUALIZO CONTACTO CON EXITO")
            }
            .addOnFailureListener {
                mensaje("ERROR: ${it.message!!}")
            }
        nombreLN.setText("")
        telLN.setText("")
    }

    fun eliminar(id: String) {
        baseRemota.collection("listaNegra")
            .document(id)
            .delete()
            .addOnSuccessListener {
                alerta("SE ELIMINO CONTACTO CON EXITO")
            }
            .addOnFailureListener {
                mensaje("ERROR: ${it.message!!}")
            }
    }

    fun insertar() {
        var datosContacto = hashMapOf(
            "nombre" to nombreLN.text.toString(),
            "telefono" to telLN.text.toString()
        )

        baseRemota.collection("listaNegra")
            .add(datosContacto as Any)
            .addOnSuccessListener {
                alerta("SE AGREGO CONTACTO EN LA NUBE")
            }
            .addOnFailureListener {
                mensaje("ERROR: ${it.message}")
            }

        nombreLN.setText("")
        telLN.setText("")
    }//insertar

    fun alerta(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }//alerta

    fun mensaje(s: String) {
        AlertDialog.Builder(this)
            .setTitle("ATENCION")
            .setMessage(s)
            .setPositiveButton("OK"){d,i->}
            .show()
    }//mensaje
}