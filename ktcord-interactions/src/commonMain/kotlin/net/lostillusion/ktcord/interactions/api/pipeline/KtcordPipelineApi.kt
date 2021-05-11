package net.lostillusion.ktcord.interactions.api.pipeline

/**
 * This annotation represents its targeted type is part of the pipeline api which exposes the underlying pipeline of
 * processing requests from Discord. This api provides direct access to the call coming from Discord and if used
 * incorrectly, it may cause incorrect processing of the call and therefore causing a failed interaction.
 */
@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS, AnnotationTarget.PROPERTY)
annotation class KtcordPipelineApi