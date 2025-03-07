package com.example.staysafe.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staysafe.model.data.User

@Composable
fun UserListSheet(
    users: List<User>,
    onUserSelected: (User) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Nearby Users", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        users.forEach { user ->
            UserListItem(user, onClick = { onUserSelected(user) })
        }
    }
}

@Composable
fun UserListItem(user: User, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(12.dp)) {
        Icon(Icons.Default.Person, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(user.userFirstname, fontWeight = FontWeight.Bold)
            Text("Tap to see location")
        }
    }
}

