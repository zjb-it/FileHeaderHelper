package com.zjb.fileheader.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.zjb.fileheader.entity.Comment;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * @author zhaojingbo(zjbhnay @ 163.com)
 * @date 2020-11-24 11:16:22
 */
public class JavaDocUtils {

    /**
     * windows系统中System.lineSeparator()返回 \r\n报错
     */
    private static String LINE_SEPARATOR = "\n";
    /**
     * 增加类上注释
     *
     * @param psiClass
     * @param comment
     * @param factory
     */
    public static void addOrUpdateClassComment(PsiClass psiClass, Comment comment, PsiElementFactory factory, Project project) {
        StringBuffer javadoc = new StringBuffer();
        javadoc.append("/**");
        javadoc.append(LINE_SEPARATOR);

        javadoc.append("* @author ");
        javadoc.append(comment.getAuthor());
        javadoc.append(LINE_SEPARATOR);

        javadoc.append("* @date ");
        javadoc.append(comment.getDate());
        javadoc.append(LINE_SEPARATOR);

        List<String> others = comment.getOther();
        if (Objects.nonNull(others) && others.size() > 0) {
            for (String other : others) {
                if (!other.startsWith("*")) {
                    javadoc.append("* ");
                }
                javadoc.append(other);
                javadoc.append(LINE_SEPARATOR);
            }
        }

        javadoc.append("*/");
        PsiComment psiComment = factory.createCommentFromText(javadoc.toString(), null);
        if (psiClass.getDocComment() == null) {

            WriteCommandAction.writeCommandAction(project).run(() -> {
                psiClass.addBefore(psiComment, psiClass.getFirstChild());
            });
        } else {
            WriteCommandAction.writeCommandAction(project).run(() -> {
                psiClass.getDocComment().replace(psiComment);
            });

        }
    }

    /**
     * 获取原注释里的date
     * @param docComment
     * @return 如果原注释里没有date，则返回null
     */
    public static String getOriginDate(PsiDocComment docComment) {
        if (Objects.isNull(docComment)) {
            return null;
        }
        PsiDocTag dateTag = docComment.findTagByName("date");
        if (Objects.isNull(dateTag)) {
            return null;
        }
        String date = dateTag.getText();
        if (date.contains("*")) {
            date = StringUtils.left(date, date.indexOf("*"));
        }
        return date.trim().replace("@date", "").replace(LINE_SEPARATOR, "");
    }

    public static String getOriginAuthor(PsiDocComment docComment) {
        if (Objects.isNull(docComment)) {
            return null;
        }
        PsiDocTag authorTag = docComment.findTagByName("author");
        if (Objects.isNull(authorTag)) {
            return null;
        }
        String author = authorTag.getText();
        if (author.contains("*")) {
            author = StringUtils.left(author, author.indexOf("*"));
        }
        return author.trim().replace("@author", "").replace(LINE_SEPARATOR, "");
    }

    public static String getClassCreateTime(PsiFile psiFile ,String datePattern) {
        Path path = Paths.get(psiFile.getVirtualFile().getCanonicalPath());
        BasicFileAttributeView fileAttributeView = Files.getFileAttributeView(path, BasicFileAttributeView.class);
        try {
            long createTime = fileAttributeView.readAttributes().creationTime().toMillis();
            return new SimpleDateFormat(datePattern).format(new Date(createTime));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SimpleDateFormat(datePattern).format(new Date());
    }

}
