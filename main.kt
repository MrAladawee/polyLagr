import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.MouseInfo
import java.awt.event.MouseEvent
import kotlin.math.abs

@OptIn(ExperimentalTextApi::class)
@Composable
@Preview
fun App() {

    var txtMeasurer = rememberTextMeasurer()

    var xMin by remember {mutableStateOf(-10)}
    var xMax by remember {mutableStateOf(10)}

    var yMinMax by remember {mutableStateOf(0f)}

    var points by remember { mutableStateOf(mutableMapOf<Float, Float>()) }

    Canvas(modifier = Modifier.fillMaxSize().clickable { var p = MouseInfo.getPointerInfo().location; points[p.x.toFloat()] = p.y.toFloat()},
        onDraw = {

            // Axis
            // ox
            drawLine(color = Color.Black, start = Offset(0f, this.size.height*(1+yMinMax)/2), end = Offset(this.size.width, this.size.height*(1+yMinMax)/2))
            // oy
            drawLine(color = Color.Black, start = Offset(-this.size.width*(xMin) / (xMax - xMin), 0f), end = Offset(-this.size.width * (xMin) / (xMax - xMin), this.size.height))

            // text Ox
            for (i in xMin..xMax) {
                drawText(textMeasurer = txtMeasurer, text = i.toString(), topLeft = Offset(this.size.width * (i - xMin) / (xMax - xMin), this.size.height*(1+yMinMax)/2))
                drawLine(color = Color.Black, start = Offset(this.size.width*(i-xMin)/(xMax-xMin),this.size.height*(1+yMinMax)/2-5f),
                    end = Offset(this.size.width*(i-xMin)/(xMax-xMin),this.size.height*(1+yMinMax)/2+5f))
            }

            //draw point
            for (point in points) {
                drawCircle(color = Color.Green, center = Offset(point.key, point.value), radius = 5f)
            }
        })
    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
        Row {
            TextField(value = xMin.toString(), onValueChange = { text -> xMin = text.toIntOrNull() ?: xMin })
            TextField(value = xMax.toString(), onValueChange = { text -> xMax = text.toIntOrNull() ?: xMax })
            Slider(value = yMinMax, onValueChange = { text -> yMinMax = text }, valueRange = -1f..1f)
        }
    }


}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
