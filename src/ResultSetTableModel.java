import javax.swing.table.AbstractTableModel;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;

public class ResultSetTableModel extends AbstractTableModel 
{
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private ResultSetMetaData metaData;
    private int rowCount;
    private boolean connectedToDatabase = false;
    private PreparedStatement logStatement;

    public ResultSetTableModel(DataSource dataSource) throws SQLException 
    {
        try 
        {
            Properties properties = new Properties();
            try (FileInputStream filein = new FileInputStream("db.properties")) 
            {
                properties.load(filein);
            }
            
            this.connection = dataSource.getConnection();
            this.statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            String logSQL = "INSERT INTO operationslog (operation, timestamp) VALUES (?, CURRENT_TIMESTAMP)";
            this.logStatement = connection.prepareStatement(logSQL);
            
            connectedToDatabase = true;
        } catch (Exception e) 
        {
            e.printStackTrace();
            throw new SQLException("Failed to initialize database connection", e);
        }
    }

    public void setQuery(String query) throws SQLException 
    {
        if (!connectedToDatabase) throw new IllegalStateException("Not connected to database");
        
        resultSet = statement.executeQuery(query);
        metaData = resultSet.getMetaData();
        resultSet.last();
        rowCount = resultSet.getRow();
        fireTableStructureChanged();

        logStatement.setString(1, "Query executed: " + query);
        logStatement.executeUpdate();
    }

    public void setUpdate(String sqlUpdate) throws SQLException 
    {
        if (!connectedToDatabase) throw new IllegalStateException("Not connected to database");
        
        int rowsAffected = statement.executeUpdate(sqlUpdate);
        
        logStatement.setString(1, "Update executed: " + sqlUpdate + " (Rows affected: " + rowsAffected + ")");
        logStatement.executeUpdate();
    }

    public int getColumnCount() 
    {
        try 
        {
            return metaData.getColumnCount();
        } catch (SQLException e) 
        {
            e.printStackTrace();
        }
        return 0;
    }

    public int getRowCount() 
    {
        return rowCount;
    }

    public Object getValueAt(int row, int column) 
    {
        try 
        {
            if (!connectedToDatabase) throw new IllegalStateException("Not connected to database");
            
            resultSet.absolute(row + 1);
            return resultSet.getObject(column + 1);
        } catch (SQLException e) 
        {
            e.printStackTrace();
        }
        return null;
    }

    public String getColumnName(int column) 
    {
        try 
        {
            return metaData.getColumnName(column + 1);
        } catch (SQLException e) 
        {
            e.printStackTrace();
        }
        return "";
    }

    public Class<?> getColumnClass(int column) 
    {
        try 
        {
            return Class.forName(metaData.getColumnClassName(column + 1));
        } catch (Exception e) 
        {
            e.printStackTrace();
        }
        return Object.class;
    }

    public void disconnectFromDatabase() 
    {
        if (!connectedToDatabase) 
        	{
        		return;
        	}
        
        try 
        {
            resultSet.close();
            statement.close();
            connection.close();
            connectedToDatabase = false;
        } catch (SQLException e) 
        {
            e.printStackTrace();
        }
    }
}
