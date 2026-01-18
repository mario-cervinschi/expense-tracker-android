package com.example.expensetracker.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

@Composable
fun DonutChart(
    income: Double,
    expenses: Double,
    modifier: Modifier = Modifier
) {
    val animatedIncomeValue by animateFloatAsState(
        targetValue = income.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "income_animation"
    )

    val animatedExpensesValue by animateFloatAsState(
        targetValue = expenses.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "expenses_animation"
    )

    val total = animatedIncomeValue + animatedExpensesValue
    val incomeRatio = if (total > 0) animatedIncomeValue / total else 0f
    val expenseRatio = if (total > 0) animatedExpensesValue / total else 0f

    var startAnimation by remember { mutableStateOf(false) }

    val animatedIncomeSweep by animateFloatAsState(
        targetValue = if (startAnimation) (incomeRatio * 180f) else 0f,
        animationSpec = tween(1200),
        label = "income_sweep"
    )
    val animatedExpenseSweep by animateFloatAsState(
        targetValue = if (startAnimation) (expenseRatio * 180f) else 0f,
        animationSpec = tween(1200),
        label = "expense_sweep"
    )
    val animatedSweep by animateFloatAsState(
        targetValue = if (startAnimation) 180f else 0f,
        animationSpec = tween(1200),
        label = "full_sweep"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Column(
        modifier = modifier.padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(220.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 28.dp.toPx()
                val radius = min(size.width, size.height) / 2
                val topLeft = size.center.copy(
                    x = size.center.x - radius,
                    y = size.center.y - radius
                )

                if (incomeRatio == 0f && expenseRatio == 0f) {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        startAngle = 180f,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                } else {
                    drawArc(
                        color = Color(0xFFF44336),
                        startAngle = 180f,
                        sweepAngle = animatedExpenseSweep,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = 180f + animatedExpenseSweep,
                        sweepAngle = animatedIncomeSweep,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "In: +${String.format("%.0f", animatedIncomeValue)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                )

                Text(
                    text = "Out: -${String.format("%.0f", animatedExpensesValue)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF44336)
                    )
                )

                HorizontalDivider(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .size(width = 40.dp, height = 1.dp),
                    color = Color.LightGray
                )

                Text(
                    text = "Balance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format("%.2f", animatedIncomeValue - animatedExpensesValue),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = if (animatedIncomeValue - animatedExpensesValue >= 0)
                        Color(0xFF4CAF50)
                    else
                        Color(0xFFF44336)
                )
                Text(
                    text = "RON",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}