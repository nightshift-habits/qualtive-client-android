package io.qualtive.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.qualtive.Enquiry
import io.qualtive.Page
import io.qualtive.Qualtive
import io.qualtive.QualtiveException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val scope = rememberCoroutineScope()
                    var containerId by remember { mutableStateOf("ci-test") }
                    var enquiryId by remember { mutableStateOf("android") }
                    var previewToken by remember { mutableStateOf("") }
                    var loading by remember { mutableStateOf(false) }
                    var resultText by remember { mutableStateOf("Fetch an enquiry to see the parsed model.") }

                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .safeDrawingPadding()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Qualtive demo",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        OutlinedTextField(
                            value = containerId,
                            onValueChange = { containerId = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Container id") },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = enquiryId,
                            onValueChange = { enquiryId = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Enquiry id / slug") },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = previewToken,
                            onValueChange = { previewToken = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Preview token (optional)") },
                            singleLine = true,
                        )
                        Button(
                            onClick = {
                                if (loading) return@Button
                                loading = true
                                resultText = "Loading…"
                                scope.launch {
                                    resultText =
                                        try {
                                            val client =
                                                Qualtive(
                                                    context = this@MainActivity,
                                                    containerId = containerId.trim(),
                                                )
                                            val enquiry =
                                                client.fetchEnquiry(
                                                    enquiryId = enquiryId.trim(),
                                                    previewToken =
                                                        previewToken.trim().ifEmpty { null },
                                                )
                                            formatEnquiry(enquiry)
                                        } catch (error: QualtiveException.NotFound) {
                                            "Not found: ${error.message}"
                                        } catch (error: QualtiveException.Connection) {
                                            "Connection failed: ${error.message}"
                                        } catch (error: QualtiveException.RemoteMaintenance) {
                                            "Remote maintenance: ${error.message}"
                                        } catch (error: QualtiveException.Unexpected) {
                                            "Unexpected: ${error.message}"
                                        } catch (error: Throwable) {
                                            "Error: ${error.message ?: error::class.simpleName}"
                                        } finally {
                                            loading = false
                                        }
                                }
                            },
                            enabled = !loading,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (loading) "Fetching…" else "Fetch enquiry")
                        }
                        Text(
                            text = resultText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

private fun formatEnquiry(enquiry: Enquiry): String =
    buildString {
        appendLine("id: ${enquiry.id}")
        appendLine("slug: ${enquiry.slug}")
        appendLine("name: ${enquiry.name}")
        appendLine("contactRequired: ${enquiry.isUserContactDetailsRequired}")
        appendLine("container: ${enquiry.container.id} whiteLabel=${enquiry.container.isWhiteLabel} visibility=${enquiry.container.visibilityMode}")
        appendLine("theme: corner=${enquiry.theme.cornerStyle} background=${enquiry.theme.background} font=${enquiry.theme.font}")
        appendLine("pages: ${enquiry.pages.size}")
        enquiry.pages.forEachIndexed { pageIndex, page ->
            appendLine("  page[$pageIndex] content=${page.content.size}")
            page.content.forEachIndexed { contentIndex, content ->
                appendLine("    [$contentIndex] ${describePageContent(content)}")
            }
        }
        appendLine("submittedPages: ${enquiry.submittedPages.size}")
        enquiry.submittedPages.forEachIndexed { pageIndex, page ->
            appendLine("  submitted[$pageIndex] conditions=${page.conditions.size} content=${page.content.size}")
            page.conditions.forEachIndexed { conditionIndex, condition ->
                appendLine("    condition[$conditionIndex] $condition")
            }
            page.content.forEachIndexed { contentIndex, content ->
                appendLine("    content[$contentIndex] ${content::class.simpleName}")
            }
        }
        appendLine("entryContentTemplate: ${enquiry.entryContentTemplate().size} items")
    }

private fun describePageContent(content: Page.Content): String =
    when (content) {
        is Page.Content.Title -> "title text=${content.text}"
        is Page.Content.Body -> "body text=${content.text}"
        is Page.Content.Image -> "image url=${content.attachment.url}"
        is Page.Content.Score ->
            "score type=${content.scoreType} leading=${content.leadingText} trailing=${content.trailingText}"
        is Page.Content.Text ->
            "text placeholder=${content.placeholder} storage=${content.storageTarget}"
        is Page.Content.Select ->
            "select options=${content.options} custom=${content.allowsCustomInput}"
        is Page.Content.Multiselect -> "multiselect options=${content.options}"
        is Page.Content.Attachments -> "attachments"
        is Page.Content.ContactDetails ->
            "contactDetails title=${content.title} placeholder=${content.placeholder}"
    }
