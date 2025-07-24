import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    private static final String STUDENTS_FILE = "Data/students.csv";
    private static final String ATTENDANCE_FILE = "Data/attendance.csv";
    private static final String PAYMENTS_FILE = "Data/payments.csv";
    private int studentIdCounter = 1000;
    private Map<String, String> studentMap = new HashMap<>(); // ID -> Name (for dropdown)
    private JComboBox<String> studentDropdown;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Indian Classical Dance Class Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        loadStudentMap();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Student Registration", createStudentPanel());
        tabbedPane.addTab("Attendance Entry", createAttendancePanel());
        tabbedPane.addTab("Payment Entry", createPaymentPanel());
        tabbedPane.addTab("View Reports", createReportPanel());
        tabbedPane.addTab("Class Schedules", createSchedulePanel());
        tabbedPane.addTab("Instructor Info", createInstructorPanel());
        tabbedPane.addTab("Student Progress", createProgressPanel());

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private void loadStudentMap() {
        studentMap.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(STUDENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    studentMap.put(parts[0], parts[1] + " (" + parts[0] + ") - " + parts[3]);
                }
            }
        } catch (IOException e) {
            // skip loading if file doesn't exist yet
        }
    }

    private void refreshStudentDropdown() {
        if (studentDropdown != null) {
            loadStudentMap();
            studentDropdown.removeAllItems();
            for (String value : studentMap.values()) {
                studentDropdown.addItem(value);
            }
        }
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Register New Student"));

        JTextField nameField = new JTextField();
        JTextField ageField = new JTextField();
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced"});
        JTextField idField = new JTextField("[Auto-generated]");
        idField.setEditable(false);

        JButton registerBtn = new JButton("Register Student");
        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String age = ageField.getText().trim();
            String category = (String) categoryBox.getSelectedItem();
            String id = String.valueOf(studentIdCounter++);
            idField.setText(id);

            try (PrintWriter out = new PrintWriter(new FileWriter(STUDENTS_FILE, true))) {
                out.println(id + "," + name + "," + age + "," + category);
                studentMap.put(id, name + " (" + id + ") - " + category);
                refreshStudentDropdown();
                JOptionPane.showMessageDialog(panel, "Student registered with ID: " + id);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(new JLabel("Full Name:")); panel.add(nameField);
        panel.add(new JLabel("Age:")); panel.add(ageField);
        panel.add(new JLabel("Category:")); panel.add(categoryBox);
        panel.add(new JLabel("Generated ID:")); panel.add(idField);
        panel.add(new JLabel("")); panel.add(registerBtn);

        return panel;
    }

    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Log Attendance"));

        studentDropdown = new JComboBox<>(studentMap.values().toArray(new String[0]));
        studentDropdown.setEditable(true);
        JTextField editor = (JTextField) studentDropdown.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String input = editor.getText();
                studentDropdown.removeAllItems();
                for (String item : studentMap.values()) {
                    if (item.toLowerCase().contains(input.toLowerCase())) {
                        studentDropdown.addItem(item);
                    }
                }
                editor.setText(input);
                studentDropdown.showPopup();
            }
        });

        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Present", "Absent"});
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);

        JButton markBtn = new JButton("Submit Attendance");
        markBtn.addActionListener(e -> {
            String selected = (String) studentDropdown.getSelectedItem();
            if (selected == null || !selected.contains("(")) {
                JOptionPane.showMessageDialog(panel, "Invalid student selection.");
                return;
            }

            String id = selected.substring(selected.indexOf("(") + 1, selected.indexOf(")"));
            String name = selected.substring(0, selected.indexOf(" ("));
            String category = selected.substring(selected.lastIndexOf("-") + 2);
            String status = (String) statusBox.getSelectedItem();
            String date = new SimpleDateFormat("yyyy-MM-dd").format(dateSpinner.getValue());

            try (PrintWriter out = new PrintWriter(new FileWriter(ATTENDANCE_FILE, true))) {
                out.println(id + "," + name + "," + category + "," + date + "," + status);
                JOptionPane.showMessageDialog(panel, "Attendance marked.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(new JLabel("Student (Name/ID - Category):")); panel.add(studentDropdown);
        panel.add(new JLabel("Status:")); panel.add(statusBox);
        panel.add(new JLabel("Date:")); panel.add(dateSpinner);
        panel.add(new JLabel("")); panel.add(markBtn);

        return panel;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Log Payment"));

        JTextField idField = new JTextField();
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced"});
        JTextField amountField = new JTextField();
        JTextField dateField = new JTextField("YYYY-MM-DD");

        JButton payBtn = new JButton("Record Payment");
        payBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String category = (String) categoryBox.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText().trim());
            String date = dateField.getText().trim();

            double required = category.equals("Beginner") ? 100 : category.equals("Intermediate") ? 150 : 200;
            if (amount < required) {
                JOptionPane.showMessageDialog(panel, "Amount paid is less than required: $" + required);
            } else {
                try (PrintWriter out = new PrintWriter(new FileWriter(PAYMENTS_FILE, true))) {
                    out.println(id + "," + category + "," + amount + "," + date);
                    JOptionPane.showMessageDialog(panel, "Payment recorded.");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        panel.add(new JLabel("Student ID:")); panel.add(idField);
        panel.add(new JLabel("Category:")); panel.add(categoryBox);
        panel.add(new JLabel("Amount Paid:")); panel.add(amountField);
        panel.add(new JLabel("Date:")); panel.add(dateField);
        panel.add(new JLabel("")); panel.add(payBtn);

        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Export or View Reports"));
        JTextArea reportArea = new JTextArea("Reports will be shown here...");
        JButton exportBtn = new JButton("Export to CSV");

        exportBtn.addActionListener(e -> reportArea.setText("Report export feature coming soon..."));

        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        panel.add(exportBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Weekly Class Schedule"));
        JTextArea scheduleArea = new JTextArea(
                "Beginner: Saturdays 10 AM\n" +
                        "Intermediate: Saturdays 12 PM\n" +
                        "Advanced: Sundays 10 AM"
        );
        panel.add(new JScrollPane(scheduleArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInstructorPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.setBorder(BorderFactory.createTitledBorder("Instructor Information"));
        panel.add(new JLabel("Beginner Instructor: Ms. Nandini Rao"));
        panel.add(new JLabel("Intermediate Instructor: Mr. Rajeev Iyer"));
        panel.add(new JLabel("Advanced Instructor: Dr. Meera Krishnan"));
        return panel;
    }

    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Track Student Progress"));
        JTextArea progressArea = new JTextArea("Enter progress notes here...");
        panel.add(new JScrollPane(progressArea), BorderLayout.CENTER);
        panel.add(new JButton("Save Notes"), BorderLayout.SOUTH);
        return panel;
    }
}


