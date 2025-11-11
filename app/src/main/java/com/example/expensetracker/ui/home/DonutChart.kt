package com.example.expensetracker.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun DonutChart(
    income: Double,
    expenses: Double,
    modifier: Modifier = Modifier
) {
    val total = income + expenses
    val incomeRatio = if (total > 0) income / total else 0.0
    val expenseRatio = if (total > 0) expenses / total else 0.0

    var startAnimation by remember { mutableStateOf(false) }
    val animatedIncomeSweep by animateFloatAsState(
        targetValue = if (startAnimation) (incomeRatio * 180f).toFloat() else 0f,
        animationSpec = androidx.compose.animation.core.tween(1200)
    )
    val animatedExpenseSweep by animateFloatAsState(
        targetValue = if (startAnimation) (expenseRatio * 180f).toFloat() else 0f,
        animationSpec = androidx.compose.animation.core.tween(1200)
    )
    val animatedSweep by animateFloatAsState(
        targetValue = if (startAnimation) (180f).toFloat() else 0f,
        animationSpec = androidx.compose.animation.core.tween(1200)
    )

    LaunchedEffect(Unit) { startAnimation = true }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 24.dp.toPx()
                val radius = min(size.width, size.height) / 2
                val topLeft = size.center.copy(x = size.center.x - radius, y = size.center.y - radius)
                val arcSize = Size(radius * 2, radius * 2)

                if(incomeRatio == 0.0 && expenseRatio == 0.0){
                    drawArc(
                        color = Color.LightGray,
                        startAngle = 180f,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                } else {
                    drawArc(
                        color = Color(0xFFFF6F61), // Red
                        startAngle = 180f,
                        sweepAngle = animatedExpenseSweep,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = Color(0xFF4CAF50), // Green
                        startAngle = 180f + animatedExpenseSweep,
                        sweepAngle = animatedIncomeSweep,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "RON",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = String.format("%.2f", income - expenses),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (income - expenses >= 0) Color(0xFF4CAF50) else Color(0xFFFF6F61)
                )
            }
        }
    }
}