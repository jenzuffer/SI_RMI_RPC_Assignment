package dk.dd.rmi.dbserver;

/**
 * @author template Dora Di
 */

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
public class BankImplementation implements Server_Bank_Interface {
    public BankImplementation() throws RemoteException {
        LocateRegistry.createRegistry(1099);
        Server_Bank_Interface stub = (Server_Bank_Interface) UnicastRemoteObject.exportObject(this, 1099);
        //intensive logging message
        System.out.println("BankImplementation()");
        //RemoteServer.setLog(System.out);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("Server1", stub);
        create_tables();

    }

    //instead of an in memory database i just run a database on my localhost
    //table is created but requires to have a database with the name customer
    public static String url = "jdbc:mysql://localhost:3306/customer?useSSL=true";
    public static String user = "root";
    public static String password = "1234";
    public static String driver = "com.mysql.jdbc.Driver";

    @GetMapping("/hello")
    public String sayHello(@RequestParam(value = "myName", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    @GetMapping("/bank")
    public List<Customer> getMillionaires() {
        List<Customer> list = new ArrayList<Customer>();
        try {
            Class.forName(driver);
            try (Connection con = DriverManager.getConnection(url, user, password)) {
                try (PreparedStatement ps = con.prepareStatement("select * from Customer where amount >= 100000;")) {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        int accnum = rs.getInt(1);
                        String name = rs.getString(2);
                        double amount = rs.getDouble(3);
                        list.add(new Customer(accnum, name, amount));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }

    @GetMapping("/name")
    public List<Customer> findAllByName(String name) throws RemoteException {
        List<Customer> list = new ArrayList<Customer>();
        try {
            Class.forName(driver);
            try (Connection con = DriverManager.getConnection(url, user, password)) {
                try (PreparedStatement ps = con.prepareStatement("select * from customer c where c.name like \"% " +
                        name + "%\"")) {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        int accnum = rs.getInt(1);
                        String name_customer = rs.getString(2);
                        double amount = rs.getDouble(3);
                        list.add(new Customer(accnum, name_customer, amount));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }

    @Override
    public String add_Customer_data_return_report(File file) {
        String database_report = "database content size in MB: ";
        Customer customer = Read_customer_from_xml_file(file);
        System.out.println("customer prior amount: " + customer.get_amount() + " customer name: "
                + customer.get_name());
        customer.setAmount(customer.get_amount() + new Random(-5000).nextInt(5000));
        System.out.println("customer post amount: " + customer.get_amount());
        String sql_query = "update customer set amount = " + customer.get_amount() + " where accnum = " + customer.get_accnum() + "";
        Update_result_set(sql_query);
        database_report += receive_db_size();
        return database_report;
    }

    private String receive_db_size() {
        //deppending on your scheme it may be public instead or whatever scheme you placed it in
        String sql_query_retreive_size = "SELECT SUM(data_length + index_length) / 1024 / 1024 AS \"Size (MB)\" FROM information_schema.TABLES where TABLE_SCHEMA = \"customer\" and TABLE_NAME = \"customer\"";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (Connection con = DriverManager.getConnection(url, user, password)) {
            try (PreparedStatement ps = con.prepareStatement(sql_query_retreive_size)) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                return String.valueOf(rs.getDouble(1));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return "failure receiving size";
    }

    private static Customer Read_customer_from_xml_file(File Xml_file) {
        //https://www.tutorialspoint.com/java_xml/java_dom_parse_document.htm
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = null;
        try {
            doc = dBuilder.parse(Xml_file);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Customer xml_customer = null;
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("Bank");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            System.out.println(nList.item(temp).getTextContent());
            NodeList customer = ((Element) nList.item(temp)).getElementsByTagName("Customer");
            System.out.println(customer.item(0).getAttributes().getLength());
            String textContent = customer.item(0).getAttributes().item(0).getTextContent();
            String nodeName = customer.item(0).getAttributes().item(0).getNodeName();
            String textContent1 = customer.item(0).getAttributes().item(1).getTextContent();
            String nodeName1 = customer.item(0).getAttributes().item(1).getNodeName();
            String textContent2 = customer.item(0).getAttributes().item(2).getTextContent();
            String nodeName2 = customer.item(0).getAttributes().item(2).getNodeName();
            int accnum = Integer.valueOf(textContent);
            double amount = Double.parseDouble(textContent1);
            System.out.println(nodeName + ": " + accnum);
            System.out.println(nodeName1 + ": " + textContent1);
            System.out.println(nodeName2 + ": " + amount);
            xml_customer = new Customer(accnum, textContent1, amount);
        }
        System.out.println("reached end of XML object conversion");
        return xml_customer;
    }

    public void Update_result_set(String Sql_query) {
        try {
            Class.forName(driver);
            //has autocommit=true
            try (Connection con = DriverManager.getConnection(url, user, password)) {
                try (PreparedStatement ps = con.prepareStatement(Sql_query)) {
                    ps.executeUpdate();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void create_tables() {
        String create_table = "create table IF NOT EXISTS Customer (accnum Integer not null AUTO_INCREMENT PRIMARY KEY, name varchar(250) not null, amount double not null);";
        Update_result_set(create_table);
        String insert_customers = "INSERT INTO Customer (name, amount) VALUES\n" +
                "('Alice Wonderland', 1000),\n" +
                "('Bill Bates', 10000),\n" +
                "('John doe', 50000),\n" +
                "('Corunsho Alakija', 100000);";
        Update_result_set(insert_customers);
    }

}  



