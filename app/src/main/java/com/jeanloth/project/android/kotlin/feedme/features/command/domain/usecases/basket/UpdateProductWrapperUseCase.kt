package com.jeanloth.project.android.kotlin.feedme.features.command.domain.usecases.basket

import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.Wrapper
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.WrapperItem
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.models.product.Product
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.repository.BaseRepository
import com.jeanloth.project.android.kotlin.feedme.features.command.domain.repository.ProductWrapperRepository

class UpdateProductWrapperUseCase (
    private val repository: ProductWrapperRepository
) {
    operator fun invoke(wrappers: List<Wrapper<Product>>?,isAssociatedToCommand: Boolean) = wrappers?.let { repository.update(wrappers, isAssociatedToCommand) }
}