package edu.training.droidbountyhunterkotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.fragment.app.Fragment

class AcercaDeFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_acerca_de, container,false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var rating = "0.0"

        if (System.getProperty("rating") != null){
            rating = System.getProperty("rating")
        }
        if (rating.isEmpty()){
            rating = "0.0"
        }
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        ratingBar.rating = rating.toFloat()
        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            System.setProperty("rating", rating.toString())
            ratingBar.rating = rating
        }
    }
}