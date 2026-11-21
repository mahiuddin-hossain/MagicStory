package com.mahiuddin.magicstory.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahiuddin.magicstory.ui.components.TemplateCard

data class QuickCategoryItem(
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onTemplateSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val templates by viewModel.templates.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val quickCategories = listOf(
        QuickCategoryItem("Social media", Icons.Outlined.Favorite),
        QuickCategoryItem("Presentations", Icons.Outlined.CoPresent),
        QuickCategoryItem("Videos", Icons.Outlined.Videocam),
        QuickCategoryItem("Prints", Icons.Outlined.Print),
        QuickCategoryItem("Website", Icons.Outlined.Language)
    )

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.White,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    // Header Bar: Magic Design Logo & Crown Icon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Red Play/Magic Logo Icon
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Unspecified
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val path = Path().apply {
                                        moveTo(size.width * 0.2f, size.height * 0.15f)
                                        lineTo(size.width * 0.85f, size.height * 0.5f)
                                        lineTo(size.width * 0.2f, size.height * 0.85f)
                                        close()
                                    }
                                    drawPath(
                                        path = path,
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFFFF0055), Color(0xFFFF5858))
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Magic ",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = Color(0xFF181818)
                            )
                            Text(
                                text = "Design",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = Color(0xFFE50914)
                            )
                        }

                        // Gold/Red Crown Icon
                        IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = "VIP Crown",
                                tint = Color(0xFFE50914),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Search your content or Canva's",
                                color = Color(0xFF888888),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF202020),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = "Camera Search",
                                tint = Color(0xFF202020),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE0E0E0),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color(0xFFFAFAFA)
                        )
                    )
                }
            },
            bottomBar = {
                // Bottom Bar Spacer (Placeholder for Floating Bottom Navigation Bar)
                Spacer(modifier = Modifier.height(64.dp))
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Quick Category Action Cards Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    quickCategories.forEach { item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                if (categories.contains(item.title)) {
                                    viewModel.selectCategory(item.title)
                                } else {
                                    viewModel.selectCategory("All")
                                }
                            }
                        ) {
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFFFF0F3)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = Color(0xFFE50914),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF555555)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Section 1: "Easy-to-assemble videos" / Popular Templates Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Easy-to-assemble videos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181818)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.selectCategory("All") }
                    ) {
                        Text(
                            text = "See All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE50914)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = "See All",
                            tint = Color(0xFFE50914),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Horizontal Carousel Cards (Play with Video, Mobile Video, Facebook Video)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Card 1: Play with Video (Purple Gradient Card)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onTemplateSelect("minimalist_journal") }
                    ) {
                        Surface(
                            modifier = Modifier.size(150.dp, 100.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Unspecified
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Outlined.Movie,
                                        contentDescription = "Play with Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Play with\nVideo",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Video", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    }

                    // Card 2: Mobile Video (Soft Pink Photo Banner)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onTemplateSelect("urban_sale") }
                    ) {
                        Surface(
                            modifier = Modifier.size(150.dp, 100.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF0F3)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "📱\nMobile Video",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFE50914),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Mobile Video", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    }

                    // Card 3: Facebook Video
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onTemplateSelect("pastel_portrait") }
                    ) {
                        Surface(
                            modifier = Modifier.size(150.dp, 100.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE3F2FD)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "👍\nFacebook Video",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1976D2),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Facebook Video", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Section 2: Large Hero Editor Banner Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Soft Pink Banner Card
                    Surface(
                        modifier = Modifier
                            .width(280.dp)
                            .height(140.dp)
                            .clickable { onTemplateSelect("minimalist_journal") },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF0F5)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Edit your\nvideos with\nease",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF181818)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Try video editor →",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFE50914)
                                )
                            }

                            Surface(
                                modifier = Modifier.size(74.dp, 94.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFF4081)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Surface(
                                        modifier = Modifier.size(32.dp),
                                        shape = CircleShape,
                                        color = Color.White
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.PlayArrow,
                                                contentDescription = "Play",
                                                tint = Color(0xFFE50914),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Red Gradient Banner Card
                    Surface(
                        modifier = Modifier
                            .width(280.dp)
                            .height(140.dp)
                            .clickable { onTemplateSelect("urban_sale") },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Unspecified
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFE50914), Color(0xFFFF5252))
                                    )
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text(
                                    text = "Edit Stories with\nBest Quality ✨",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Create now →",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Template Grid Section (When search/categories active)
                if (templates.isNotEmpty()) {
                    Text(
                        text = "All Templates (${templates.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181818),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    ) {
                        items(templates, key = { it.id }) { template ->
                            TemplateCard(
                                template = template,
                                onClick = { onTemplateSelect(template.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
