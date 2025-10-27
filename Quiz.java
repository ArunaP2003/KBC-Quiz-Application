package com.gqt.project;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Quiz extends JFrame {
    static Connection conn;

    static boolean used5050 = false;
    static boolean usedAudience = false;
    static boolean usedSkip = false;
    static boolean usedPhone = false;

    static int[] prizeMoney = {1000,2000,3000,5000,10000,20000,40000,80000,160000,320000};

    JLabel questionLabel;
    JButton optA, optB, optC, optD;
    JButton lifeline5050, lifelineAudience, lifelineSkip, lifelinePhone;
    JLabel moneyLadder[];

    ResultSet rs;
    Statement stmt;
    int qNo = 0;
    int guaranteed = 0;

    String correctOption = "";

    public Quiz() {
        setTitle("💰 KBC Quiz Game");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ===== Gradient Background Panel =====
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(128, 0, 128), getWidth(), getHeight(), new Color(75, 0, 130));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(mainPanel);

        // ===== Lifeline Panel (Top) =====
        JPanel lifelinePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        lifelinePanel.setOpaque(false);

        lifeline5050 = createLifelineButton("50-50");
        lifelineAudience = createLifelineButton("Audience Poll");
        lifelineSkip = createLifelineButton("Skip");
        lifelinePhone = createLifelineButton("Phone-a-Friend");

        lifelinePanel.add(lifeline5050);
        lifelinePanel.add(lifelineAudience);
        lifelinePanel.add(lifelineSkip);
        lifelinePanel.add(lifelinePhone);

        mainPanel.add(lifelinePanel, BorderLayout.NORTH);

        // ===== Question Panel =====
        JPanel questionPanel = new JPanel();
        questionPanel.setBackground(new Color(128, 0, 128, 180));
        questionPanel.setBorder(new LineBorder(Color.YELLOW, 3, true));
        questionPanel.setLayout(new BorderLayout());
        questionLabel = new JLabel("Question will appear here", SwingConstants.CENTER);
        questionLabel.setForeground(Color.WHITE);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        questionLabel.setBorder(new EmptyBorder(15, 15, 15, 15));
        questionPanel.add(questionLabel, BorderLayout.CENTER);
        mainPanel.add(questionPanel, BorderLayout.CENTER);

        // ===== Options Panel =====
        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        optionsPanel.setOpaque(false);

        optA = createOptionButton("A)");
        optB = createOptionButton("B)");
        optC = createOptionButton("C)");
        optD = createOptionButton("D)");

        optionsPanel.add(optA);
        optionsPanel.add(optB);
        optionsPanel.add(optC);
        optionsPanel.add(optD);

        mainPanel.add(optionsPanel, BorderLayout.SOUTH);

        // ===== Right Panel (Prize Ladder) =====
        JPanel rightPanel = new JPanel(new GridLayout(10, 1));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new LineBorder(Color.ORANGE, 2, true));
        moneyLadder = new JLabel[10];
        for (int i = 9; i >= 0; i--) {
            moneyLadder[i] = new JLabel("₹ " + prizeMoney[i], SwingConstants.CENTER);
            moneyLadder[i].setFont(new Font("Verdana", Font.BOLD, 16));
            moneyLadder[i].setForeground(Color.LIGHT_GRAY);
            rightPanel.add(moneyLadder[i]);
        }
        highlightPrize(0);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        // ===== Database connection =====
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizdb", "root", "Root");
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery("SELECT * FROM questions");
            loadNextQuestion();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }

        // ===== Button Listeners =====
        optA.addActionListener(e -> checkAnswer("A"));
        optB.addActionListener(e -> checkAnswer("B"));
        optC.addActionListener(e -> checkAnswer("C"));
        optD.addActionListener(e -> checkAnswer("D"));

        lifeline5050.addActionListener(e -> use5050());
        lifelineAudience.addActionListener(e -> useAudiencePoll());
        lifelineSkip.addActionListener(e -> useSkip());
        lifelinePhone.addActionListener(e -> usePhone());
    }

    private JButton createOptionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(75, 0, 130));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(Color.CYAN, 2, true));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createLifelineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(Color.BLACK);
        btn.setBackground(new Color(255, 215, 0));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(Color.ORANGE, 2, true));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void highlightPrize(int index) {
        for (int i = 0; i < moneyLadder.length; i++) {
            if (i == index) {
                moneyLadder[i].setForeground(Color.YELLOW);
                moneyLadder[i].setFont(new Font("Verdana", Font.BOLD, 18));
            } else {
                moneyLadder[i].setForeground(Color.LIGHT_GRAY);
                moneyLadder[i].setFont(new Font("Verdana", Font.BOLD, 16));
            }
        }
    }

    // ===== Load Current Question =====
    void loadNextQuestion() {
        try {
            if (rs.absolute(qNo + 1)) { // Move to correct row
                questionLabel.setText("Q" + (qNo + 1) + " for ₹" + prizeMoney[qNo] + ": " + rs.getString("question"));
                optA.setText("A) " + rs.getString("optionA"));
                optB.setText("B) " + rs.getString("optionB"));
                optC.setText("C) " + rs.getString("optionC"));
                optD.setText("D) " + rs.getString("optionD"));

                optA.setEnabled(true);
                optB.setEnabled(true);
                optC.setEnabled(true);
                optD.setEnabled(true);

                correctOption = rs.getString("correctOption");

                highlightPrize(qNo);
            } else {
                JOptionPane.showMessageDialog(this, "🎉 Congratulations! You won ₹" + prizeMoney[prizeMoney.length - 1]);
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void checkAnswer(String selected) {
        if (selected.equals(correctOption)) {
            JOptionPane.showMessageDialog(this, "✅ Correct! You won ₹" + prizeMoney[qNo]);
            qNo++; // increment AFTER correct answer
            loadNextQuestion();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Wrong Answer!");
            if (qNo >= 7) guaranteed = prizeMoney[6];
            else if (qNo >= 5) guaranteed = prizeMoney[4];
            else guaranteed = 0;
            JOptionPane.showMessageDialog(this, "💸 You take home ₹" + guaranteed);
            System.exit(0);
        }
    }

    void use5050() {
        if (used5050) {
            JOptionPane.showMessageDialog(this, "❌ 50-50 already used!");
            return;
        }
        java.util.List<JButton> opts = Arrays.asList(optA, optB, optC, optD);
        java.util.List<String> codes = Arrays.asList("A", "B", "C", "D");
        int correctIndex = codes.indexOf(correctOption);

        ArrayList<Integer> wrongs = new ArrayList<>();
        for (int i = 0; i < 4; i++) if (i != correctIndex) wrongs.add(i);
        Collections.shuffle(wrongs);

        opts.get(wrongs.get(0)).setEnabled(false);
        opts.get(wrongs.get(1)).setEnabled(false);

        JOptionPane.showMessageDialog(this, "👉 50-50 Lifeline Activated!");
        used5050 = true;
    }

    void useAudiencePoll() {
        if (usedAudience) {
            JOptionPane.showMessageDialog(this, "❌ Audience Poll already used!");
            return;
        }
        Random rand = new Random();
        int correct = 50 + rand.nextInt(31);
        int remaining = 100 - correct;
        int opt2 = rand.nextInt(remaining);
        int opt3 = rand.nextInt(remaining - opt2);
        int opt4 = remaining - opt2 - opt3;

        String pollResult = "";
        if (correctOption.equals("A"))
            pollResult = "A: " + correct + "%\nB: " + opt2 + "%\nC: " + opt3 + "%\nD: " + opt4 + "%";
        else if (correctOption.equals("B"))
            pollResult = "A: " + opt2 + "%\nB: " + correct + "%\nC: " + opt3 + "%\nD: " + opt4 + "%";
        else if (correctOption.equals("C"))
            pollResult = "A: " + opt2 + "%\nB: " + opt3 + "%\nC: " + correct + "%\nD: " + opt4 + "%";
        else
            pollResult = "A: " + opt2 + "%\nB: " + opt3 + "%\nC: " + opt4 + "%\nD: " + correct + "%";

        JOptionPane.showMessageDialog(this, "📊 Audience Poll:\n\n" + pollResult);
        usedAudience = true;
    }

    void useSkip() {
        if (usedSkip) {
            JOptionPane.showMessageDialog(this, "❌ Skip already used!");
            return;
        }
        JOptionPane.showMessageDialog(this, "👉 Skip Lifeline Activated! Moving to next question...");
        qNo++; // increment when skipping
        loadNextQuestion();
        usedSkip = true;
    }

    void usePhone() {
        if (usedPhone) {
            JOptionPane.showMessageDialog(this, "❌ Phone-a-Friend already used!");
            return;
        }
        JOptionPane.showMessageDialog(this, "📞 Your friend thinks option " + correctOption + " is correct!");
        usedPhone = true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Quiz().setVisible(true));
    }
}
