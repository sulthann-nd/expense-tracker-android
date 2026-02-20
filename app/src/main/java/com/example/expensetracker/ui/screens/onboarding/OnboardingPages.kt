package com.example.expensetracker.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.ui.components.FeatureRow
import com.example.expensetracker.ui.components.OrangeButton

@Composable
fun WelcomePage(onNext: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Welcome to", color = Color.White, fontSize = 28.sp)
        Text("Expense Tracker", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(40.dp))
        // Replace with your Image(painterResource(R.drawable.wallet_illustration)...)
        Text("👛", fontSize = 120.sp)
        Spacer(Modifier.weight(1f))
        FeatureRow("Track Your Spending")
        FeatureRow("Categorize Expenses")
        FeatureRow("Gain Insights")
        Spacer(Modifier.height(60.dp))
        OrangeButton("Next", onClick = onNext)
    }
}

@Composable
fun FinancialGoalsPage(onNext: () -> Unit) {
    var limit by remember { mutableStateOf(1500f) }
    Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Set Your\nFinancial Goals", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(30.dp))
        Text("🐷", fontSize = 100.sp)
        Spacer(Modifier.weight(1f))
        Text("Monthly Spending Limit", color = Color.White.copy(0.9f))
        Slider(value = limit, onValueChange = { limit = it }, valueRange = 0f..5000f, colors = SliderDefaults.colors(thumbColor = Color.Yellow))
        Text("INR ${limit.toInt()}", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(60.dp))
        OrangeButton("Continue", onClick = onNext)
    }
}

@Composable
fun PrimaryGoalsPage(onGetStarted: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Primary Goals", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        // Replace with Image(painterResource(R.drawable.onboarding_graphics)...)
        Text("📊", fontSize = 120.sp)
        Spacer(Modifier.weight(1f))
        FeatureRow("Log expenses in seconds")
        FeatureRow("Understand your money better")
        FeatureRow("Reliable anytime, anywhere")
        Spacer(Modifier.height(60.dp))
        OrangeButton("Get Started", onClick = onGetStarted)
    }
}