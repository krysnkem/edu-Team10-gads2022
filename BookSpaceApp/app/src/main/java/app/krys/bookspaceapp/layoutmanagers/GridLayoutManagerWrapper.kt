package app.krys.bookspaceapp.layoutmanagers

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager

class GridLayoutManagerWrapper(context: Context, attrs: AttributeSet, defStyleAttr:Int,
                               defStyleRes: Int): GridLayoutManager(context, attrs, defStyleAttr, defStyleRes) {
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}