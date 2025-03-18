package com.example.staysafe.map

import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.staysafe.R
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState

@OptIn(UnstableApi::class)
@Composable
fun CustomMarker(
    imageUrl: String?,
    fullName: String,
    location: LatLng,
    onClick: () -> Unit,
    size: Int
) {
    Log.d("CustomMarker", "CustomMarker called with imageUrl: $imageUrl")
    val markerState = remember { MarkerState(position = location) }
    val outerShape = RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp)
    val innerShape = RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp)
//    val painter = rememberAsyncImagePainter(
//        ImageRequest.Builder(LocalContext.current)
//            .data(imageUrl)
//            .allowHardware(false)
//            .build()
//    )


    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .allowHardware(false)
            .error(R.drawable.avataaars)
            .placeholder(R.drawable.avataaars)
            .build()
    )


    MarkerComposable(
        keys = arrayOf(fullName, painter.state),
        state = markerState,
        title = fullName,
        anchor = Offset(0.5f, 1f),
        onClick = {
            onClick()
            true
        }
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(outerShape)
                .background(Color(0xff304358))
                .padding(3.dp)
                .shadow(4.dp, outerShape)
                .border(1.dp, Color.White, innerShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painter,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(innerShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}
