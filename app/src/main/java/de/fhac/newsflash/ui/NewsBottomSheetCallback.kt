package de.fhac.newsflash.ui

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.fhac.newsflash.R
import de.fhac.newsflash.databinding.BottomSheetBinding
import de.fhac.newsflash.ui.activities.MainActivity

/**
 * Callback for News Bottom sheet
 */
class NewsBottomSheetCallback(
    private val binding: BottomSheetBinding,
    private val mainActivity: MainActivity
) :
    BottomSheetBehavior.BottomSheetCallback() {

    private var lastBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

    /**
     * Methode to determine actions, when bottom sheet state has changed
     */
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
                    BottomSheetBehavior.STATE_EXPANDED -> previewGroup.alpha = 1f
                }
            }
            BottomSheetBehavior.STATE_HIDDEN -> {
                mainActivity.resetCurrentNews()
            }
        }
        lastBottomSheetState = newState
    }

    /**
     * Method to determine behavior during bottom sheet slide
     */
    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        if (slideOffset > 0) {
            binding.apply {
                // Fading animation between WebView and preview group
                webViewGroup.alpha = slideOffset
                previewGroup.alpha = 1f - slideOffset
                if (slideOffset > 0.5) {
                    // Set corner radius and shadow size dependent on slideOffset
                    var drawable = mainConstraintLayout.background as GradientDrawable
                    drawable.cornerRadius =
                        (2 * (1 - slideOffset) * mainActivity.resources.getDimension(R.dimen.cornerRadiusBig))
                    binding.imgViewShadow.layoutParams.height =
                        (2 * (1 - slideOffset) * mainActivity.resources.getDimension(R.dimen.shadowViewHeight)).toInt()
                    setMarginTop(
                        binding.mainConstraintLayout,
                        (2 * (1 - slideOffset) * mainActivity.resources.getDimension(R.dimen.shadowMargin)).toInt()
                    )
                }
            }
        } else {
            setWebContentInvisible()
            // Add background blur animation (Android 12 and above)
            mainActivity.setBackgroundBlurred(1f + slideOffset)
        }
    }

    /**
     * Sets top margin of a constraint layout
     */
    private fun setMarginTop(layout: ConstraintLayout, margin: Int) {
        var layoutParams = layout.layoutParams as FrameLayout.LayoutParams
        layoutParams.topMargin = margin
        layout.layoutParams = layoutParams
    }


    /**
     * Sets web content visibility to gone and visibility of preview group to visible
     */
    private fun setWebContentInvisible() {
        binding.apply {
            previewGroup.visibility = View.VISIBLE
            webViewGroup.visibility = View.GONE
            btRefresh.visibility = View.GONE
        }
    }

    /**
     * Set visibility of preview group to visible and web content gone
     */
    private fun setWebContentVisible() {
        binding.apply {
            previewGroup.visibility = View.GONE
            webViewGroup.visibility = View.VISIBLE
            btRefresh.visibility = View.VISIBLE
        }
    }
}