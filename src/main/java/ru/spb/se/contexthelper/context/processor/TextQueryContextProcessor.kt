package ru.spb.se.contexthelper.context.processor

import com.intellij.psi.PsiElement

abstract class TextQueryContextProcessor(
    initPsiElement: PsiElement
) : AbstractContextProcessor(initPsiElement) {
    abstract fun generateTextQuery(): String
}