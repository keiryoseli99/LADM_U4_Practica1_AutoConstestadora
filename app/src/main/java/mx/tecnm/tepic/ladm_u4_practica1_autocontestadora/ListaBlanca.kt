package mx.tecnm.tepic.ladm_u4_practica1_autocontestadora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_lista_blanca.*

class ListaBlanca : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()
    var dataListaBlanca = ArrayList<String>()
    var listaID = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_blanca)

        baseRemota.collection("listaBlanca")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    mensaje(error.message!!)
                    return@addSnapshotListener
                }
                dataListaBlanca.clear()
                listaID.clear()
                for (document in querySnapshot!!){
                    val cadena = "NOMBRE: ${document.getString("nombre")} \nTEL: ${document.get("telefono")}"
                    dataListaBlanca.add(cadena)
                    listaID.add(document.id)
                }//for
                listaBlanca.adapter = ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, dataListaBlanca)
                listaBlanca.setOnItemClickListener { adapterView, view, posicion, l ->
                    eliminarActualizar(posicion)
                }
            }//collection

        buttonLB.setOnClickListener {
            insertar()
        }
    }

    fun eliminarActualizar(posicion: Int) {
        val id = listaID.get(posicion)

        AlertDialog.Builder(this)
            .setTitle("ATENCION")
            .setMessage("¿QUE DESEA HACER CON\n${dataListaBlanca.get(posicion)}?")
            .setPositiveButton("ACTUALIZAR"){d,i-> leerDatos(id)}
            .setNeutralButton("CANCELAR"){d,i-> d.cancel()}
            .setNegativeButton("ELIMINAR"){d,i -> eliminar(id)}
            .show()
    }

    fun leerDatos(id: String) {
        val docRef = baseRemota.collection("listaBlanca").document(id)
        docRef.get()
            .addOnSuccessListener { document ->
                val nombre = document.getString("nombre")
                val tel = document.getString("telefono")
                nombreLB.setText(nombre)
                telLB.setText(tel)
            }
        actualizarLB.setOnClickListener {
            actualizar(id)
        }
    }
    fun actualizar(id: String) {
        baseRemota.collection("listaBlanca")
            .document(id)
            .update(
                mapOf(
                    "nombre" to nombreLB.text.toString(),
                    "telefono" to telLB.text.toString()
                )
            )
            .addOnSuccessListener {
                alerta("SE ACTUALIZO CONTACTO CON EXITO")
            }
            .addOnFailureListener {
                mensaje("ERROR: ${it.message!!}")
            }
        nombreLB.setText("")
        telLB.setText("")
    }

    fun eliminar(id: String) {
        baseRemota.collection("listaBlanca")
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
        val datosContacto = hashMapOf(
            "nombre" to nombreLB.text.toString(),
            "telefono" to telLB.text.toString()
        )

        baseRemota.collection("listaBlanca")
            .add(datosContacto as Any)
            .addOnSuccessListener {
                alerta("SE AGREGO CONTACTO EN LA NUBE")
            }
            .addOnFailureListener {
                mensaje("ERROR: ${it.message}")
            }

        nombreLB.setText("")
        telLB.setText("")
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
