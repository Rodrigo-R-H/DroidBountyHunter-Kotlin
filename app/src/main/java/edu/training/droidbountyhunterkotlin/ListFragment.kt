package edu.training.droidbountyhunterkotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import edu.training.droidbountyhunterkotlin.data.DatabaseBountyHunter
import edu.training.droidbountyhunterkotlin.models.Fugitivo
import edu.training.droidbountyhunterkotlin.network.JSONUtils
import edu.training.droidbountyhunterkotlin.network.NetworkServices
import edu.training.droidbountyhunterkotlin.network.OnTaskListener
import kotlinx.coroutines.launch

const val SECTION_NUMBER : String = "section_number"

class ListFragment: Fragment() {
    private val viewModel: FugitivoViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val modo = requireArguments()[SECTION_NUMBER] as Int


        val fugitivosCapturadosList = view.findViewById<ListView>(R.id.listaFugitivosCapturados)


        actualizarDatos(fugitivosCapturadosList, modo)

        fugitivosCapturadosList.setOnItemClickListener { adapterView, view, position, id ->
//            val intent = Intent(context, DetalleActivity::class.java)
//            intent.putExtra("titulo", (view as TextView).text)
//            intent.putExtra("modo", modo)
//
//            startActivity(intent)

            val fugitivoList = fugitivosCapturadosList.tag as Array<Fugitivo>
            viewModel.selectFugitivo(fugitivoList[position])

        }
    }


    private fun actualizarDatos(listView: ListView?, modo: Int){
        val database = DatabaseBountyHunter(requireContext())
        val fugitivos = database.obtenerFugitivos(modo)
        if(fugitivos.isNotEmpty()){
            val values = ArrayList<Fugitivo>()
            fugitivos.mapTo(values){ it }
            val adapterFugitivo = AdapterFugitivos(requireContext(),values)
            listView!!.adapter = adapterFugitivo
            listView.tag = fugitivos
        }
        else {
            if (modo == 0) {
                lifecycleScope.launch{
                    NetworkServices.execute("Fugitivos", object : OnTaskListener {
                        override fun tareaCompletada(respuesta: String) {
                            JSONUtils.parsearFugitivos(respuesta, context!!)
                            actualizarDatos(listView, modo)
                        }
                        override fun tareaConError(codigo: Int, mensaje: String, error: String) {
                            Toast.makeText(requireContext(), "Ocurrio un problema con el WebService!!! --- Código de error: $codigo \nMensaje: $mensaje",
                                Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
        }
    }
}