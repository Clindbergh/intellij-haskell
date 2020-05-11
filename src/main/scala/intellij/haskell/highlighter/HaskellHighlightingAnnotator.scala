package intellij.haskell.highlighter

import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.psi.PsiElement
import intellij.haskell.psi._
import intellij.haskell.psi.impl.HaskellStringLiteralElementImpl

class HaskellHighlightingAnnotator extends Annotator {
  override def annotate(element: PsiElement, holder: AnnotationHolder): Unit = {
    element match {
      case psi: HaskellImportQualified => holder.createInfoAnnotation(psi, null)
        .setTextAttributes(HaskellSyntaxHighlighter.Keyword)
      case psi: HaskellImportQualifiedAs => holder.createInfoAnnotation(psi.getFirstChild, null)
        .setTextAttributes(HaskellSyntaxHighlighter.Keyword)
      case psi: HaskellImportHiding => holder.createInfoAnnotation(psi, null)
        .setTextAttributes(HaskellSyntaxHighlighter.Keyword)
      case psi: HaskellPragma => holder.createInfoAnnotation(psi, null).setTextAttributes(HaskellSyntaxHighlighter.PragmaContent)
        Option(psi.getFirstChild).foreach(c => holder.createInfoAnnotation(c, null).setTextAttributes(HaskellSyntaxHighlighter.Pragma))
        Option(psi.getLastChild).map(_.getPrevSibling).foreach(c => holder.createInfoAnnotation(c, null).setTextAttributes(HaskellSyntaxHighlighter.Pragma))
      case psi: HaskellTypeSignature => holder.createInfoAnnotation(psi.getFirstChild, null)
        .setTextAttributes(HaskellSyntaxHighlighter.FunctionName)
      case psi: HaskellStringLiteralElementImpl => holder.createInfoAnnotation(psi, null)
        .setTextAttributes(HaskellSyntaxHighlighter.String)
      case _ =>
    }
  }
}
