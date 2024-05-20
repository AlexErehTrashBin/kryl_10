import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.*
import kotlinx.coroutines.flow.transform
import org.jetbrains.compose.ui.tooling.preview.Preview

data class GraphParams(
    val xStart: Double,
    val xEnd: Double,
    val stepSize: Double,
    val yStart: Double,
)

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun Graph(
    params: GraphParams
) {
    val method = remember(params.xStart, params.yStart, params.xEnd, params.stepSize) {
        /*RungeKuttaMethod(
            params.xStart, params.yStart, params.xEnd, params.stepSize
        ) { x: Double, y: Double ->
            (y - 2 * x * y * y) / (x)
        }*/
        RungeKuttaMethod(
            params.xStart, params.yStart, params.xEnd, params.stepSize
        ) { x: Double, y: Double ->
            ((y * y * y) / 2.0 - y / x) / (x)
        }
    }
    val state by method.state.transform { value ->
        if (value == null) {
            return@transform
        }
        val mapped = value
            .map { element ->
                DefaultPoint(element.first, element.second)
            }
        emit(mapped)
    }.collectAsState(initial = null)
    if (state == null) {
        Text("Не удаётся построить график!")
    } else {
        XYGraph(
            rememberDoubleLinearAxisModel(state!!.autoScaleXRange(), allowZooming = true, allowPanning = true),
            rememberDoubleLinearAxisModel(state!!.autoScaleYRange(), allowZooming = true, allowPanning = true)
        ) {
            LinePlot(
                state!!,
                lineStyle = LineStyle(SolidColor(Color.Blue), strokeWidth = 3.dp)
            )
        }
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        val xStart = remember {
            mutableStateOf("0.25")
        }
        val xEnd = remember {
            mutableStateOf("10.0")
        }
        val stepSize = remember {
            mutableStateOf("0.1")
        }
        val yStart = remember {
            mutableStateOf("1.0")
        }

        val transformed = remember {
            mutableStateOf(
                try {
                    GraphParams(
                        xStart = xStart.value.toDouble(),
                        xEnd = xEnd.value.toDouble(),
                        stepSize = stepSize.value.toDouble(),
                        yStart = yStart.value.toDouble()
                    )
                } catch (e: NumberFormatException) {
                    null
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(0.4f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = xStart.value,
                    onValueChange = {
                        xStart.value = it
                    },
                    label = {
                        Text("Start X")
                    }
                )
                TextField(
                    value = xEnd.value,
                    onValueChange = {
                        xEnd.value = it
                    },
                    label = {
                        Text("End X")
                    }
                )
                TextField(
                    value = stepSize.value,
                    onValueChange = {
                        stepSize.value = it
                    },
                    label = {
                        Text("Step size")
                    }
                )
                TextField(
                    value = yStart.value,
                    onValueChange = {
                        yStart.value = it
                    },
                    label = {
                        Text("Y Start")
                    }
                )
                Button(
                    onClick = {
                        transformed.value = try {
                            GraphParams(
                                xStart = xStart.value.toDouble(),
                                xEnd = xEnd.value.toDouble(),
                                stepSize = stepSize.value.toDouble(),
                                yStart = yStart.value.toDouble()
                            )
                        } catch (e: NumberFormatException) {
                            null
                        }
                    }
                ) {
                    Text("Recalculate")
                }
            }
            Column(
                modifier = Modifier.weight(0.6f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (transformed.value != null) {
                    Graph(
                        params = transformed.value!!
                    )
                } else {
                    Text("Не удаётся построить график!")
                }
            }
        }
    }
}