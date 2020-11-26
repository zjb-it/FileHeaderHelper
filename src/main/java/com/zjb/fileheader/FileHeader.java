/**
 * @author zjb111
 * @author zjb
 */ /**
 * @author zjb
 */
package com.zjb.fileheader;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.zjb.fileheader.ui.FileHeaderDialog;

/**
 * @author zhaojingbo(zjbhnay @ 163.com)
 * @date 2020-11-20 10:26:01
 */
public class FileHeader extends AnAction {
    public static final String TITLE = "FileHeaderHelper";

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        PsiElementFactory instance = PsiElementFactory.getInstance(project);
        FileHeaderDialog helper = new FileHeaderDialog();
        helper.setProject(project);
        helper.setPsiFile(psiFile);
        helper.setPsiElementFactory(instance);
        helper.setSize(550, 350);
        helper.setLocationRelativeTo(null);
        helper.setTitle(TITLE);
        helper.setVisible(true);
    }


}
