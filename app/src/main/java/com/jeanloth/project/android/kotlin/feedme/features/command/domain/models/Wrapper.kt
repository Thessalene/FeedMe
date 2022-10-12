package com.jeanloth.project.android.kotlin.feedme.features.command.domain.models

data class Wrapper<T : WrapperItem>(
    var id: Long = 0L,
    val item : T,
    var basketId : Long = 0L,
    var quantity : Int,
    val status : ProductWrapperStatus = ProductWrapperStatus.TO_DO
){
    val totalPrice = quantity * item.unitPrice

    override fun equals(other: Any?): Boolean {
        return other is Wrapper<*> && this.item == other.item && this.quantity == other.quantity && this.status == other.status
    }

    override fun toString(): String {
        return "Wrapper : [id : $id, basketId: $basketId, item: $item, quantity: $quantity, status: $status]"
    }
}

interface WrapperItem {
    val unitPrice: Float
}

interface WrapperItemEntity

enum class ProductWrapperStatus {
    TO_DO,
    IN_PROGRESS,
    DONE,
    CANCELED,
}