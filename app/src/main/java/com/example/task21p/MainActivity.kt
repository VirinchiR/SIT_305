package com.example.task21p

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.task21p.ui.theme.Task21PTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Task21PTheme {
                AppScreen()
            }
        }
    }
}

@Composable
fun AppScreen() {

    // currency list
    val currencyList = listOf("USD","AUD","EUR","JPY","GBP")

    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("AUD") }

    var openFrom by remember { mutableStateOf(false) }
    var openTo by remember { mutableStateOf(false) }

    // fuel list
    val fuelList = listOf("MPG","KM/L","Gallons","Liters")

    var fuelFrom by remember { mutableStateOf("MPG") }
    var fuelTo by remember { mutableStateOf("KM/L") }

    var openFuelFrom by remember { mutableStateOf(false) }
    var openFuelTo by remember { mutableStateOf(false) }

    // temperature list
    val tempList = listOf("Celsius","Fahrenheit","Kelvin")

    var tempFrom by remember { mutableStateOf("Celsius") }
    var tempTo by remember { mutableStateOf("Fahrenheit") }

    var openTempFrom by remember { mutableStateOf(false) }
    var openTempTo by remember { mutableStateOf(false) }

    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text("Travel Companion Converter")

        Spacer(modifier = Modifier.height(20.dp))

        // Currency section
        Text("Currency")

        Box {
            Button(onClick = { openFrom = true }) {
                Text("From: $fromCurrency")
            }

            DropdownMenu(
                expanded = openFrom,
                onDismissRequest = { openFrom = false }
            ) {
                currencyList.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            fromCurrency = it
                            openFrom = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box {
            Button(onClick = { openTo = true }) {
                Text("To: $toCurrency")
            }

            DropdownMenu(
                expanded = openTo,
                onDismissRequest = { openTo = false }
            ) {
                currencyList.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            toCurrency = it
                            openTo = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Fuel section
        Text("Fuel")

        Box {
            Button(onClick = { openFuelFrom = true }) {
                Text("From: $fuelFrom")
            }

            DropdownMenu(
                expanded = openFuelFrom,
                onDismissRequest = { openFuelFrom = false }
            ) {
                fuelList.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            fuelFrom = it
                            openFuelFrom = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box {
            Button(onClick = { openFuelTo = true }) {
                Text("To: $fuelTo")
            }

            DropdownMenu(
                expanded = openFuelTo,
                onDismissRequest = { openFuelTo = false }
            ) {
                fuelList.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            fuelTo = it
                            openFuelTo = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Temperature section
        Text("Temperature")

        Box {
            Button(onClick = { openTempFrom = true }) {
                Text("From: $tempFrom")
            }

            DropdownMenu(
                expanded = openTempFrom,
                onDismissRequest = { openTempFrom = false }
            ) {
                tempList.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            tempFrom = it
                            openTempFrom = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box {
            Button(onClick = { openTempTo = true }) {
                Text("To: $tempTo")
            }

            DropdownMenu(
                expanded = openTempTo,
                onDismissRequest = { openTempTo = false }
            ) {
                tempList.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            tempTo = it
                            openTempTo = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // input
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Enter value") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {

            if(input.isEmpty()){
                result = "Enter a number"
            }
            else{

                val value = input.toDouble()

                // currency conversion
                var usd = value

                if(fromCurrency == "AUD") usd = value / 1.55
                if(fromCurrency == "EUR") usd = value / 0.92
                if(fromCurrency == "JPY") usd = value / 148.50
                if(fromCurrency == "GBP") usd = value / 0.78

                var currencyResult = usd

                if(toCurrency == "AUD") currencyResult = usd * 1.55
                if(toCurrency == "EUR") currencyResult = usd * 0.92
                if(toCurrency == "JPY") currencyResult = usd * 148.50
                if(toCurrency == "GBP") currencyResult = usd * 0.78

                // fuel conversion
                var fuelResult = value

                if(fuelFrom == "MPG" && fuelTo == "KM/L")
                    fuelResult = value * 0.425

                if(fuelFrom == "KM/L" && fuelTo == "MPG")
                    fuelResult = value / 0.425

                if(fuelFrom == "Gallons" && fuelTo == "Liters")
                    fuelResult = value * 3.785

                if(fuelFrom == "Liters" && fuelTo == "Gallons")
                    fuelResult = value / 3.785

                if(fuelFrom == fuelTo)
                    fuelResult = value

                // temperature conversion
                var celsius = value

                if(tempFrom == "Fahrenheit")
                    celsius = (value - 32) / 1.8

                if(tempFrom == "Kelvin")
                    celsius = value - 273.15

                var tempResult = celsius

                if(tempTo == "Fahrenheit")
                    tempResult = (celsius * 1.8) + 32

                if(tempTo == "Kelvin")
                    tempResult = celsius + 273.15

                if(tempTo == "Celsius")
                    tempResult = celsius

                result =
                    "Currency: $currencyResult $toCurrency\n" +
                            "Fuel: $fuelResult $fuelTo\n" +
                            "Temperature: $tempResult $tempTo"
            }

        }) {
            Text("Convert")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Result:")
        Text(result)

    }
}