package com.github.restful.tool.linemarker;

import com.github.restful.tool.utils.Actions;
import com.github.restful.tool.utils.data.Bundle;
import com.github.restful.tool.utils.data.ProjectInfo;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;


public class SimpleLineMarkerProvider implements LineMarkerProvider {

	@Override
	public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
		if (element instanceof PsiMethod) {
			PsiMethod method = (PsiMethod) element;
			PsiIdentifier nameIdentifier = method.getNameIdentifier();

			if(null != ProjectInfo.get(method.getProject()).getApiServiceMap().get(method) && null != nameIdentifier) {
				return new LineMarkerInfo<>(nameIdentifier,
						nameIdentifier.getTextRange(),
						AllIcons.Toolwindows.WebToolWindow,
						v -> Bundle.getString("action.NavigateToView.text"),
						(e, m) -> {
							Actions.gotoApiServiceTree(method);
						},
						GutterIconRenderer.Alignment.LEFT);
			}

//			NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(AllIcons.Actions.Colors).
//					setTargets(method).
//					setTooltipText("test")
//					.;

//			return builder.createLineMarkerInfo(method);
		}
		return null;
	}
}