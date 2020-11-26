package com.zjb.fileheader.util;

import com.google.common.collect.Lists;
import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaojingbo(zjbhnay @ 163.com)
 * @date 2020-11-24 18:55:37
 */
public class PsiClassUtil {
    /**
     * 获取项目中的所有class
     * @param psiFile 项目中的任意文件
     * @return 项目中的所有class
     */
    public static List<PsiClass> listAllClasses(PsiFile psiFile) {
        PsiDirectory directory = psiFile.getContainingDirectory();
        PsiDirectory rootDir = getRootDir(directory);
        return recursionDirectory(rootDir);
    }

    /**
     * 递归获取根目录（src）
     * @param directory 任意directory
     * @return src根目录
     */
    private static PsiDirectory getRootDir(PsiDirectory directory) {
        if (directory.toString().endsWith("src")) {
            return directory;
        }
        return getRootDir(directory.getParent());
    }

    /**
     *  从根目录向下查所有的Class (当前目录的class+子目录的class
     * @param rootDirectory 根目录
     * @return 所有class
     */
    private static List<PsiClass> recursionDirectory(PsiDirectory rootDirectory) {
        List<PsiClass> currDirClasses = listCurrDirPsiClasses(rootDirectory);
        PsiElement[] children = rootDirectory.getChildren();
        if (children.length < 1) {
            return Lists.newArrayList();
        }
        currDirClasses.addAll(listChildrenClass(children));
        return currDirClasses;

    }

    /**
     * 当前directory的class
     * @param directory
     * @return 当前directory的class
     */
    private static List<PsiClass> listCurrDirPsiClasses(PsiDirectory directory){
        PsiClass[] classes = JavaDirectoryService.getInstance().getClasses(directory);
        return Lists.newArrayList(classes);
    }

    /**
     * 获取PsiElement的所有class
     * @param psiElements
     * @return
     */
    private static List<PsiClass> listChildrenClass(PsiElement[] psiElements) {
        if (psiElements.length < 1) {
            return Lists.newArrayList();
        }
        ArrayList<PsiClass> result = Lists.newArrayList();
        for (PsiElement psiElement : psiElements) {
            if (psiElement instanceof PsiDirectory) {
                result.addAll(recursionDirectory((PsiDirectory) psiElement));
            }
        }
        return result;
    }
}
