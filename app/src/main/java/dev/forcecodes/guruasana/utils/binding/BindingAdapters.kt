package dev.forcecodes.guruasana.utils.binding

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import coil.decode.SvgDecoder
import coil.load

@BindingAdapter(value = ["imageDrawable", "imageUri"], requireAll = false)
fun ImageView.setImage(@DrawableRes drawable: Int?, uri: String?) {
    drawable?.let(::load)

    uri?.let {
        load(it) {
            decoderFactory { result, options, _ ->
                SvgDecoder(result.source, options)
            }
        }
    }
}