package com.example.pr22_novgorodtseva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.pr22_novgorodtseva.ui.theme.Pr22_novgorodtsevaTheme

@Composable
fun MemoriaGameScreen(gridSize: Int = 6, modifier: Modifier = Modifier) {
    var isGameOver by remember { mutableStateOf(false) }
    val cellStates = remember { mutableStateListOf(*Array(gridSize * gridSize) { CellState.CLOSED }) }
    val pictures = remember { generatePictureList(gridSize).toMutableList() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isGameOver) {
            Text(
                "Игра закончена!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    cellStates.replaceAll { CellState.CLOSED }
                    pictures.shuffle()
                    isGameOver = false
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Начать заново", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(gridSize)) {
                items(pictures.size) { index ->
                    Cell(
                        pictureName = pictures[index],
                        state = cellStates[index],
                        onClick = {
                            coroutineScope.launch {
                                onCellClick(index, cellStates, pictures) { gameOver ->
                                    isGameOver = gameOver
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Cell(pictureName: String, state: CellState, onClick: () -> Unit) {
    val imageId = when (state) {
        CellState.CLOSED -> R.drawable.closed_image
        CellState.OPEN -> getDrawableId(pictureName) ?: R.drawable.closed_image
        CellState.DELETED -> getDrawableId(pictureName) ?: R.drawable.closed_image
    }

    Image(
        painter = painterResource(imageId),
        contentDescription = null,
        modifier = Modifier
            .size(80.dp)
            .clickable(enabled = state == CellState.CLOSED, onClick = onClick)
    )
}

fun generatePictureList(gridSize: Int): List<String> {
    val pictures = mutableListOf<String>()
    val pictureNames = listOf(
        "ananas", "arbuz", "banan", "granat", "grusha", "kiwi", "klubnika",
        "limon", "malina", "malina2", "persik", "potato", "sliva", "vinograd",
        "vinograd1", "vinograd2", "vishnia"
    )
    val pairs = (gridSize * gridSize) / 2
    for (i in 0 until pairs) {
        pictures.add(pictureNames[i % pictureNames.size])
        pictures.add(pictureNames[i % pictureNames.size])
    }
    return pictures.shuffled()
}

suspend fun onCellClick(
    index: Int,
    cellStates: MutableList<CellState>,
    pictures: List<String>,
    onGameOver: (Boolean) -> Unit
) {
    if (cellStates[index] != CellState.CLOSED) return

    cellStates[index] = CellState.OPEN

    val openCells = cellStates.mapIndexedNotNull { i, state -> if (state == CellState.OPEN) i else null }

    if (openCells.size == 2) {
        delay(500)

        if (pictures[openCells[0]] == pictures[openCells[1]]) {
            cellStates[openCells[0]] = CellState.DELETED
            cellStates[openCells[1]] = CellState.DELETED
        } else {
            // Закрываем разные картинки
            cellStates[openCells[0]] = CellState.CLOSED
            cellStates[openCells[1]] = CellState.CLOSED
        }
    }

    // Проверка, завершена ли игра
    onGameOver(cellStates.none { it == CellState.CLOSED })
}

enum class CellState {
    CLOSED,
    OPEN,
    DELETED
}

@Composable
fun getDrawableId(name: String): Int? {
    val context = LocalContext.current
    val drawableId = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (drawableId != 0) drawableId else null
}