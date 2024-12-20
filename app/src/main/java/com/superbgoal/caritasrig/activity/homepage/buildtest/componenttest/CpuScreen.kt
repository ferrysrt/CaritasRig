package com.superbgoal.caritasrig.activity.homepage.buildtest.componenttest

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RangeSlider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.superbgoal.caritasrig.R
import com.superbgoal.caritasrig.data.loadItemsFromResources
import com.superbgoal.caritasrig.data.model.buildmanager.BuildManager
import com.superbgoal.caritasrig.data.model.component.Processor
import com.superbgoal.caritasrig.functions.auth.ComponentCard
import com.superbgoal.caritasrig.functions.auth.saveComponent

@Composable
fun CpuScreen(navController: NavController) {
    // Load processors data
    val context = LocalContext.current
    val processors: List<Processor> = remember {
        loadItemsFromResources(
            context = context,
            resourceId = R.raw.processor
        )
    }

    var showFilterDialog by remember { mutableStateOf(false) }
    var filteredProcessors by remember { mutableStateOf(processors) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Set background image
        Image(
            painter = painterResource(id = R.drawable.component_bg),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // Main content with TopAppBar and ProcessorList
        Column {
            TopAppBar(
                backgroundColor = Color.Transparent,
                contentColor = Color.White,
                elevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 10.dp)
                    ) {
                        Text(
                            text = "Part Pick",
                            style = MaterialTheme.typography.h4,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "CPU",
                            style = MaterialTheme.typography.subtitle1,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigateUp()
                        },
                        modifier = Modifier.padding(start = 20.dp, top = 10.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier.padding(end = 20.dp, top = 10.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter),
                            contentDescription = "Filter"
                        )
                    }
                }
            )

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                ProcessorList(processors = filteredProcessors,navController)
            }
        }

        if (showFilterDialog) {
            FilterDialog(
                onDismiss = { showFilterDialog = false },
                onApply = { coreCount, coreClock, boostClock, brands ->
                    showFilterDialog = false
                    filteredProcessors = processors.filter { processor ->
                        processor.core_count in coreCount &&
                                processor.core_clock in coreClock &&
                                processor.boost_clock in boostClock &&
                                brands.any { processor.name.contains(it, ignoreCase = true) }
                    }
                }
            )
        }
    }
}

@Composable
fun ProcessorList(processors: List<Processor>,navController: NavController) {
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(processors) { processor ->
            val isLoading = remember { mutableStateOf(false) }
            ComponentCard(
                title = processor.name,
                details = "${processor.price}$ | ${processor.core_count} cores | ${processor.core_clock} GHz",
                context = context,
                component = processor,
                isLoading = isLoading.value,
                navController = navController,
                onAddClick = {
                    isLoading.value = true
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val userId = currentUser?.uid.toString()
                    val buildTitle = BuildManager.getBuildTitle()
                    buildTitle?.let { title ->
                        saveComponent(
                            userId = userId,
                            buildTitle = title,
                            componentType = "cpu",
                            componentData = processor,
                            onSuccess = {
                                isLoading.value = false
                                navController.navigateUp()
                                Log.e("test", "ketriger")
                            },
                            onFailure = { errorMessage ->
                                isLoading.value = false

                            },
                            onLoading = { isLoading.value = it }
                        )
                    } ?: run {
                        isLoading.value = false
                        Log.e("BuildActivity", "Build title is null; unable to store CPU.")
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onApply: (coreCount: IntRange, coreClock: ClosedFloatingPointRange<Double>, boostClock: ClosedFloatingPointRange<Double>, selectedBrands: List<String>) -> Unit
) {
    // States for filter criteria
    val coreCountRange = remember { mutableStateOf(0f..16f) }
    val coreClockRange = remember { mutableStateOf(1.0f..5.0f) }
    val boostClockRange = remember { mutableStateOf(1.0f..5.0f) }
    val selectedBrands = remember { mutableStateOf(listOf("AMD", "Intel")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Filter CPUs")
        },
        text = {
            Column {
                // Core count slider
                Text(text = "Core Count: ${coreCountRange.value.start.toInt()} - ${coreCountRange.value.endInclusive.toInt()}")
                RangeSlider(
                    value = coreCountRange.value,
                    onValueChange = { range ->
                        coreCountRange.value = range
                    },
                    valueRange = 0f..16f
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Core clock slider
                Text(text = "Core Clock: ${
                    String.format("%.2f", coreClockRange.value.start)
                } GHz - ${
                    String.format("%.2f", coreClockRange.value.endInclusive)
                } GHz")
                RangeSlider(
                    value = coreClockRange.value,
                    onValueChange = { range ->
                        coreClockRange.value = range
                    },
                    valueRange = 1.0f..5.0f
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Boost clock slider
                Text(text = "Boost Clock: ${
                    String.format("%.2f", boostClockRange.value.start)
                } GHz - ${
                    String.format("%.2f", boostClockRange.value.endInclusive)
                } GHz")
                RangeSlider(
                    value = boostClockRange.value,
                    onValueChange = { range ->
                        boostClockRange.value = range
                    },
                    valueRange = 1.0f..5.0f
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Brand selection checkboxes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = "AMD" in selectedBrands.value,
                        onCheckedChange = {
                            if (it) selectedBrands.value = selectedBrands.value + "AMD"
                            else selectedBrands.value = selectedBrands.value - "AMD"
                        }
                    )
                    Text(text = "AMD")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = "Intel" in selectedBrands.value,
                        onCheckedChange = {
                            if (it) selectedBrands.value = selectedBrands.value + "Intel"
                            else selectedBrands.value = selectedBrands.value - "Intel"
                        }
                    )
                    Text(text = "Intel")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onApply(
                    coreCountRange.value.start.toInt()..coreCountRange.value.endInclusive.toInt(),
                    coreClockRange.value.start.toDouble()..coreClockRange.value.endInclusive.toDouble(),
                    boostClockRange.value.start.toDouble()..boostClockRange.value.endInclusive.toDouble(),
                    selectedBrands.value
                )
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}