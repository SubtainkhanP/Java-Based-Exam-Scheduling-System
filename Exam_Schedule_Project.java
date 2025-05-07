package Exam_Schedule_Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Exam_Schedule_Project extends JFrame {
    private JTextField subjectField, sessionField, studentsField, searchField;
    private JTextArea subjectListArea;
    private JTable examTable;
    private DefaultTableModel tableModel;
    private Map<String, Integer>[] subjectsPerSession;
    private ExamScheduler scheduler;

    public Exam_Schedule_Project() {
        subjectsPerSession = new HashMap[4];
        for (int i = 0; i < 4; i++) {
            subjectsPerSession[i] = new HashMap<>();
        }

        tableModel = new DefaultTableModel(new String[]{"Year", "Subject", "Rooms", "Time", "Date"}, 0);
        scheduler = new ExamScheduler(tableModel);

        setTitle("University Exam Scheduling System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = createInputPanel();
        JPanel outputPanel = createOutputPanel();
        JPanel controlPanel = createControlPanel();

        add(inputPanel, BorderLayout.WEST);
        add(outputPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.NORTH);
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE, 2), "Exam Scheduling", TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), Color.BLUE));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        subjectField = new JTextField(15);
        sessionField = new JTextField(15);
        studentsField = new JTextField(15);
        subjectListArea = new JTextArea(10, 20);
        subjectListArea.setEditable(false);
        subjectListArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(subjectField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Year (2021-2024):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(sessionField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Number of Students:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(studentsField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        inputPanel.add(new JLabel("Subjects List:"), gbc);
        gbc.gridy = 4;
        inputPanel.add(new JScrollPane(subjectListArea), gbc);

        JButton addSubjectButton = new JButton("Add Subject");
        JButton scheduleButton = new JButton("Schedule Exams");

        addSubjectButton.setBackground(Color.GREEN);
        scheduleButton.setBackground(Color.CYAN);

        gbc.gridy = 5; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(addSubjectButton, gbc);
        gbc.gridx = 1;
        inputPanel.add(scheduleButton, gbc);

        addSubjectButton.addActionListener(e -> addSubject());
        scheduleButton.addActionListener(e -> scheduleExams());

        return inputPanel;
    }

    private JPanel createOutputPanel() {
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE, 2), "Exam Schedule", TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), Color.BLUE));

        examTable = new JTable(tableModel);
        examTable.setBackground(new Color(240, 248, 255));
        examTable.setForeground(Color.BLACK);
        examTable.setFont(new Font("Serif", Font.PLAIN, 14));
        examTable.getTableHeader().setFont(new Font("Serif", Font.BOLD, 14));
        examTable.getTableHeader().setBackground(new Color(30, 144, 255));
        examTable.getTableHeader().setForeground(Color.WHITE);

        JPopupMenu popupMenu = createPopupMenu();
        examTable.setComponentPopupMenu(popupMenu);

        outputPanel.add(new JScrollPane(examTable), BorderLayout.CENTER);
        return outputPanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Search:");
        searchField = new JTextField(20);
        JButton exportButton = new JButton("Export Schedule");

        exportButton.addActionListener(e -> exportSchedule());
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterSchedule();
            }
        });

        controlPanel.add(searchLabel);
        controlPanel.add(searchField);
        controlPanel.add(exportButton);
        return controlPanel;
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit");
        JMenuItem deleteItem = new JMenuItem("Delete");

        editItem.addActionListener(e -> editEntry());
        deleteItem.addActionListener(e -> deleteEntry());

        popupMenu.add(editItem);
        popupMenu.add(deleteItem);
        return popupMenu;
    }

    private void addSubject() {
        String subject = subjectField.getText().trim();
        int year, students;
        try {
            year = Integer.parseInt(sessionField.getText().trim()) - 2021;
            students = Integer.parseInt(studentsField.getText().trim());
            if (year >= 0 && year < 4 && !subject.isEmpty() && students > 0) {
                subjectsPerSession[year].put(subject, students);
                subjectListArea.append("Year " + (2021 + year) + ": " + subject + " (" + students + " students)\n");
                subjectField.setText("");
                sessionField.setText("");
                studentsField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Please enter valid details.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }

    private void scheduleExams() {
        tableModel.setRowCount(0);
        scheduler.assignExamSchedule(subjectsPerSession);
    }

    private void editEntry() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow != -1) {
            String subject = tableModel.getValueAt(selectedRow, 1).toString();
            String newSubject = JOptionPane.showInputDialog(this, "Edit Subject:", subject);
            if (newSubject != null && !newSubject.trim().isEmpty()) {
                tableModel.setValueAt(newSubject, selectedRow, 1);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to edit.");
        }
    }

    private void deleteEntry() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
        }
    }

    private void exportSchedule() {
        try (FileWriter writer = new FileWriter("ExamSchedule.csv")) {
            // Write headers
            writer.write("Year,Subject,Rooms,Time,Date\n");

            // Iterate through table rows
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                StringBuilder row = new StringBuilder();

                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    Object cellValue = tableModel.getValueAt(i, j);

                    // Process cell data based on column
                    if (cellValue instanceof Date) {
                        // Format dates as yyyy-MM-dd
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        row.append(dateFormat.format((Date) cellValue));
                    } else if (cellValue != null) {
                        // Handle other values (escape commas for CSV safety)
                        String sanitizedValue = cellValue.toString().replace(",", " ");
                        row.append(sanitizedValue);
                    } else {
                        row.append(""); // Handle null values
                    }

                    if (j < tableModel.getColumnCount() - 1) {
                        row.append(","); // Add comma unless it's the last column
                    }
                }
                row.append("\n");
                writer.write(row.toString());
            }

            JOptionPane.showMessageDialog(this, "Schedule exported successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error exporting schedule: " + ex.getMessage());
        }
    }

    private void filterSchedule() {
        String query = searchField.getText().toLowerCase();
        DefaultTableModel filteredModel = new DefaultTableModel(new String[]{"Year", "Subject", "Rooms", "Time", "Date"}, 0);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean matches = false;
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                String value = tableModel.getValueAt(i, j).toString().toLowerCase();
                if (value.contains(query)) {
                    matches = true;
                    break;
                }
            }
            if (matches) {
                filteredModel.addRow(new Object[]{
                        tableModel.getValueAt(i, 0),
                        tableModel.getValueAt(i, 1),
                        tableModel.getValueAt(i, 2),
                        tableModel.getValueAt(i, 3),
                        tableModel.getValueAt(i, 4)
                });
            }
        }
        examTable.setModel(filteredModel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Exam_Schedule_Project gui = new Exam_Schedule_Project();
            gui.setVisible(true);
        });
    }
}

class ExamScheduler {
    private Map<String, Exam> examSchedule = new HashMap<>();
    private final String[] rooms = {"Room E", "Room A", "Room B", "Room C", "Room D"};
    private final String[] sessionTimes = {"9AM - 12PM", "12:30PM - 3:30PM"};
    private final int[] roomCapacities = {70, 70, 50, 50, 50};
    private Map<Date, Set<Integer>> sessionDailyUsage = new HashMap<>();
    private Map<Date, Set<String>> roomDailyUsage = new HashMap<>();
    private DefaultTableModel tableModel;

    public ExamScheduler(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }

    public void assignExamSchedule(Map<String, Integer>[] subjectsPerSession) {
        long oneDay = 24 * 60 * 60 * 1000;
        Date startDate = new Date();

        for (int yearIndex = 0; yearIndex < 4; yearIndex++) {
            Date examDate = new Date(startDate.getTime());
            int year = 2021 + yearIndex;

            for (Map.Entry<String, Integer> entry : subjectsPerSession[yearIndex].entrySet()) {
                String subject = entry.getKey();
                int studentStrength = entry.getValue();
                boolean scheduled = false;

                while (!scheduled) {
                    if (isSessionScheduledForDate(year, examDate)) {
                        examDate = new Date(examDate.getTime() + oneDay);
                        continue;
                    }

                    for (String timeSlot : sessionTimes) {
                        int studentsRemaining = studentStrength;
                        ArrayList<String> assignedRooms = new ArrayList<>();
                        Map<String, Integer> roomStudentMap = new HashMap<>();

                        for (int i = 0; i < rooms.length && studentsRemaining > 0; i++) {
                            if (!isRoomInUse(examDate, timeSlot, rooms[i])) {
                                assignedRooms.add(rooms[i]);
                                int assignedStudents = Math.min(studentsRemaining, roomCapacities[i]);
                                roomStudentMap.put(rooms[i], assignedStudents);
                                studentsRemaining -= assignedStudents;
                                markRoomUsage(examDate, timeSlot, rooms[i]);
                            }
                        }

                        if (studentsRemaining == 0) {
                            Exam exam = new Exam(subject, assignedRooms.toArray(new String[0]), timeSlot, examDate, year, roomStudentMap);
                            examSchedule.put(subject, exam);

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            StringBuilder roomDetails = new StringBuilder();
                            for (Map.Entry<String, Integer> roomEntry : roomStudentMap.entrySet()) {
                                roomDetails.append(roomEntry.getKey()).append(" (" + roomEntry.getValue() + "), ");
                            }
                            tableModel.addRow(new Object[]{year, subject, roomDetails.toString(), timeSlot, sdf.format(examDate)});
                            markSessionForDate(year, examDate);
                            scheduled = true;
                            break;
                        }
                    }

                    if (!scheduled) {
                        examDate = new Date(examDate.getTime() + oneDay);
                    }
                }
            }
        }
    }

    private boolean isSessionScheduledForDate(int year, Date date) {
        sessionDailyUsage.putIfAbsent(date, new HashSet<>());
        return sessionDailyUsage.get(date).contains(year);
    }

    private void markSessionForDate(int year, Date date) {
        sessionDailyUsage.putIfAbsent(date, new HashSet<>());
        sessionDailyUsage.get(date).add(year);
    }

    private boolean isRoomInUse(Date date, String timeSlot, String room) {
        roomDailyUsage.putIfAbsent(date, new HashSet<>());
        return roomDailyUsage.get(date).contains(timeSlot + room);
    }

    private void markRoomUsage(Date date, String timeSlot, String room) {
        roomDailyUsage.putIfAbsent(date, new HashSet<>());
        roomDailyUsage.get(date).add(timeSlot + room);
    }
}

class Exam {
    private String subject;
    private String[] rooms;
    private String sessionTime;
    private Date date;
    private int session;
    private Map<String, Integer> roomStudentMap;

    public Exam(String subject, String[] rooms, String sessionTime, Date date, int session, Map<String, Integer> roomStudentMap) {
        this.subject = subject;
        this.rooms = rooms;
        this.sessionTime = sessionTime;
        this.date = date;
        this.session = session;
        this.roomStudentMap = roomStudentMap;
    }
}
