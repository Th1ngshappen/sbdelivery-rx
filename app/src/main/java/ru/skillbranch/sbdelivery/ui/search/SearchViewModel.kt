package ru.skillbranch.sbdelivery.ui.search

import android.os.Handler
import androidx.core.os.postDelayed
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.skillbranch.sbdelivery.core.BaseViewModel
import ru.skillbranch.sbdelivery.domain.SearchUseCase
import ru.skillbranch.sbdelivery.repository.error.AnotherError
import ru.skillbranch.sbdelivery.repository.error.EmptyDishesError
import ru.skillbranch.sbdelivery.repository.mapper.DishesMapper
import ru.skillbranch.sbdelivery.ui.main.MainState
import java.util.concurrent.TimeUnit

class SearchViewModel(
    private val useCase: SearchUseCase,
    private val mapper: DishesMapper
) : BaseViewModel() {
    private val action = MutableLiveData<SearchState>()
    val state: LiveData<SearchState>
        get() = action

    fun initState() {
        useCase.getDishes()
            .doOnSubscribe { action.value = SearchState.Loading }
            .map { dishes -> mapper.mapDtoToState(dishes) }
            .subscribe({
                val newState = SearchState.Result(it)
                action.value = newState
            }, {
                val newState = if (it is EmptyDishesError) SearchState.Error(it.messageDishes)
                else SearchState.Error("")
                action.value = newState
                it.printStackTrace()
            }).track()
    }

    fun setSearchEvent(searchEvent: Observable<String>) {
        searchEvent
            .debounce(800L, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .switchMap { useCase.findDishesByName(it) }
            .flatMap {
                if (it.isEmpty()) Observable.error(
                    EmptyDishesError("Данные не найдены")
                ) else Observable.just(it)
            }
            .map { mapper.mapDtoToState(it) }
            .delay(800L, TimeUnit.MILLISECONDS) // delay use computation scheduler
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { action.value = SearchState.Loading }
            .subscribe({
                val newState = SearchState.Result(it)
                action.value = newState
            }, {
                if (it is EmptyDishesError) {
                    action.value = SearchState.Error(it.messageDishes)
                    setSearchEvent(searchEvent)
                } else {
                    action.value = SearchState.Error("to pass tests")
                }
                it.printStackTrace()
            }).track()

    }

}