package com.likehome.app.ui.styles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likehome.app.R
import com.likehome.app.core.RingStyle
import com.likehome.app.ui.theme.Bangers
import com.likehome.app.ui.theme.Display
import com.likehome.app.ui.theme.LH

/** A tiny picture of each ringing style, so pickers read at a glance. */
@Composable
fun StyleThumb(style: RingStyle, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(14.dp))) {
        when (style) {
            RingStyle.CHAT -> ChatThumb(Modifier.fillMaxSize())
            RingStyle.COMIC -> ComicThumb(Modifier.fillMaxSize())
            RingStyle.SMART_MIX -> Row(Modifier.fillMaxSize()) {
                ChatThumb(Modifier.weight(1f).fillMaxHeight())
                ComicThumb(Modifier.weight(1f).fillMaxHeight())
            }
            RingStyle.NEWS -> Column(Modifier.fillMaxSize().background(Color(0xFF0B1B3A)).padding(8.dp), verticalArrangement = Arrangement.Bottom) {
                Text("BREAKING", style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = Color.White,
                    modifier = Modifier.background(Color(0xFFD62828)).padding(horizontal = 4.dp, vertical = 2.dp))
                Box(Modifier.fillMaxWidth().height(10.dp).background(Color.White))
            }
            RingStyle.RETRO -> Box(Modifier.fillMaxSize().background(Color(0xFF0D0D0D)), contentAlignment = Alignment.Center) {
                Text("BOSS", style = TextStyle(fontFamily = FontFamily(Font(R.font.press_start)), fontSize = 10.sp), color = Color(0xFF39FF14))
            }
            RingStyle.FILMY -> Box(
                Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF3A0712), Color(0xFF12020A)))),
                contentAlignment = Alignment.Center,
            ) {
                Text("The End", style = TextStyle(fontFamily = Display, fontSize = 16.sp), color = Color(0xFFE2B04A))
            }
            RingStyle.KITCHEN -> Box(Modifier.fillMaxSize().background(Color(0xFFFFE3C4)), contentAlignment = Alignment.Center) {
                Box(Modifier.size(26.dp).clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)).background(Color.White))
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(12.dp).background(Color(0xFFC98B55)))
            }
            RingStyle.NIGHT -> Box(
                Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF070914), Color(0xFF121733)))),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(Color(0xFFE9E4CF)))
                Box(Modifier.offset(x = 6.dp, y = (-4).dp).size(19.dp).clip(CircleShape).background(Color(0xFF0C1022)))
            }
            RingStyle.FRIDGE -> Box(Modifier.fillMaxSize().background(Color(0xFFE8ECEE)), contentAlignment = Alignment.Center) {
                Box(Modifier.size(40.dp).rotate(-6f).background(Color(0xFFFFE680)))
                Box(Modifier.offset(y = (-20).dp).size(9.dp).clip(CircleShape).background(Color(0xFFE17055)))
            }
        }
    }
}

@Composable
fun ChatThumb(modifier: Modifier) {
    Column(modifier.background(LH.Espresso).padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        listOf(0.9f, 0.7f, 0.8f).forEachIndexed { i, w ->
            Box(
                Modifier.fillMaxWidth(w).height(13.dp).clip(RoundedCornerShape(7.dp))
                    .background(if (i == 2) LH.MyBubble else LH.Bubble)
            )
        }
    }
}

@Composable
fun ComicThumb(modifier: Modifier) {
    Box(modifier.background(Color(0xFFFFD23F)).padding(8.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White)
                .border(2.dp, Color(0xFF111111), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text("UTH JAO!", style = TextStyle(fontFamily = Bangers, fontSize = 15.sp), color = Color(0xFF111111))
        }
    }
}
