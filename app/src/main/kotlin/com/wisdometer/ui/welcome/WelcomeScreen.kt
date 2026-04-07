package com.wisdometer.ui.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisdometer.ui.theme.WisdometerTypography

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Wisdometer",
                style = WisdometerTypography.headlineLarge,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Track your predictions. Sharpen your judgment.",
                style = WisdometerTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(40.dp))

            ConceptItem(
                title = "Make predictions",
                body = "Ask a question, add possible outcomes, and assign a probability to each. They must add up to 100%.",
            )
            Spacer(modifier = Modifier.height(20.dp))
            ConceptItem(
                title = "Set a timeframe",
                body = "Every prediction has a start and end date. When the end date arrives, come back and record what actually happened.",
            )
            Spacer(modifier = Modifier.height(20.dp))
            ConceptItem(
                title = "Track your accuracy",
                body = "Your profile shows how well-calibrated you are. A perfect forecaster who says 70% should be right 7 times out of 10.",
            )
            Spacer(modifier = Modifier.height(20.dp))
            ConceptItem(
                title = "Improve over time",
                body = "Review your Brier score, calibration chart, and accuracy trends. See which topics you predict best.",
            )

            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Get started")
            }
        }
    }
}

@Composable
private fun ConceptItem(title: String, body: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            style = WisdometerTypography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
