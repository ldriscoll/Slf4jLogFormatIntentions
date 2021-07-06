package com.lukedriscoll.slf4jlogformat.intention;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiPolyadicExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiConcatenationUtil;
import com.lukedriscoll.slf4jlogformat.Slf4jLogFormatBundle;
import com.siyeh.ig.PsiReplacementUtil;
import com.siyeh.ipp.base.Intention;
import com.siyeh.ipp.base.PsiElementPredicate;

public class ReplaceConcatenationWithSlf4jIntention extends Intention {

  @Override
  protected void processIntention(@NotNull PsiElement psiElement) {
    System.out.println("Running Method is " + psiElement.toString());
    // we need to take the first argument and convert that into the right format, then add the second argument, if that exists
    PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) psiElement;
    final PsiReferenceExpression methodExpression = methodCallExpression.getMethodExpression();

    final List<PsiExpression> formatParameters = new ArrayList();
    String formatString = PsiConcatenationUtil.buildUnescapedFormatString(
            methodCallExpression.getArgumentList().getExpressions()[0],
            false,
            formatParameters);
    formatString = StringEscapeUtils.escapeJava(formatString);
    formatString = formatString.replaceAll("\\{\\d+\\}", "{}");
    formatString = formatString.replaceAll("''", "'");

    StringBuilder builder = new StringBuilder();
    final PsiExpression qualifier = methodExpression.getQualifierExpression();
    if (qualifier != null) {
      builder.append(qualifier.getText());
      builder.append('.');
    }
    builder.append(methodExpression.getReferenceName());
    builder.append("(\"");
    builder.append(formatString);
    builder.append('\"');
    for (PsiExpression formatParameter : formatParameters) {
      builder.append(", ");
      builder.append(formatParameter.getText());
    }
    if (methodCallExpression.getArgumentList().getExpressions().length == 2) {
      builder.append(", ");
      builder.append(methodCallExpression.getArgumentList().getExpressions()[1].getText());
    }
    builder.append(')');
    System.out.println("Builder is " + builder);
    PsiReplacementUtil.replaceExpression(methodCallExpression, builder.toString());
  }

  @NotNull
  @Override
  protected PsiElementPredicate getElementPredicate() {
    return new PsiElementPredicate() {
      @Override
      public boolean satisfiedBy(PsiElement psiElement) {
        if (!(psiElement instanceof PsiMethodCallExpression)) {
          return false;
        }

        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) psiElement;
        PsiMethod method = methodCallExpression.resolveMethod();

        if (method == null) {
          return false;
        }

        if (!PsiType.VOID.equals(method.getReturnType())) {
          return false;
        }

        PsiClass ownerClass = method.getContainingClass();
        if (ownerClass == null) {
          return false;
        }

        if (!Logger.class.getName().equals(ownerClass.getQualifiedName())) {
          return false;
        }

        int parameterCount = methodCallExpression.getArgumentList().getExpressions().length;
        if (parameterCount == 0) {
          return false;
        }
        if (parameterCount > 2) {
          return false;
        }

        if (parameterCount == 2
            && !PsiType.getJavaLangThrowable(psiElement.getManager(), psiElement.getResolveScope()).isAssignableFrom(method.getParameterList().getParameters()[1].getType())) {
          return false;
        }

        if (!(methodCallExpression.getArgumentList().getExpressions()[0] instanceof PsiPolyadicExpression)) {
          return false;
        }

        return true;
      }
    };
  }

  @NotNull
  @Override
  public String getText() {
    return Slf4jLogFormatBundle.message("replace.concatenation.with.slf4j.format.intention.name");
  }

  @Nls
  @NotNull
  @Override
  public String getFamilyName() {
    return Slf4jLogFormatBundle.defaultableMessage("replace.concatenation.with.slf4j.format.intention.family.name");
  }
}
