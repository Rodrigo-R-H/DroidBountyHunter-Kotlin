package edu.training.droidbountyhunterkotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import edu.training.droidbountyhunterkotlin.models.Fugitivo
import edu.training.droidbountyhunterkotlin.utils.PictureTools

class AdapterFugitivos(val context : Context, val listItem: ArrayList<Fugitivo>) : BaseAdapter() {
    override fun getCount(): Int {
        return listItem.size
    }

    override fun getItem(p0: Int): Any {
        return listItem[p0]
    }

    override fun getItemId(p0: Int): Long {
        return listItem[p0].id.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        var myView = p1
        val fugitivo = getItem(p0) as Fugitivo
        if (myView == null){
            val inflator : LayoutInflater = LayoutInflater.from(context)
            myView = inflator.inflate(R.layout.item_fugitivo_list, null)
        }

        val bitmap = fugitivo!!.photo?.let {
            PictureTools.decodeSampledBitmapFromUri(
                it,
                200,200)
        }

        val imgFotoFugitivo = myView!!.findViewById<ImageView>(R.id.imgFotoFugitivo)
        if(bitmap != null){
            imgFotoFugitivo.setImageBitmap(bitmap)
        }


        val txtVwNombreFugitivo = myView!!.findViewById<TextView>(R.id.txtVwNombreFugitivo)
        txtVwNombreFugitivo.text = fugitivo.name

        val txtVwFecha = myView!!.findViewById<TextView>(R.id.txtFecha)
        txtVwFecha.text = fugitivo.captureDate

        return myView
    }

}