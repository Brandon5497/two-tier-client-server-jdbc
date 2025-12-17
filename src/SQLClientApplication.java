/*
Name: Brandon Ramlagan
Course: CNT 4714 Spring 2025
Assignment title: Project 3 – A Two-tier Client-Server Application
Date: March 14, 2025
Class: SQLClientApplication
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.io.*;
import java.util.Properties;
import javax.swing.table.DefaultTableModel;

public class SQLClientApplication extends JPanel 
{
    private JButton ConnectButton, DisConnectButton, ClearCommand, ExecuteButton, ClearWindow, CloseApp; 
    private JLabel CommandLabel, StatusLabel, DBURLLabel, UserPropertiesLabel;
    private JTextArea textCommand;
    private JComboBox<String> dbPropertiesComboBox, userPropertiesComboBox;
    private JTextField userText;
    private JPasswordField passwordText;
    private JTable resultTable;
    private DefaultTableModel Empty;
    private Connection connect;

    public SQLClientApplication()
    {
    	setLayout(new BorderLayout());

    	JPanel ConnectionDetailsPanel = new JPanel();
    	ConnectionDetailsPanel.setLayout(new GridLayout(0, 2));

    	JPanel SQLCommandPanel = new JPanel();
    	SQLCommandPanel.setLayout(new BoxLayout(SQLCommandPanel, BoxLayout.Y_AXIS));

    	JPanel SQLCommandButtonsPanel = new JPanel();
    	SQLCommandButtonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

    	String[] dbPropertiesItems = {"project3.properties", "bikedb.properties", "project3app.properties"};
    	String[] userPropertiesItems = {"root.properties", "client1.properties", "client2.properties"};

    	dbPropertiesComboBox = new JComboBox<>(dbPropertiesItems);
    	userPropertiesComboBox = new JComboBox<>(userPropertiesItems);

    	ConnectButton = new JButton("Connect to Database");
    	DisConnectButton = new JButton("Disconnect From Database");
    	ClearCommand = new JButton("Clear SQL Command");
    	ExecuteButton = new JButton("Execute SQL Command");
    	ClearWindow = new JButton("Clear Result Window");
    	CloseApp = new JButton("Close Application");

    	DBURLLabel = new JLabel("DB URL Properties");
    	UserPropertiesLabel = new JLabel("User Properties");
    	CommandLabel = new JLabel("Enter An SQL Command:");
    	StatusLabel = new JLabel("Status: Not Connected");

    	userText = new JTextField(15);
    	passwordText = new JPasswordField(15);
    	textCommand = new JTextArea(5, 40);

    	resultTable = new JTable();
    	Empty = new DefaultTableModel();
    	resultTable.setModel(Empty);
    	JScrollPane tableScrollPane = new JScrollPane(resultTable);

    	ConnectionDetailsPanel.add(DBURLLabel);
    	ConnectionDetailsPanel.add(dbPropertiesComboBox);
    	ConnectionDetailsPanel.add(UserPropertiesLabel);
    	ConnectionDetailsPanel.add(userPropertiesComboBox);
    	ConnectionDetailsPanel.add(new JLabel("Username:"));
    	ConnectionDetailsPanel.add(userText);
    	ConnectionDetailsPanel.add(new JLabel("Password:"));
    	ConnectionDetailsPanel.add(passwordText);
    	ConnectionDetailsPanel.add(ConnectButton);
    	ConnectionDetailsPanel.add(DisConnectButton);

    	SQLCommandPanel.add(CommandLabel); 
    	SQLCommandPanel.add(new JScrollPane(textCommand));

    	SQLCommandButtonsPanel.add(ExecuteButton);
    	SQLCommandButtonsPanel.add(ClearCommand);
    	SQLCommandPanel.add(SQLCommandButtonsPanel); 

    	JPanel topPanel = new JPanel(new GridLayout(1, 2));
    	topPanel.add(ConnectionDetailsPanel);
    	topPanel.add(SQLCommandPanel);
    	add(topPanel, BorderLayout.NORTH); 

    	JPanel buttonPanel = new JPanel();
    	buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); 
    	buttonPanel.add(ClearWindow);
    	buttonPanel.add(CloseApp);
    	add(buttonPanel, BorderLayout.CENTER);

    	JPanel bottomPanel = new JPanel(new BorderLayout());
    	bottomPanel.add(StatusLabel, BorderLayout.NORTH);
    	bottomPanel.add(tableScrollPane, BorderLayout.CENTER);

    	add(bottomPanel, BorderLayout.SOUTH); 


        ConnectButton.addActionListener(e -> connectToDatabase());
        DisConnectButton.addActionListener(e -> disconnectDatabase());
        ExecuteButton.addActionListener(e -> executeSQL());
        ClearCommand.addActionListener(e -> textCommand.setText(""));
        ClearWindow.addActionListener(e -> resultTable.setModel(new DefaultTableModel()));
        CloseApp.addActionListener(e -> System.exit(0));
    }

    private void connectToDatabase() 
    {
        try 
        {
            if (connect != null) connect.close();
            StatusLabel.setText("Status: No Connection");

            String dbConfigFile = (String) dbPropertiesComboBox.getSelectedItem();
            String userConfigFile = (String) userPropertiesComboBox.getSelectedItem();

            Properties dbProps = loadProperties(dbConfigFile);
            Properties userProps = loadProperties(userConfigFile);

            String dbUrl = dbProps.getProperty("MYSQL_DB_URL");
            
            String username = userText.getText().trim();
            String password = new String(passwordText.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) 
            {
                JOptionPane.showMessageDialog(this, "Username and password must not be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (username.isEmpty()) 
            {
                username = userProps.getProperty("MYSQL_DB_USERNAME");
            }
            if (password.isEmpty()) 
            {
                password = userProps.getProperty("MYSQL_DB_PASSWORD");
            }

            connect = DriverManager.getConnection(dbUrl, username, password);
            StatusLabel.setText("Status: Connected to " + dbUrl);
            JOptionPane.showMessageDialog(this, "Connected successfully!");
        } catch (SQLException | IOException e) 
        {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private Properties loadProperties(String filename) throws IOException 
    {
        Properties props = new Properties();
        
        try (InputStream input = new FileInputStream(filename)) 
        {
            props.load(input);
        }
        return props;
    }

    private void disconnectDatabase() 
    {
        try 
        {
            if (connect != null) 
            {
                connect.close();
                StatusLabel.setText("Status: Disconnected");
                JOptionPane.showMessageDialog(this, "Disconnected successfully.");
            }
        } catch (SQLException e) 
        {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeSQL() 
    {
        try 
        {
            if (connect == null) 
            {
                JOptionPane.showMessageDialog(this, "No active database connection.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = textCommand.getText().trim();
            Statement stmt = connect.createStatement();

            if (sql.toUpperCase().startsWith("SELECT")) 
            {
                ResultSet rs = stmt.executeQuery(sql);
                resultTable.setModel(buildTableModel(rs));
            } 
            else 
            {
                int rowsAffected = stmt.executeUpdate(sql);
                JOptionPane.showMessageDialog(this, "Query executed. Rows affected: " + rowsAffected);
            }
        } catch (SQLException e) 
        {
            JOptionPane.showMessageDialog(this, e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException 
    {
        DefaultTableModel model = new DefaultTableModel();
        int columnCount = rs.getMetaData().getColumnCount();

        for (int i = 1; i <= columnCount; i++) 
        {
            model.addColumn(rs.getMetaData().getColumnName(i));
        }

        while (rs.next()) 
        {
            Object[] rowData = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) 
            {
                rowData[i] = rs.getObject(i + 1);
            }
            model.addRow(rowData);
        }
        return model;
    }

    public static void main(String[] args) 
    {
        JFrame frame = new JFrame("SQL Client Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new SQLClientApplication());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
