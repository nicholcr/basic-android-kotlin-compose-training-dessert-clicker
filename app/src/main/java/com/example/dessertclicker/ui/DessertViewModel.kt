package com.example.dessertclicker.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.dessertclicker.R
import com.example.dessertclicker.data.Datasource.dessertList
import com.example.dessertclicker.model.Dessert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DessertViewModel : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow(DessertUiState())
    val uiState: StateFlow<DessertUiState> = _uiState.asStateFlow()

    private var currentDessert: Dessert = dessertList.first()
    private var currentDessertPrice = currentDessert.price
    private var currentDessertImageId = currentDessert.imageId

    private fun updateDessertState() {
        _uiState.update { currentState ->
            currentState.copy(
                revenue = currentState.revenue + currentDessertPrice,
                dessertsSold = currentState.dessertsSold.inc(),
                currentDessertImageId = determineDessertToShow(currentState.dessertsSold).imageId
            )
        }
    }

    /**
     * Determine which dessert to show.
     */
    private fun determineDessertToShow(
        dessertsSold: Int
    ): Dessert {
        var dessertToShow = dessertList.first()
        for (dessert in dessertList) {
            if (dessertsSold >= dessert.startProductionAmount) {
                dessertToShow = dessert
            } else {
                // The list of desserts is sorted by startProductionAmount. As you sell more desserts,
                // you'll start producing more expensive desserts as determined by startProductionAmount
                // We know to break as soon as we see a dessert who's "startProductionAmount" is greater
                // than the amount sold.
                break
            }
        }

        return dessertToShow
    }

    fun dessertClicked() {
        // Update the revenue
        updateDessertState()

        // Show the next dessert
        currentDessert = determineDessertToShow(uiState.value.dessertsSold)
        currentDessertImageId = currentDessert.imageId
        currentDessertPrice = currentDessert.price
    }

    /**
     * Share desserts sold information using ACTION_SEND intent
     */
    fun shareSoldDessertsInformation(intentContext: Context, dessertsSold: Int, revenue: Int) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                intentContext.getString(R.string.share_text, dessertsSold, revenue)
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)

        try {
            ContextCompat.startActivity(intentContext, shareIntent, null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                intentContext,
                intentContext.getString(R.string.sharing_not_available),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun startDessertClicker() {
        _uiState.value = DessertUiState(
            currentDessertImageId = currentDessert.imageId,
            revenue = 0,
            dessertsSold = 0
        )
    }

    init {
        startDessertClicker()
    }
}