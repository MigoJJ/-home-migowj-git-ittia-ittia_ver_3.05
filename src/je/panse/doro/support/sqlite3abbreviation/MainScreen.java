package je.panse.doro.support.sqlite3abbreviation;

import javax.swing.*;      			
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import je.panse.doro.entry.EntryDir;


public class MainScreen extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
//    private String dbURL = "jdbc:sqlite:/home/migowj/git/ittia_ver_4.01/src/je/panse/doro/support/sqlite3/abbreviation/AbbFullDis.db";
    private static String dbURL = "jdbc:sqlite:" + EntryDir.homeDir + "/support/sqlite3abbreviation/AbbFullDis.db";

    public MainScreen() {
        setTitle("Database Interaction Screen");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"Abbreviation", "Full Text"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // South panel for buttons
        JPanel southPanel = new JPanel();

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> showAddDialog());
        southPanel.add(addButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteRecord());
        southPanel.add(deleteButton);

        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> editRecord());
        southPanel.add(editButton);

        JButton findButton = new JButton("Find");
        findButton.addActionListener(e -> showFindDialog());
        southPanel.add(findButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        southPanel.add(exitButton);

        add(southPanel, BorderLayout.SOUTH);

        // Load initial data
        loadData();
        setVisible(true);
    }

    private void loadData() {
        try (Connection conn = DriverManager.getConnection(dbURL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT abbreviation, full_text FROM Abbreviations")) {

            tableModel.setRowCount(0); // Clear existing data
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getString("abbreviation"), rs.getString("full_text")});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showFindDialog() {
        JTextField searchText = new JTextField(30);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Search Text:"));
        panel.add(searchText);
        int result = JOptionPane.showConfirmDialog(null, panel, "Find Abbreviation or Full Text", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            findRecords(searchText.getText());
        }
    }

    private void showAddDialog() {
        JTextField abbreviationField = new JTextField(10);
        JTextField fullTextField = new JTextField(30);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Abbreviation:"));
        panel.add(abbreviationField);
        panel.add(new JLabel("Full Text:"));
        panel.add(fullTextField);
        int result = JOptionPane.showConfirmDialog(null, panel, "Add New Entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            insertRecord(abbreviationField.getText(), fullTextField.getText());
        }
    }


    private void findRecords(String searchText) {
        String sql = "SELECT abbreviation, full_text FROM Abbreviations WHERE abbreviation LIKE ? OR full_text LIKE ?";
        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchText + "%");
            pstmt.setString(2, "%" + searchText + "%");
            ResultSet rs = pstmt.executeQuery();
            tableModel.setRowCount(0); // Clear existing data
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getString("abbreviation"), rs.getString("full_text")});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error finding data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void insertRecord(String abbreviation, String fullText) {
//    	String dbURL = "jdbc:sqlite:/home/migowj/Programs/SQLite3/database/IttiasupportAbb.db";
        String sql = "INSERT INTO Abbreviations (abbreviation, full_text) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, abbreviation);
            pstmt.setString(2, fullText);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                tableModel.addRow(new Object[]{abbreviation, fullText});
                JOptionPane.showMessageDialog(this, "Record added successfully!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error inserting data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String abbreviation = (String) tableModel.getValueAt(selectedRow, 0);
//            String dbURL = "jdbc:sqlite:/home/migowj/Programs/SQLite3/database/IttiasupportAbb.db";
            String sql = "DELETE FROM Abbreviations WHERE abbreviation = ?";
            try (Connection conn = DriverManager.getConnection(dbURL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, abbreviation);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Record deleted successfully!");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
        }
    }

    private void editRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String abbreviation = (String) tableModel.getValueAt(selectedRow, 0);
            JTextField abbreviationField = new JTextField(abbreviation, 10);
            JTextField fullTextField = new JTextField((String) tableModel.getValueAt(selectedRow, 1), 30);
            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Abbreviation:"));
            panel.add(abbreviationField);
            panel.add(new JLabel("Full Text:"));
            panel.add(fullTextField);
            int result = JOptionPane.showConfirmDialog(null, panel, "Edit Entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                updateRecord(abbreviation, abbreviationField.getText(), fullTextField.getText());
                loadData();  // Reload data to reflect changes
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to edit.");
        }
    }

    private void updateRecord(String oldAbbreviation, String newAbbreviation, String newFullText) {
//    	String dbURL = "jdbc:sqlite:/home/migowj/Programs/SQLite3/database/IttiasupportAbb.db";
        String sql = "UPDATE Abbreviations SET abbreviation = ?, full_text = ? WHERE abbreviation = ?";
        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newAbbreviation);
            pstmt.setString(2, newFullText);
            pstmt.setString(3, oldAbbreviation);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Record updated successfully!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] args) {
        new MainScreen();
    }
}
