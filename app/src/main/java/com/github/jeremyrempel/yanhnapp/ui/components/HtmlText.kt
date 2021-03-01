package com.github.jeremyrempel.yanhnapp.ui.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import org.intellij.lang.annotations.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor

@Composable
fun HtmlText(html: String, handleLink: (String) -> Unit) {

    val bold = SpanStyle(fontWeight = FontWeight.Bold)
    val italic = SpanStyle(fontStyle = FontStyle.Italic)
    val underline = SpanStyle(textDecoration = TextDecoration.Underline)
    val link = SpanStyle(
        textDecoration = TextDecoration.Underline,
        color = MaterialTheme.colors.primaryVariant
    )

    val paragraph = ParagraphStyle()

    val formattedString = remember(html) {
        buildAnnotatedString {
            var cursorPosition = 0
            val appendAndUpdateCursor: (String) -> Unit = {
                append(it)
                cursorPosition += it.length
            }

            val doc = Jsoup.parse(html)

            doc.traverse(object : NodeVisitor {
                override fun head(node: Node, depth: Int) {
                    when (node) {
                        is Element -> {
                            when (node.tagName()) {
                                "b" -> pushStyle(bold)
                                "i" -> pushStyle(italic)
                                "u" -> pushStyle(underline)
                                "br" -> appendAndUpdateCursor("\n")
                                "p" -> {
                                    pushStyle(paragraph)

                                    if (cursorPosition > 0) {
                                        appendAndUpdateCursor("\n")
                                    }
                                }
                                "a" -> {
                                    val start = cursorPosition
                                    val end = start + node.text().length
                                    val href = node.attr("href")

                                    addStringAnnotation(
                                        tag = "link",
                                        start = start,
                                        end = end,
                                        annotation = href
                                    )
                                    pushStyle(link)
                                }
                            }
                        }
                        is TextNode -> {
                            if (node.text().isNotBlank()) {
                                appendAndUpdateCursor(node.text())
                            }
                        }
                        else -> {
                            throw Exception("Unknown node type")
                        }
                    }
                }

                override fun tail(node: Node, depth: Int) {
                    if (node is Element) {
                        when (node.tagName()) {
                            "b", "i", "u", "a" -> pop()
                            "p" -> {
                                pop()
                            }
                        }
                    }
                }
            })
        }
    }

    ClickableText(
        formattedString,
        style = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onSurface),
        onClick = { offset ->
            formattedString
                .getStringAnnotations(start = offset, end = offset)
                .firstOrNull { it.tag == "link" }
                ?.let { annotation ->
                    handleLink(annotation.item)
                }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewHtmlText() {

    @Language("HTML")
    val testHtml = """
<p>
    Hello <b><i><u>World</u></i></b>
</p>

<p>
    I&#x27;m Paragraph 2<br/>
    <a href="https://google.com">link to google</a>
</p>

<p>paragraph 3. <a href="https://yahoo.com">link to yahoo</a></p>
    """.trimIndent()

    HtmlText(testHtml) {
        println(it)
    }
}
