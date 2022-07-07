package com.intellisoft.nndak.viewmodels

import android.app.Application
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.intellisoft.nndak.R


class LayoutListViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {

    fun getLayoutList(): List<Layout> {
        return Layout.values().toList()
    }

    enum class Layout(
        @DrawableRes val iconId: Int,
        @StringRes val textId: Int,
    ) {
        DEFAULT(R.drawable.prof, R.string.home_babies),
        PAGINATED(R.drawable.edit, R.string.app_registration),
        REVIEW(R.drawable.bre, R.string.app_human_milk),
        READ_ONLY(R.drawable.monitor, R.string.app_statistics),
    }
}
