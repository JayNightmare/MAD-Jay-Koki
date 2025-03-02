package com.example.staysafe.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.staysafe.model.data.Location
import com.example.staysafe.model.data.User
import com.example.staysafe.ui.components.BottomNavigationBar
import com.example.staysafe.ui.components.TopNavigationBar
import com.example.staysafe.viewModel.PeopleViewModel

@Composable
fun PeopleScreen(navController: NavController, viewModel: PeopleViewModel){
    val users by viewModel.users.collectAsStateWithLifecycle()
    val username by viewModel.usernames.collectAsStateWithLifecycle()
    val location by viewModel.locationName.collectAsStateWithLifecycle()
    val longitude by viewModel.userLongitude.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle() // Collect locations as Map

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = { TopNavigationBar() },
        bottomBar = { BottomNavigationBar() }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (users.isEmpty()) {
                Text(
                    text = "No people found",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                //Should I make locationID to foreign key of the users?
                /*LazyColumn {
                    items(users) { user ->
                        // Find the correct Location for this User
                        val userLocation = locations[user.toString()]?: Location(0)
                        PeopleItem(users, userLocation)
                    }
                }
            }

                 */
        }
    }
}

}
@Composable
fun PeopleItem(user: User, location: Location){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}, modifier = Modifier.size(40.dp),false, colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White
            )
        }
        Column (modifier = Modifier.weight(1f)) {
            Text(
                text =user.userUsername,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = location.locationName,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        //Distance
        Text(
            text = user.userLongitude.toString(),
            color = Color.White,
            fontSize = 14.sp
        )
    }
}