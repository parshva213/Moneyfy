package com.moneyfy.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moneyfy.MoneyfyApplication
import com.moneyfy.data.Category
import com.moneyfy.data.CategoryType
import com.moneyfy.firebase.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val categoryRepository: CategoryRepository =
        (application as MoneyfyApplication).categoryRepository

    val allCategories: Flow<List<Category>> = categoryRepository.getAllCategories()

    val incomeCategories = categoryRepository.getCategoriesByType(CategoryType.INCOME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories = categoryRepository.getCategoriesByType(CategoryType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryRepository.insertCategory(category)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryRepository.updateCategory(category)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(category)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
