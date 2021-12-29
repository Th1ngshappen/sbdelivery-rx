package ru.skillbranch.sbdelivery.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import ru.skillbranch.sbdelivery.core.BaseViewModel
import ru.skillbranch.sbdelivery.core.adapter.ProductItemState
import ru.skillbranch.sbdelivery.core.notifier.BasketNotifier
import ru.skillbranch.sbdelivery.core.notifier.event.BasketEvent
import ru.skillbranch.sbdelivery.domain.filter.CategoriesFilter
import ru.skillbranch.sbdelivery.domain.filter.CategoriesFilterUseCase
import ru.skillbranch.sbdelivery.repository.DishesRepositoryContract
import ru.skillbranch.sbdelivery.repository.error.EmptyDishesError
import ru.skillbranch.sbdelivery.repository.mapper.CategoriesMapper
import ru.skillbranch.sbdelivery.repository.mapper.DishesMapper
import ru.skillbranch.sbdelivery.ui.search.SearchState

class MainViewModel(
    private val repository: DishesRepositoryContract,
    private val dishesMapper: DishesMapper,
    private val categoriesMapper: CategoriesMapper,
    private val notifier: BasketNotifier,
    private val useCase: CategoriesFilter
) : BaseViewModel() {

    private val defaultState = MainState.Loader
    private val action = MutableLiveData<MainState>()
    val state: LiveData<MainState>
        get() = action

    init {
        loadDishes()
    }

    fun loadDishes() {
        repository.getDishes()
            .doOnSubscribe { action.value = defaultState }
            .flatMap { if (it.isEmpty()) Single.error(EmptyDishesError("Список пуст")) else Single.just(it)}
            .flatMap { dishes -> repository.getCategories().map { it to dishes } }
            .map { categoriesMapper.mapDtoToState(it.first) to dishesMapper.mapDtoToState(it.second) }
            .subscribe({
                val newState = MainState.Result(it.second, it.first)
                action.value = newState
            }, {
                if (it is EmptyDishesError) {
                    action.value = MainState.Error(it.messageDishes, it)
                } else {
                    action.value = MainState.Error("Что то пошло не по плану", it)
                }
                it.printStackTrace()
            }).track()
    }


    fun setFilterEvent(filterEvent: Single<String>) {
        filterEvent
            .flatMap { categoryId -> useCase.categoryFilterDishes(categoryId) }
            .flatMap { dishes -> repository.getCategories().map { it to dishes } }
            .map { categoriesMapper.mapDtoToState(it.first) to dishesMapper.mapDtoToState(it.second) }
            .subscribe({
                val newState = MainState.Result(it.second, it.first)
                action.value = newState
            }, {
                if (it is EmptyDishesError) {
                    action.value = MainState.Error(it.messageDishes, it)
                } else {
                    action.value = MainState.Error("Что-то пошло не по плану", it)
                }
                it.printStackTrace()
            }).track()
    }

    fun handleAddBasket(item: ProductItemState) {
        notifier.putDishes(BasketEvent.AddDish(item.id, item.title, item.price))
    }
}