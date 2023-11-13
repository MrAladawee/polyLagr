import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.MouseInfo
import java.awt.Point
import java.awt.event.MouseEvent
import javax.xml.crypto.Data
import kotlin.Exception
import kotlin.math.abs

fun Interpolation(Ox : Array<Double>, Oy : Array<Double>, xval : Double) : Double
{

    /*
    yval = sum(i = 0, Ox.size - 1) Oy[i] * ( prod(j = 0, Ox.size - 1) if (i!=j) (xval - Ox[j])/(Ox[i] - Ox[j]) )
     */

    var yval = 0.0;
    var prod : Double

    for (i in 0 .. Ox.size - 1)
    {
        prod = Oy[i]

        for (j in 0 .. Ox.size - 1)
        {
            if (i != j)
            {
                prod *= (xval - Ox[j]) / (Ox[i] - Ox[j])
            }
        }
        yval += prod
    }
    return yval
}

// Общее вычисление значений Oy во всем множестве точек
fun Interpolation(Ox : Array<Double>, Oy : Array<Double>, xvals : Array<Int>) : Array<Double>
{
    var yvals = Array(xvals.size) { 0.0 }

    for (i in 0 .. xvals.size - 1) {
        yvals[i] = Interpolation(Ox, Oy, xvals[i].toDouble())
    }

    return yvals;
}

fun Interpolation(Ox : Array<Double>, Oy : Array<Double>, xvals : Array<Double>) : Array<Double>
{
    var yvals = Array(xvals.size) { 0.0 }

    for (i in 0 .. xvals.size - 1) {
        yvals[i] = Interpolation(Ox, Oy, xvals[i])
    }

    return yvals;
}

fun convertToScreen(cartesianx : Double, cartesiany : Double, screenwidth : Int, screenheight : Int) : Pair<Double, Double> {
    return Pair(cartesianx + screenwidth / 2, -cartesiany + screenheight / 2)
}

fun converToDec(screenx : Double, screeny : Double, screenwidth : Int, screenheight : Int) : Pair<Double, Double>{
    return Pair(screenx - screenwidth / 2, -screeny + screenheight / 2)
}

fun converToDecX(screenx : Double, screenwidth : Int, screenheight : Int) : Double{
    return screenx - screenwidth / 2
}

fun converToDecY(screeny : Double, screenwidth : Int, screenheight : Int) : Double{
    return -screeny + screenheight / 2
}

var DataSetScale : Array<Pair<Int,Int>> = Array<Pair<Int,Int>>(2) {Pair(0,0)}

@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {

    // Initialization values of Mins & Maxs x, y
    var xMin by remember { mutableStateOf(-10) }
    var xMax by remember { mutableStateOf( 10) }
    var yMin by remember { mutableStateOf(0) }
    var ymin by remember { mutableStateOf(-10) }
    var yMax by remember { mutableStateOf(10) }

    var yMinMax by remember{mutableStateOf(0f)}
    var selectY by remember{mutableStateOf(true)}

    val textMeasurer = rememberTextMeasurer()

    // DataBase of dots. Example: (0.0, 1.0)
    var points_screen by remember { mutableStateOf(mutableMapOf<Double, Double>()) }
    var points_cart by remember { mutableStateOf(mutableMapOf<Double, Double>()) }


    // Main window for graphics
    Canvas(modifier = Modifier.fillMaxSize().clickable{}. onPointerEvent(PointerEventType.Press){


        // Click in window
        var point = it.changes.first().position // screen point

        var (point_dec_x, point_dec_y) = converToDec(point.x.toDouble(), point.y.toDouble(), this.size.width, this.size.height)


        points_cart[point_dec_x] = point_dec_y
        points_screen[point.x.toDouble()] = point.y.toDouble()

    },
        onDraw = {

            var width : Int
            var height : Int

            if (DataSetScale[0] == Pair(0,0)) {
                width = this.size.width.toInt()
                height = this.size.height.toInt()
                DataSetScale[0] = Pair(width,height)
            }
            else if (DataSetScale[0] != Pair(0,0) && DataSetScale[1] == Pair(0,0)) {
                width = this.size.width.toInt()
                height = this.size.height.toInt()
                DataSetScale[1] = Pair(width,height)
            }
            else {
                DataSetScale[0] = DataSetScale[1]
                width = this.size.width.toInt()
                height = this.size.height.toInt()
                DataSetScale[1] = Pair(width,height)
            }

            var yMax = this.size.height*(xMax-xMin)/this.size.width + yMin

            if(yMax*yMin<0){
                yMinMax = ((xMax-xMin)/(xMax+xMin)).toFloat()
            }

            // Axis OX
            drawLine(
                color = Color.Black,
                start = Offset(0f, this.size.height * (1 + yMinMax) / 2),
                end = Offset(this.size.width, this.size.height * (1 + yMinMax) / 2)
            )

            // Axis OY
            drawLine(color = Color.Black,
                start = Offset(-this.size.width*xMin/(xMax-xMin), 0f),
                end = Offset(-this.size.width*xMin/(xMax-xMin), this.size.height))

            // Text below OX
            for(i in xMin .. xMax) {

                drawLine(color = Color.Black,
                    start = Offset(this.size.width*(i-xMin)/(xMax-xMin),
                        this.size.height*(1+yMinMax)/2-5),
                    end = Offset(this.size.width*(i-xMin)/(xMax-xMin),
                        this.size.height*(1+yMinMax)/2+5))

                drawText(textMeasurer = textMeasurer, text = i.toString(),
                    topLeft = Offset(this.size.width*(i-xMin)/(xMax-xMin)-5,
                        this.size.height*(1+yMinMax)/2))

            }

            /*
                for(point in points) {
                drawCircle(
                    color = Color.Green,
                    radius = 10f,
                    center = Offset(point.key.toFloat(), point.value.toFloat())
                )
            }
            */


            for (point in points_cart) {

                var (pointScreeX, pointScreenY) = convertToScreen(
                    point.key.toDouble(),
                    point.value.toDouble(),
                    width,
                    height
                )

                drawCircle(
                    color = Color.Red,
                    radius = 3f,
                    center = Offset(pointScreeX.toFloat(), pointScreenY.toFloat())
                )
            }

            /* РАБОТА С СКРИН ПОИНТ
            // Узловые points
            var Ox_screen: Array<Double> = Array(points_screen.size) { 0.0 }
            var Oy_screen: Array<Double> = Array(points_screen.size) { 0.0 }

            var count = 0
            for (point in points_screen) {
                Ox_screen[count] = point.key
                Oy_screen[count] = point.value
                count++
            }

            // All pixels
            var xvals_screen: Array<Int> = Array(width) { 0 }
            for (i in 0..width - 1) {
                xvals_screen[i] = i
            }

            var yvals_screen: Array<Double>
            yvals_screen = Interpolation(Ox_screen, Oy_screen, xvals_screen)

            for (i in 1..xvals_screen.size - 1) {
                drawLine(
                    color = Color.Blue,
                    start = Offset(xvals_screen[i - 1].toFloat(), yvals_screen[i - 1].toFloat()),
                    end = Offset(xvals_screen[i].toFloat(), yvals_screen[i].toFloat())
                )
            }
            */

            // Узловые points
            var Ox_cart: Array<Double> = Array(points_screen.size) { 0.0 }
            var Oy_cart: Array<Double> = Array(points_screen.size) { 0.0 }

            var count = 0
            for (point in points_cart) {
                Ox_cart[count] = point.key
                Oy_cart[count] = point.value
                count++
            }

            // All pixels

            var xvals_cart = Array<Double>(width) {0.0}

            for (i in 0..width-1) {

                xvals_cart[i] = converToDecX(i.toDouble(), width, height)

            }

            var yvals_cart: Array<Double>
            yvals_cart = Interpolation(Ox_cart, Oy_cart, xvals_cart)

            for (i in 1..xvals_cart.size - 1) {


                var (first_x_screen, frist_y_screen) = convertToScreen(xvals_cart[i - 1], yvals_cart[i - 1], width, height)
                var (second_x_screen, second_y_screen) = convertToScreen(xvals_cart[i], yvals_cart[i], width, height)



                drawLine(
                    color = Color.Blue,
                    start = Offset(first_x_screen.toFloat(), frist_y_screen.toFloat()),
                    end = Offset(second_x_screen.toFloat(), second_y_screen.toFloat())
                )
            }

            /*if (points.size != 0) {
                var coord = Array<Point>(this.size.width.toInt()) {Point(0,0)}

                var xvals = Array(this.size.width.toInt()) {0};
                var x : Array<Double> = Array(points.size) {0.0}
                var y : Array<Double> = Array(points.size) {0.0}

                var count = 0

                for (part in points) {
                    drawCircle(
                        color = Color.Green,
                        radius = 10f,
                        center = Offset(part.key.toFloat(), part.value.toFloat())
                    )

                    x[count] = part.key.toDouble()
                    y[count] = part.key.toDouble()

                    count++
                }

                for (i in 0 .. this.size.width.toInt() - 1) {
                    xvals[i] = i
                }

                var yvals = Interpolation(x,y, xvals)

                for (i in 0 .. points.size - 1) {
                    coord[i].x = xvals[i]
                    coord[i].y = yvals[i].toInt()
                }

                for (i in 1..coord.size-1) {
                    drawLine(
                        color = Color.Blue,
                        start = Offset(coord[i-1].x.toFloat(), coord[i-1].y.toFloat()),
                        end = Offset(coord[i].x.toFloat(), coord[i].y.toFloat())
                    )
                }

            }
             */

        })

    // Text and edit values of Mins & Maxs x,y
    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {

        Row(modifier = Modifier.padding(10.dp, 10.dp)){

            // TextField for xMin
            Column(modifier = Modifier.padding(10.dp, 10.dp)){

                Text("xMin")
                TextField(value = xMin.toString(),
                    onValueChange = { value -> xMin = value.toIntOrNull() ?:-10 })

            }

            // TextField for xMax
            Column(modifier = Modifier.padding(10.dp, 10.dp)) {

                Text("xMax")
                TextField(value = xMax.toString(),
                    onValueChange = { value -> xMax = value.toIntOrNull() ?: 0 })

            }

            // Select input type for yMin & yMinMax
            Column{

                Row{
                    RadioButton(
                        selected = selectY,
                        onClick = { selectY = true },
                        modifier = Modifier.padding(8.dp)
                    )
                    Text("yMin", fontSize = 22.sp)
                }
                Row{
                    RadioButton(
                        selected = !selectY,
                        onClick = { selectY = false },
                    )
                    Text("ySlider", fontSize = 22.sp)
                }

            }

            Column(modifier = Modifier.padding(10.dp, 10.dp)) {

                // Hide slider for weight
                if(!selectY)
                {
                    Slider(value = yMinMax,
                        valueRange = -1f..1f,
                        steps = 9,
                        onValueChange = { yMinMax = it })
                }

                // Hide textField for yMin
                else
                {
                    TextField(value = yMin.toString(),
                        onValueChange = { value -> yMin = value.toIntOrNull() ?: 0 })
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
