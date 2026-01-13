package digital.euforia.app.domain.model.article

sealed interface ArticleBlock {
    data class H1(val text: String) : ArticleBlock
    data class H2(val text: String) : ArticleBlock
    data class H3(val text: String) : ArticleBlock
    data class Paragraph(val text: String) : ArticleBlock
    data class Bullet(val text: String) : ArticleBlock
    data class Image(val url: String, val alt: String?) : ArticleBlock
}