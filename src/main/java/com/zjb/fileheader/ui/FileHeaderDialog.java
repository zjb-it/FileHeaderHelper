package com.zjb.fileheader.ui;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.zjb.fileheader.FileHeader;
import com.zjb.fileheader.entity.Comment;
import com.zjb.fileheader.util.JavaDocUtils;
import com.zjb.fileheader.util.PsiClassUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.zjb.fileheader.util.JavaDocUtils.*;


/**
 * @author zhaojingbo(zjbhnay @ 163.com)
 * @date 2020-11-24 15:42:19
 */
public class FileHeaderDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JRadioButton originAuthorRadio;
    private JRadioButton newAuthorRadio;
    private JTextField newAuthor;
    private JRadioButton appendHeader;
    private JRadioButton newHeader;
    private JTextArea otherHeader;
    private JRadioButton originDateRadio;
    private JRadioButton fileCreateTime;
    private JTextField fileDatePattern;
    private JRadioButton noneOtherHeader;

    private Project project;
    private PsiFile psiFile;
    private PsiElementFactory psiElementFactory;

    public FileHeaderDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);


        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        newAuthorRadio.addItemListener(event -> {
            if (newAuthorRadio.isSelected()) {
                newAuthor.setEnabled(true);
            } else {
                newAuthor.setText("");
                newAuthor.setEnabled(false);
            }
        });
        fileCreateTime.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (fileCreateTime.isSelected()) {
                    fileDatePattern.setEnabled(true);
                } else {
                    fileDatePattern.setEnabled(false);
                    newAuthor.setText("yyyy-MM-dd HH:mm:ss");
                }
            }
        });
        noneOtherHeader.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    otherHeader.setText("");
                    otherHeader.setEnabled(true);
                } else {
                    otherHeader.setEnabled(false);
                    otherHeader.setText("多行之间以;分隔");
                }
            }
        });
    }

    private void onOK() {
        List<PsiClass> psiClasses = PsiClassUtil.listAllClasses(psiFile);
        for (PsiClass psiClass : psiClasses) {
            JavaDocUtils.addOrUpdateClassComment(psiClass, new Comment(getAuthor(psiClass), getDate(psiClass), getOther(psiClass)), psiElementFactory, project);
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    /**
     * 获取author
     *
     * @param psiClass
     * @return
     */
    private String getAuthor(PsiClass psiClass) {
        if (originAuthorRadio.isSelected()) {
            String originAuthor = getOriginAuthor(psiClass.getDocComment());
            if (Objects.nonNull(originAuthor)) {
                return originAuthor;
            }
            return SystemUtils.USER_NAME;
        }
        if (StringUtils.isBlank(newAuthor.getText())) {
            Messages.showErrorDialog(project, "替换author不能为空", FileHeader.TITLE);
            throw new RuntimeException("替换author不能为空");
        }
        return newAuthor.getText();
    }

    /**
     * 获取date
     *
     * @param psiClass
     * @return
     */
    private String getDate(PsiClass psiClass) {
        if (originDateRadio.isSelected()) {
            String originDate = getOriginDate(psiClass.getDocComment());
            if (Objects.nonNull(originDate)) {
                return originDate;
            }
        }
        if (StringUtils.isBlank(fileDatePattern.getText())) {
            Messages.showErrorDialog(project, "date pattern不能为空", FileHeader.TITLE);
            throw new RuntimeException("date pattern不能为空");
        }
        return getClassCreateTime(psiClass.getContainingFile(), fileDatePattern.getText());
    }

    /**
     * 获取other
     *
     * @param psiClass
     * @return
     */
    private List<String> getOther(PsiClass psiClass) {
        //如果清空原来的其它header
        if (noneOtherHeader.isSelected()) {
            return Collections.emptyList();
        }
        String otherHeaderText = otherHeader.getText();
        //如果替换原来的其它header
        if (newHeader.isSelected()) {
            if (StringUtils.isNotBlank(otherHeaderText)) {
                return Lists.newArrayList(otherHeaderText.split(";"));
            }
            return Collections.emptyList();
        }
        //如果在原来的其它header后面append
        PsiDocComment docComment = psiClass.getDocComment();

        List<String> originOtherHeader = getOriginOtherHeader(docComment);
        if (StringUtils.isNotBlank(otherHeaderText)) {
            originOtherHeader.addAll(Lists.newArrayList(otherHeaderText.split(";")));
        }
        return originOtherHeader;
    }

    /**
     * 获取原来的other header
     * @param docComment
     * @return
     */
    private List<String> getOriginOtherHeader(PsiDocComment docComment) {
        if (Objects.isNull(docComment)) {
            return Lists.newArrayList();
        }
        PsiDocTag[] tags = docComment.getTags();
        if (tags.length < 1) {
            return Lists.newArrayList(docComment.getText().split(";"));
        }
        PsiDocTag tag = tags[tags.length - 1];
        String text = tag.getText();
        String[] split = text.trim().split("\n");
        ArrayList<String> strings = Lists.newArrayList(split);
        strings.remove(0);
        return strings;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setPsiFile(PsiFile psiFile) {
        this.psiFile = psiFile;
    }

    public void setPsiElementFactory(PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
    }
}
