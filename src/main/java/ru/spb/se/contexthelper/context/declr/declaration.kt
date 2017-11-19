package ru.spb.se.contexthelper.context.declr

import com.intellij.psi.PsiElement

/** Represents an extracted declaration statement. */
data class Declaration(val psiElement: PsiElement, val parentLevel: Int)

/** Wrapper for a context based on available declarations. */
data class DeclarationsContext(val declarations: List<Declaration>)
