package ru.spb.se.contexthelper.context.processor

import com.intellij.psi.PsiElement

/** Base class for implementing ContextProcessors. */
abstract class AbstractContextProcessor(initPsiElement: PsiElement) {
    protected val psiElement: PsiElement = findNonPunctuationElement(initPsiElement)

    private fun findNonPunctuationElement(psiElement: PsiElement): PsiElement {
        val isPunctuation = psiElement.text.chars().noneMatch(Character::isLetterOrDigit)
        if (!isPunctuation) {
            return psiElement
        }
        val prevPsiElement = psiElement.prevSibling
        return if (prevPsiElement != null) findNonPunctuationElement(prevPsiElement) else psiElement
    }
}