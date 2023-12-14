package com.bluebellcspl.maarevacommoditytradingapp.model

data class ExpandableObject(
    var Expandable: Boolean = false,
){
    fun isExpandable(): Boolean {
        return Expandable
    }
}

