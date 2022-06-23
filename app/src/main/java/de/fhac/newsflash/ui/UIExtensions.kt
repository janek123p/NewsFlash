package de.fhac.newsflash.ui

import android.view.View
import android.view.animation.AnimationUtils
import de.fhac.newsflash.R

class UIExtensions {
    companion object{
        fun View.setOnClickListenerWithAnimation(listener : View.OnClickListener?){
            this.setOnClickListener{
                this.startAnimation(AnimationUtils.loadAnimation(context, R.anim.button_bounce))
                listener?.onClick(this)
            }
        }
    }
}