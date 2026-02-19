package com.example.expensetracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.data.model.ExchangeRateResponse
import com.example.expensetracker.data.model.SymbolsResponse
import com.example.expensetracker.ui.viewmodel.ExchangeRateViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

// DESIGN 2: Tab-Based Layout
// DESIGN 2: Tab-Based Layout
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRateScreenDesign2(viewModel: ExchangeRateViewModel = viewModel()) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("EUR") }
    var amount by remember { mutableStateOf("100.0") }
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(7)) }
    var showDatePicker by remember { mutableStateOf(false) }

    val latestRates by viewModel.latestRates.collectAsState()
    val symbols by viewModel.symbols.collectAsState()
    val conversion by viewModel.conversion.collectAsState()
    val historicalRates by viewModel.historicalRates.collectAsState()

    val onShowDatePicker = { showDatePicker = true }

    // Get currency codes for dropdowns
    val currencyCodes = symbols?.getOrNull()?.symbols?.keys?.sorted() ?: listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD")

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = selectedDateMillis
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH) + 1
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        startDate = LocalDate.of(year, month, day)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val tabs = listOf("Latest", "Symbols", "Convert", "History")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exchange Rates (EUR Base)") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> LatestRatesTab(latestRates, viewModel::fetchLatestRates, currencyCodes)
                1 -> SymbolsTab(symbols, viewModel::fetchSymbols, currencyCodes)
                2 -> ConversionTab(
                    fromCurrency, toCurrency, amount,
                    { fromCurrency = it }, { toCurrency = it }, { amount = it },
                    conversion, viewModel::convertCurrency,
                    currencyCodes
                )
                3 -> HistoricalRatesTab(startDate, { startDate = it }, historicalRates, viewModel::fetchHistoricalRates, onShowDatePicker, currencyCodes)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LatestRatesTab(
    latestRates: Result<ExchangeRateResponse>?,
    onFetch: () -> Unit,
    currencyCodes: List<String>
) {
    var selectedCurrency by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        // Fetch button
        Button(onClick = onFetch, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Get Latest Rates")
        }

        Spacer(modifier = Modifier.height(16.dp))

        latestRates?.let { result ->
            when {
                result.isSuccess -> {
                    val data = result.getOrNull()
                    if (data != null) {
                        // Currency filter dropdown
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedCurrency ?: "All currencies",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filter currency") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All currencies") },
                                    onClick = {
                                        selectedCurrency = null
                                        expanded = false
                                    }
                                )
                                currencyCodes.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text(currency) },
                                        onClick = {
                                            selectedCurrency = currency
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Base: ${data.base} (${data.date})", style = MaterialTheme.typography.titleMedium)

                        Spacer(modifier = Modifier.height(8.dp))

                        val filteredRates = if (selectedCurrency != null) {
                            data.rates.filterKeys { it == selectedCurrency }
                        } else {
                            data.rates
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredRates.entries.toList()) { (currency, rate) ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(currency, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                                        Text("%.4f".format(rate), style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    }
                }
                result.isFailure -> Text("Error: ${result.exceptionOrNull()?.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SymbolsTab(
    symbols: Result<SymbolsResponse>?,
    onFetch: () -> Unit,
    currencyCodes: List<String>
) {
    var selectedCurrency by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        // Fetch button
        Button(onClick = onFetch, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Get Currency Symbols")
        }

        Spacer(modifier = Modifier.height(16.dp))

        symbols?.let { result ->
            when {
                result.isSuccess -> {
                    val data = result.getOrNull()
                    if (data != null) {
                        // Currency filter dropdown
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedCurrency ?: "All currencies",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filter currency") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All currencies") },
                                    onClick = {
                                        selectedCurrency = null
                                        expanded = false
                                    }
                                )
                                currencyCodes.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text(currency) },
                                        onClick = {
                                            selectedCurrency = currency
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val filteredSymbols = if (selectedCurrency != null) {
                            data.symbols.filterKeys { it == selectedCurrency }
                        } else {
                            data.symbols
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredSymbols.entries.toList()) { (code, name) ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(code, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                        Text(name, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
                result.isFailure -> Text("Error: ${result.exceptionOrNull()?.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversionTab(
    fromCurrency: String, toCurrency: String, amount: String,
    onFromChange: (String) -> Unit, onToChange: (String) -> Unit, onAmountChange: (String) -> Unit,
    conversion: Result<Double>?, onConvert: (String, String, Double) -> Unit,
    currencyCodes: List<String>
) {
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Currency Converter", style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // From currency dropdown
            ExposedDropdownMenuBox(
                expanded = fromExpanded,
                onExpandedChange = { fromExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = fromCurrency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("From") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = fromExpanded,
                    onDismissRequest = { fromExpanded = false }
                ) {
                    currencyCodes.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency) },
                            onClick = {
                                onFromChange(currency)
                                fromExpanded = false
                            }
                        )
                    }
                }
            }

            // To currency dropdown
            ExposedDropdownMenuBox(
                expanded = toExpanded,
                onExpandedChange = { toExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = toCurrency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("To") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = toExpanded,
                    onDismissRequest = { toExpanded = false }
                ) {
                    currencyCodes.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency) },
                            onClick = {
                                onToChange(currency)
                                toExpanded = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(onClick = {
            val amt = amount.toDoubleOrNull() ?: 0.0
            onConvert(fromCurrency, toCurrency, amt)
        }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Convert Currency")
        }

        conversion?.let { result ->
            when {
                result.isSuccess -> {
                    val convertedAmount = result.getOrNull()
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Converted Amount: $convertedAmount $toCurrency",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                result.isFailure -> Text("Error: ${result.exceptionOrNull()?.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoricalRatesTab(
    startDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    historicalRates: Result<ExchangeRateResponse>?,
    onFetch: (LocalDate) -> Unit,
    onShowDatePicker: () -> Unit,
    currencyCodes: List<String>
) {
    var selectedCurrency by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Historical Rates", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            onValueChange = {},
            label = { Text("Select Date") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = onShowDatePicker) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onFetch(startDate) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Get Historical Rates")
        }

        historicalRates?.let { result ->
            when {
                result.isSuccess -> {
                    val data = result.getOrNull()
                    if (data != null) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Currency filter dropdown
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedCurrency ?: "All currencies",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filter currency") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All currencies") },
                                    onClick = {
                                        selectedCurrency = null
                                        expanded = false
                                    }
                                )
                                currencyCodes.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text(currency) },
                                        onClick = {
                                            selectedCurrency = currency
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Rates for ${data.base} (${data.date})", style = MaterialTheme.typography.titleMedium)

                        Spacer(modifier = Modifier.height(8.dp))

                        val filteredRates = if (selectedCurrency != null) {
                            data.rates.filterKeys { it == selectedCurrency }
                        } else {
                            data.rates
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredRates.entries.toList()) { (currency, rate) ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(currency, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                                        Text("%.4f".format(rate), style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    }
                }
                result.isFailure -> Text("Error: ${result.exceptionOrNull()?.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// DESIGN 3: Compact Grid Layout
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRateScreenDesign3(viewModel: ExchangeRateViewModel = viewModel()) {
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("EUR") }
    var amount by remember { mutableStateOf("100.0") }
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(7)) }
    var showDatePicker by remember { mutableStateOf(false) }

    val latestRates by viewModel.latestRates.collectAsState()
    val symbols by viewModel.symbols.collectAsState()
    val conversion by viewModel.conversion.collectAsState()
    val historicalRates by viewModel.historicalRates.collectAsState()

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        startDate = LocalDate.of(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Exchange Rate API", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)

        // Quick Actions Row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ElevatedButton(
                onClick = { viewModel.fetchLatestRates() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Latest Rates")
            }
            ElevatedButton(
                onClick = { viewModel.fetchSymbols() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Symbols")
            }
        }

        // Latest Rates Section
        latestRates?.let { result ->
            when {
                result.isSuccess -> {
                    val data = result.getOrNull()
                    Text("Latest Rates (${data?.date})", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(modifier = Modifier.height(120.dp)) {
                        items(data?.rates?.entries?.toList()?.take(10) ?: emptyList()) { (currency, rate) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("$currency:", style = MaterialTheme.typography.bodyMedium)
                                Text("%.2f".format(rate), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    if ((data?.rates?.size ?: 0) > 10) {
                        Text("+ ${(data?.rates?.size ?: 0) - 10} more currencies", style = MaterialTheme.typography.bodySmall)
                    }
                }
                result.isFailure -> Text("❌ Latest rates error", color = MaterialTheme.colorScheme.error)
            }
        }

        // Converter Section
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Quick Converter", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fromCurrency,
                        onValueChange = { fromCurrency = it },
                        label = { Text("From") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = toCurrency,
                        onValueChange = { toCurrency = it },
                        label = { Text("To") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        viewModel.convertCurrency(fromCurrency, toCurrency, amt)
                    }) {
                        Text("=")
                    }
                }
                conversion?.let { result ->
                    when {
                        result.isSuccess -> {
                            val convertedAmount = result.getOrNull()
                            Text("$amount $fromCurrency = $convertedAmount $toCurrency", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        result.isFailure -> Text("❌ Conversion error", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Historical Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Historical Rates", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    onValueChange = {},
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date"
                            )
                        }
                    }
                )
                Button(onClick = { viewModel.fetchHistoricalRates(startDate) }, modifier = Modifier.align(Alignment.End)) {
                    Text("Get Rates")
                }
                historicalRates?.let { result ->
                    when {
                        result.isSuccess -> {
                            val data = result.getOrNull()
                            LazyColumn(modifier = Modifier.height(120.dp)) {
                                items(data?.rates?.entries?.toList()?.take(8) ?: emptyList()) { (currency, rate) ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("$currency:", style = MaterialTheme.typography.bodyMedium)
                                        Text("%.2f".format(rate), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                        result.isFailure -> Text("❌ Historical rates error", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Symbols Section (Compact)
        symbols?.let { result ->
            when {
                result.isSuccess -> {
                    val data = result.getOrNull()
                    Text("Available Currencies", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(modifier = Modifier.height(100.dp)) {
                        items(data?.symbols?.entries?.toList()?.take(15) ?: emptyList()) { (code, name) ->
                            Text("$code - $name", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if ((data?.symbols?.size ?: 0) > 15) {
                        Text("+ ${(data?.symbols?.size ?: 0) - 15} more currencies", style = MaterialTheme.typography.bodySmall)
                    }
                }
                result.isFailure -> Text("❌ Symbols error", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// Current implementation (Design 2 by default)
@Composable
fun ExchangeRateScreen(viewModel: ExchangeRateViewModel = viewModel()) {
    ExchangeRateScreenDesign2(viewModel) // Change this to Design1() or Design3() to switch
}