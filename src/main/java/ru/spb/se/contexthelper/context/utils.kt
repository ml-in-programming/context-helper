package ru.spb.se.contexthelper.context

import com.intellij.psi.*

/** Exception is thrown if the query can not be built because of the lack of the context. */
class NotEnoughContextException(message: String) : RuntimeException(message)

fun getRelevantTypeName(psiElement: PsiElement): String? =
    when (psiElement) {
        is PsiVariable -> {
            // PsiLocalVariable || PsiField || PsiParameter
            val psiTypeElement = psiElement.typeElement
            psiTypeElement?.innermostComponentReferenceElement?.qualifiedName
        }
        is PsiMethod -> {
            val psiReturnTypeElement = psiElement.returnType
            psiReturnTypeElement?.canonicalText
        }
        is PsiClass -> {
            psiElement.qualifiedName
        }
        else -> {
            null
        }
    }