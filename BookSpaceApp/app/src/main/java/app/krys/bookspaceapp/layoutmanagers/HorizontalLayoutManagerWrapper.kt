package app.krys.bookspaceapp.layoutmanagers

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager

class HorizontalLayoutManagerWrapper(
    context: Context, attrs: AttributeSet, defStyleAttr: Int,
    defStyleRes: Int
) : LinearLayoutManager(context, attrs, defStyleAttr, defStyleRes) {
    init {
            orientation = LinearLayoutManager.HORIZONTAL
    }

}