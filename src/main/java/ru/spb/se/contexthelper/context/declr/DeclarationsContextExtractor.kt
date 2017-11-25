package ru.spb.se.contexthelper.context.declr

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState

/** Extracts all available declarations up to the given [PsiElement]. */
class DeclarationsContextExtractor(private val psiElement: PsiElement) {
    private val scopeProcessor = ContextPsiScopeProcessor()
    val context: DeclarationsContext

    init {
        extractContextFrom(psiElement, null)
        val declarations = scopeProcessor.declarations
        context = DeclarationsContext(declarations)
    }

    private fun extractContextFrom(currentPsiElement: PsiElement, lastParent: PsiElement?) {
        if (currentPsiElement !is PsiDirectory) {
            currentPsiElement.processDeclarations(
                scopeProcessor, ResolveState.initial(), lastParent, psiElement)
            scopeProcessor.upParentCounter()
            extractContextFrom(currentPsiElement.parent, currentPsiElement)
        }
    }
}