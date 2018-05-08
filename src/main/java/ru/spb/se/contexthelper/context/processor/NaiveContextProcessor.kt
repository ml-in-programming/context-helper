package ru.spb.se.contexthelper.context.processor

import com.intellij.psi.PsiElement

class NaiveContextProcessor(
    initPsiElement: PsiElement
) : TextQueryContextProcessor(initPsiElement) {

    override fun generateTextQuery(): String {
        return "${psiElement.text} ${psiElement.language.displayName.toLowerCase()}"
    }
}