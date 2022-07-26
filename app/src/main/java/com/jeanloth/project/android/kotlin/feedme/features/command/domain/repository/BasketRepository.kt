package com.jeanloth.project.android.kotlin.feedme.features.command.domain.repository


import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.Basket
import kotlinx.coroutines.flow.Flow

interface BasketRepository {

    fun save(basket : Basket) : Long

    fun saveCommandBasket(basket : Basket) : Long

    fun remove(basket : Basket)

    // Associated to commands
    fun observeBaskets() : Flow<List<Basket>>

    fun observeBaskets(comandId : Long) : Flow<List<Basket>>



}