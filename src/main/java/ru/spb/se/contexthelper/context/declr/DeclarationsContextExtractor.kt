package ru.spb.se.contexthelper.context.declr

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState

class DeclarationsContextExtractor {
    fun extractContextFrom(psiElement: PsiElement): DeclarationsContext {
        val scopeProcessor = ContextPsiScopeProcessor()
        var currentPsiElement = psiElement
        var previousPsiElement = psiElement
        while (currentPsiElement !is PsiDirectory) {
            currentPsiElement.processDeclarations(
                scopeProcessor, ResolveState.initial(), previousPsiElement, psiElement)
            previousPsiElement = currentPsiElement
            currentPsiElement = currentPsiElement.parent
        }
        val declarations = scopeProcessor.declarations
        return DeclarationsContext(declarations)
    }
}