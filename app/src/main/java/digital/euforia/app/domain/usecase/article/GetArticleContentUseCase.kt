package digital.euforia.app.domain.usecase.article

import digital.euforia.app.data.repository.ArticleRepository
import digital.euforia.app.domain.model.article.ArticleBlock
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetArticleContentUseCase @Inject constructor(
    private val articleRepository: ArticleRepository
) {
    suspend operator fun invoke(articleId: Int): ResultWrapper<List<ArticleBlock>> {
        return withContext(Dispatchers.IO) {
            articleRepository.getArticleContent(articleId).map { content ->
                parseContentToBlocks(content)
            }
        }
    }

    private val imageRegex = Regex("""!\[(.*?)\]\((.*?)\)""") // ![alt](url)
    private val linkRegex = Regex("""\[(.+?)\]\((.+?)\)""")  // [text](url)

    fun parseContentToBlocks(content: String): List<ArticleBlock> {
        val lines = content.replace("\r\n", "\n").split("\n")

        val blocks = mutableListOf<ArticleBlock>()
        val paragraphBuf = StringBuilder()

        fun flushParagraph() {
            val text = paragraphBuf.toString().trim()
            if (text.isNotEmpty()) blocks += ArticleBlock.Paragraph(text)
            paragraphBuf.clear()
        }

        for (raw in lines) {
            val line = raw.trim()

            if (line.isBlank()) {
                flushParagraph()
                continue
            }

            // Image line
            val img = imageRegex.matchEntire(line)
            if (img != null) {
                flushParagraph()
                val alt = img.groupValues[1].takeIf { it.isNotBlank() }
                val url = img.groupValues[2]
                blocks += ArticleBlock.Image(url = url, alt = alt)
                continue
            }

            when {
                line.startsWith("# ") -> {
                    flushParagraph(); blocks += ArticleBlock.H1(line.removePrefix("# ").trim())
                }

                line.startsWith("## ") -> {
                    flushParagraph(); blocks += ArticleBlock.H2(line.removePrefix("## ").trim())
                }

                line.startsWith("### ") -> {
                    flushParagraph(); blocks += ArticleBlock.H3(line.removePrefix("### ").trim())
                }

                line.startsWith("- ") -> {
                    flushParagraph(); blocks += ArticleBlock.Bullet(line.removePrefix("- ").trim())
                }

                line.startsWith("✔") -> {
                    flushParagraph(); blocks += ArticleBlock.Bullet(line.trimStart('✔', '️', ' '))
                }

                else -> {
                    if (paragraphBuf.isNotEmpty()) paragraphBuf.append('\n')
                    paragraphBuf.append(line)
                }
            }
        }

        flushParagraph()
        return blocks
    }

}