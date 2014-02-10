package igami2.DataBase;


import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author VIDYA
 */
public class DBQuery {
    
    private Connection con;
    private Statement stmt,stmt1;
    private ResultSet rs;
    private String DBpropertyFile = "../SWAT/igami2_config/dbproperties.xml";
    
    public DBQuery() {
        try {
            Properties prop = new Properties();
            InputStream in = new FileInputStream(DBpropertyFile);
            prop.loadFromXML(in);
            String userid = prop.getProperty("db_userid");
            String pass = prop.getProperty("db_pass");
            in.close();
            prop.clear();

            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/igmi2db";
            con = DriverManager.getConnection(url, userid, pass);
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            //System.out.println("Connection Sucess to "+url);
        } catch (Exception ex) {
            System.out.println("DataBase Connection Failed");
            ex.printStackTrace();
        }
    }
    
    public void addRows(String str) throws SQLException
    {        
        stmt.executeUpdate(str);
    }
    
    public ResultSet getRSTable(String table) throws SQLException
    {
        String str = null;

        str = "SELECT * FROM "+table;
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        rs = stmt.executeQuery(str);
        return rs;
    }
    
    public ResultSet getRSStmt(String str) throws SQLException
    {
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        rs = stmt.executeQuery(str);
        return rs;
    }

    public void update(String str) throws SQLException{
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        stmt.executeUpdate(str);
    }
    
    public boolean executeQuery(String str)throws SQLException{
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        return stmt.execute(str);
    }
}
