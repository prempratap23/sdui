package com.example.sduiapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val json = this.resources.openRawResource(R.raw.sample).bufferedReader().use { it.readText() }
        val gson = GsonBuilder()
            .registerTypeAdapter(Component::class.java, ComponentAdapter())
            .create()

        val screen = gson.fromJson(json, Screen::class.java)

        setContent {
            MaterialTheme {
                RenderScreen(screen)
            }
        }
    }
}

@Composable
fun RenderScreen(screen: Screen) {
    Column {
        screen.components.forEach { component ->
            when (component) {
                is Component.Carousel -> {
                    if (component.layout == "horizontal") {
                        HorizontalCarousel(component.items)
                    } else {
                        VerticalCarousel(component.items)
                    }
                }
                is Component.ListComponent -> {
                    ListComponent(component.items)
                }
            }
        }
    }
}

@Composable
fun ListComponent(items: List<ListItem>) {
    LazyColumn {
        items(items) { listItem ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
//                        listItem.action?.url?.let { navigateTo(it) }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                listItem.icon?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = listItem.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = listItem.subtitle, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
fun VerticalCarousel(items: List<Item>) {
    LazyColumn {
        items(items) { item ->
            Text(
                text = item.content ?: "",
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        item.action?.url?.let { navigateTo(it) }
                    }
            )
        }
    }
}

@Composable
fun HorizontalCarousel(items: List<Item>) {
    LazyRow {
        items(items) { item ->
            Image(
                painter = rememberAsyncImagePainter(item.url),
                contentDescription = item.description,
                modifier = Modifier
                    .padding(8.dp)
                    .size(150.dp)
                    .clickable {
                        item.action?.url?.let { navigateTo(it) }
                    }
            )
        }
    }
}

fun navigateTo(url: String) {
    // Handle navigation logic, e.g., open a WebView or navigate to a new screen
    Log.d("Navigation", "Navigating to $url")
}

data class Screen(
    val components: List<Component>
)

sealed class Component {
    data class Carousel(
        val layout: String,
        val items: List<Item>
    ) : Component()

    data class ListComponent(
        val items: List<ListItem>
    ) : Component()
}

data class Item(
    val type: String,
    val url: String? = null,
    val description: String? = null,
    val content: String? = null,
    val action: Action? = null
)

data class ListItem(
    val type: String,
    val title: String,
    val subtitle: String,
    val icon: String? = null,
    val action: Action? = null
)

data class Action(
    val type: String,
    val url: String
)

class ComponentAdapter : JsonDeserializer<Component> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Component {
        val jsonObject = json.asJsonObject
        val type = jsonObject["type"].asString

        return when (type) {
            "carousel" -> {
                val layout = jsonObject["layout"].asString
                val items = context.deserialize<List<Item>>(jsonObject["items"], object : TypeToken<List<Item>>() {}.type)
                Component.Carousel(layout, items)
            }
            "list" -> {
                val items = context.deserialize<List<ListItem>>(jsonObject["items"], object : TypeToken<List<ListItem>>() {}.type)
                Component.ListComponent(items)
            }
            else -> throw IllegalArgumentException("Unknown component type: $type")
        }
    }
}


