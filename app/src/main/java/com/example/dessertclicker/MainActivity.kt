package com.example.dessertclicker

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.dessertclicker.data.Datasource
import com.example.dessertclicker.model.Dessert
import com.example.dessertclicker.ui.theme.DessertClickerTheme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate Called")
        setContent {
            DessertClickerTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    DessertClickerApp(desserts = Datasource.dessertList)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        logLifecycle("onStart")
    }

    override fun onResume() {
        super.onResume()
        logLifecycle("onResume")
    }

    override fun onRestart() {
        super.onRestart()
        logLifecycle("onRestart")
    }

    override fun onPause() {
        super.onPause()
        logLifecycle("onPause")
    }

    override fun onStop() {
        super.onStop()
        logLifecycle("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        logLifecycle("onDestroy")
    }

    private fun logLifecycle(event: String) {
        Log.d(TAG, "$event Called")
    }
}

fun determineDessertToShow(desserts: List<Dessert>, dessertsSold: Int): Dessert {
    return desserts.firstOrNull { dessertsSold < it.startProductionAmount } ?: desserts.last()
}

private fun shareSoldDessertsInformation(context: Context, dessertsSold: Int, revenue: Int) {
    val shareIntent = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text, dessertsSold, revenue))
        type = "text/plain"
    }, null)

    try {
        ContextCompat.startActivity(context, shareIntent, null)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, context.getString(R.string.sharing_not_available), Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun DessertClickerApp(desserts: List<Dessert>) {
    var revenue by rememberSaveable { mutableStateOf(0) }
    var dessertsSold by rememberSaveable { mutableStateOf(0) }
    var currentDessert by rememberSaveable { mutableStateOf(desserts.first()) }

    val context = LocalContext.current  // Utilisation du contexte correctement ici

    Scaffold(
        topBar = {
            DessertClickerAppBar {
                // Appel dans un bloc Composable avec un contexte valide
                shareSoldDessertsInformation(context, dessertsSold, revenue)
            }
        }
    ) { paddingValues ->
        DessertClickerScreen(
            revenue = revenue,
            dessertsSold = dessertsSold,
            dessertImageId = currentDessert.imageId,
            onDessertClicked = {
                revenue += currentDessert.price
                dessertsSold++
                currentDessert = determineDessertToShow(desserts, dessertsSold)
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun DessertClickerAppBar(onShareButtonClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_name),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(onClick = onShareButtonClicked) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = stringResource(R.string.share),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun DessertClickerScreen(
    revenue: Int,
    dessertsSold: Int,
    @DrawableRes dessertImageId: Int,
    onDessertClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.bakery_back),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                Image(
                    painter = painterResource(dessertImageId),
                    contentDescription = null,
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.image_size))
                        .height(dimensionResource(R.dimen.image_size))
                        .align(Alignment.Center)
                        .clickable { onDessertClicked() },
                    contentScale = ContentScale.Crop
                )
            }
            TransactionInfo(
                revenue = revenue,
                dessertsSold = dessertsSold,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TransactionInfo(revenue: Int, dessertsSold: Int, modifier: Modifier) {
    Column(modifier = modifier.padding(dimensionResource(R.dimen.padding_medium))) {
        DessertsSoldInfo(dessertsSold)
        RevenueInfo(revenue)
    }
}

@Composable
private fun RevenueInfo(revenue: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.total_revenue),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = "$$revenue",
            textAlign = TextAlign.Right,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun DessertsSoldInfo(dessertsSold: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.dessert_sold),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = dessertsSold.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Preview
@Composable
fun MyDessertClickerAppPreview() {
    DessertClickerTheme {
        DessertClickerApp(listOf(Dessert(R.drawable.cupcake, 5, 0)))
    }
}
