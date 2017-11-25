package ru.spb.se.contexthelper.context

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.*

/** Exception is thrown if the query can not be built because of the lack of the context. */
class NotEnoughContextException(message: String) : RuntimeException(message)

private val logger = Logger.getInstance(
    "ru.spb.se.contexthelper.component.ContextHelperProjectComponent")

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
        is PsiPackage -> {
            // Currently omitting PsiPackage for the query building.
            null
        }
        else -> {
            logger.warn("getRelevantTypeName called with unexpected psiElement $psiElement")
            null
        }
    }