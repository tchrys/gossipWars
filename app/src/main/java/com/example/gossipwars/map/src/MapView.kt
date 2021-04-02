package com.example.gossipwars.map.src

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.example.gossipwars.InGameActivity
import com.example.gossipwars.R
import com.example.gossipwars.ui.map.MapFragment
import com.richpath.RichPath
import com.richpath.RichPathView
import com.richpathanimator.RichPathAnimator
import devit951.github.magictip.tip.AutoCloseMagicTip
import kotlinx.android.synthetic.main.activity_in_game.*
import java.lang.Exception

class MapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var provinceBackgroundColor = Color.BLACK
    private var provinceActiveColor = Color.CYAN
    private var provinceStrokeColor = Color.WHITE

    // make provinces clickable
    private var provinceSelectByClick = true
        set(value) {
            field = value
            if (value)
                gameMapPath.setOnPathClickListener {
                    activeProvince(it, withAnimate = true)
                } else {
                gameMapPath.setOnPathClickListener(null)
            }
        }

    var provinceCanMultiSelect = false
    var mapAnimationDuration = 200L
    var mapAppearWithAnimation = false
    var mapAdjustViewBound = false
    private val paint = Paint()
    private lateinit var surfaceView: SurfaceView
    private lateinit var gameMapPath: RichPathView
    var provinceClicked: MutableLiveData<String> = MutableLiveData()

    // give list of selected provinces
    var selectedProvinces: MutableSet<Province> = mutableSetOf()
    private val titles: MutableMap<RichPath?, String?> = mutableMapOf()

    init {
        addMapPathView()
        addSurfaceView()
        handleAttr(context, attrs)
    }

    private fun addMapPathView() {
        // show a preview of map in edit mode
        if (isInEditMode) {
            val imageView = ImageView(context)
            imageView.layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            imageView.setImageResource(R.drawable.map)
            imageView.adjustViewBounds = mapAdjustViewBound
            addView(imageView)
            return
        }
        // add rich path view for show map svg
        gameMapPath = RichPathView(context)
        gameMapPath.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        gameMapPath.adjustViewBounds = mapAdjustViewBound
        gameMapPath.setVectorDrawable(R.drawable.map)
        addView(gameMapPath)
    }

    private fun addSurfaceView() {
        // add surface view for draw titles on map
        surfaceView = SurfaceView(context)
        surfaceView.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        addView(surfaceView)
    }

    private fun handleAttr(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MapView, 0, 0)
        try {
            provinceBackgroundColor =
                typedArray.getColor(R.styleable.MapView_imProvinceBackgroundColor, Color.BLACK)
            provinceActiveColor =
                typedArray.getColor(R.styleable.MapView_imProvinceActiveBackgroundColor, Color.CYAN)
            provinceStrokeColor =
                typedArray.getColor(R.styleable.MapView_imProvinceStrokeColor, Color.WHITE)
            provinceSelectByClick =
                typedArray.getBoolean(R.styleable.MapView_imProvinceSelectByClick, true)
            provinceCanMultiSelect =
                typedArray.getBoolean(R.styleable.MapView_imProvinceMultiSelect, false)
            mapAnimationDuration =
                typedArray.getInt(R.styleable.MapView_imAnimationDuration, 200).toLong()
            mapAppearWithAnimation =
                typedArray.getBoolean(R.styleable.MapView_imMapAppearWithAnimation, false)
            mapAdjustViewBound = typedArray.getBoolean(R.styleable.MapView_imAdjustViewBound, false)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initMap()
    }

    private fun initMap() {
        // apply default attrs to all provinces
        gameMapPath.findAllRichPaths().forEach {
            it.fillColor = provinceBackgroundColor
            it.strokeColor = provinceStrokeColor
            //Ready provinces for play animation
            if (mapAppearWithAnimation) {
                it.scaleX = 5f
                it.scaleY = 5f
                it.fillAlpha = 0f
                it.strokeAlpha = 0f
            }
        }
        // appear provinces with scale, alpha animation
        if (mapAppearWithAnimation) {
            Province.values().toMutableList().shuffled().forEachIndexed { index, province ->
                RichPathAnimator.animate(gameMapPath.findRichPathByName(province.name))
                    .startDelay(25.times(index).toLong())
                    .interpolator(DecelerateInterpolator())
                    .scale(1f)
                    .fillAlpha(1f)
                    .strokeAlpha(1f)
                    .duration(mapAnimationDuration)
                    .start()
            }
        }
    }

    /**
     * َActivate a province by given properties
     * @param [province] enum of map provinces
     * @param [withBackgroundColor] background color of province in active mode
     * @param [withStrokeColor] stroke color of province in active mode
     * @param [withAnimate] activate with animation
     * @throws EnumConstantNotPresentException if province not found
     */
    @JvmOverloads
    fun activeProvince(
        province: Province, withBackgroundColor: Int? = null, withStrokeColor: Int? = null,
        withAnimate: Boolean = false
    ) {
        val provincePath = gameMapPath.findRichPathByName(province.name)
        activeProvince(provincePath, withBackgroundColor, withStrokeColor, withAnimate)
    }

    /**
     * Deactivate given province
     * @param [province] enum of map provinces.
     * @param [withAnimate] activate with animation
     * @throws EnumConstantNotPresentException if province not found
     */
    @JvmOverloads
    fun deActiveProvince(province: Province, withAnimate: Boolean = false) {
        val provincePath = gameMapPath.findRichPathByName(province.name)
        deActivateProvince(provincePath, withAnimate)
    }

    private fun activeProvince(
        provincePath: RichPath?, withBackgroundColor: Int? = null,
        withStrokeColor: Int? = null, withAnimate: Boolean = false
    ) {
        // deactivate selected provinces in single select mode
        if (!provinceCanMultiSelect) {
            Province.values().filter { it.name != provincePath?.name }
                .forEach { deActiveProvince(it) }
        }
        provincePath?.let {
            // if province is active now, deactivate it
//            if (selectedProvinces.contains(Province.valueOf(it.name))) {
//                deActivateProvince(it, withAnimate)
//            } else {
            // activate province
            provinceClicked.value = provincePath.name
            RichPathAnimator.animate(it)
                .interpolator(AccelerateDecelerateInterpolator())
                .duration(if (withAnimate) mapAnimationDuration else 0)
                .scale(1.1f, 1f)
//                    .fillColor(it.fillColor, withBackgroundColor ?: provinceActiveColor)
                .strokeColor(it.strokeColor, withStrokeColor ?: provinceStrokeColor)
                .start()
            selectedProvinces.add(Province.valueOf(it.name))
//            }
        } ?: kotlin.run {
            throw EnumConstantNotPresentException(Province::class.java, "Province not found")
        }
    }

    fun colorProvince(province: Province, color: Int) {
        val provincePath = gameMapPath.findRichPathByName(province.name)
        provincePath?.let {

            it.fillColor = color
//            it.strokeColor = color
//            RichPathAnimator.animate(it)
//                .interpolator(AccelerateDecelerateInterpolator())
//                .fillColor(it.fillColor, color)
//                .strokeColor(it.strokeColor, color)
//                .start()
        }
    }

    private fun deActivateProvince(provincePath: RichPath?, withAnimate: Boolean = false) {
        provincePath?.let {
            RichPathAnimator.animate(it)
                .interpolator(AccelerateDecelerateInterpolator())
                .duration(if (withAnimate) mapAnimationDuration else 0)
                .scale(1.1f, 1f)
//                .fillColor(provinceBackgroundColor)
                .strokeColor(provinceStrokeColor)
                .start()
            selectedProvinces.remove(Province.valueOf(it.name))
        } ?: kotlin.run {
            throw EnumConstantNotPresentException(Province::class.java, "Province not found")
        }
    }

    /**
     * Add a title to a province by given properties
     * @param [province] enum of map provinces.
     * @param [title] text you want to add to province
     * @param [typeface] type face of title
     * @param [textColor] text color of title
     */
    fun addTitle(
        province: Province,
        title: String?,
        typeface: Typeface = Typeface.DEFAULT,
        textColor: Int? = null
    ) {
        if (title == null)
            return
        // init paint for draw title
        paint.color = textColor ?: Color.BLACK
        paint.textSize = 30f
        paint.typeface = typeface
        val provincePath = gameMapPath.findRichPathByName(province.name)
        // add title to title list if not exist
        if (!titles.contains(provincePath))
            titles[provincePath] = title
        // refresh view to draw new titles
        surfaceView.invalidate()
    }

    /**
     * Remove title from a province
     * @param [province] enum of map provinces.
     */

    fun removeTitle(province: Province) {
        val provincePath = gameMapPath.findRichPathByName(province.name)
        titles.remove(provincePath)
        surfaceView.invalidate()
    }

    inner class SurfaceView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {
        init {
            // force layout to call onDraw method
            setWillNotDraw(false)
        }

        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            titles.forEach {
                val provinceBounds = RectF()
                val textBounds = Rect()
                // find title bounds
                paint.getTextBounds(it.value ?: "", 0, it.value?.length ?: 0, textBounds)
                // find province bounds
                it.key?.computeBounds(provinceBounds, true)
                // draw text on center of province
                canvas?.drawText(
                    it.value ?: "",
                    provinceBounds.centerX().minus(textBounds.width().div(2)),
                    provinceBounds.centerY(), paint
                )
            }
        }
    }


}