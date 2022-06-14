package de.fhac.newsflash

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.fhac.newsflash.databinding.BottomSheetBinding

class NewsBottomSheetCallback(
    private val binding: BottomSheetBinding,
    private val mainActivity: MainActivity
) :
    BottomSheetBehavior.BottomSheetCallback() {

    val Number.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )

    private var lastBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                setWebContentVisible()
                binding.btResizeNews.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                setWebContentInvisible()
                binding.btResizeNews.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                mainActivity.setBackgroundBlurred(1f)
            }
            BottomSheetBehavior.STATE_DRAGGING -> binding.apply {
                previewGroup.visibility = View.VISIBLE
                webViewGroup.visibility = View.VISIBLE
                when (lastBottomSheetState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> webViewGroup.alpha = 0f
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        previewGroup.alpha = 1f
                    }
                }
            }
            BottomSheetBehavior.STATE_HIDDEN -> {
                mainActivity.resetCurrentNews()
            }
        }
        lastBottomSheetState = newState
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        if (slideOffset > 0) {
            binding.apply {
                webViewGroup.alpha = slideOffset
                previewGroup.alpha = 1f - slideOffset
                if (slideOffset > 0.5) {
                    var drawable = mainConstraintLayout.background as GradientDrawable;
                    drawable.cornerRadius = (2 * (1 - slideOffset) * 30).dp;
                }
            }
        } else {
            setWebContentInvisible()
            mainActivity.setBackgroundBlurred(1f + slideOffset)
        }
    }


    private fun setWebContentInvisible() {
        binding.apply {
            previewGroup.visibility = View.VISIBLE
            webViewGroup.visibility = View.GONE
            (mainConstraintLayout.background as GradientDrawable).cornerRadius = 30.dp
        }
    }

    private fun setWebContentVisible() {
        binding.apply {
            previewGroup.visibility = View.GONE
            webViewGroup.visibility = View.VISIBLE
            (mainConstraintLayout.background as GradientDrawable).cornerRadius = 0.dp
        }
    }
}