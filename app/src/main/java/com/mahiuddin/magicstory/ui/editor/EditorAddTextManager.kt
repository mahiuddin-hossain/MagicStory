package com.mahiuddin.magicstory.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.R
import com.mahiuddin.magicstory.ui.theme.PrimaryRed

@Composable
fun EditorAddTextDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onAddHorizontalTextClicked: () -> Unit,
    onAddVerticalTextClicked: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(Color.White)
    ) {
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_text_icon),
                        contentDescription = "Horizontal Text",
                        tint = Color(0xFF202020),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Horizontal Text",
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Color(0xFF202020)
                    )
                }
            },
            onClick = {
                onAddHorizontalTextClicked()
                onDismiss()
            }
        )

        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.font_text_icon),
                        contentDescription = "Vertical Text",
                        tint = Color(0xFF202020),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Vertical Text",
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Color(0xFF202020)
                    )
                }
            },
            onClick = {
                onAddVerticalTextClicked()
                onDismiss()
            }
        )
    }
}

@Composable
fun AddTextInputDialog(
    isVertical: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf(if (isVertical) "VERTICAL" else "YOUR TEXT") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isVertical) "Add Vertical Text 🔠" else "Add Horizontal Text 🔤",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202020),
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Enter Text") },
                    singleLine = !isVertical,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        cursorColor = PrimaryRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onConfirm(inputText.trim())
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add Text", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF777777))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
