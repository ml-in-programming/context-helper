package ru.spb.se.contexthelper.context.processor

import com.intellij.psi.PsiElement

class GCSNaiveContextProcessor(
    initPsiElement: PsiElement) : AbstractContextProcessor(initPsiElement) {
    fun generateQuery(): String {
        return "${psiElement.text} ${psiElement.language.displayName.toLowerCase()}"
    }
}