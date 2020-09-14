package dk.dd.rmi.dbclient;
/**
 * @author Dora Di
 */

import java.io.File;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.util.List;
import java.util.Random;

import dk.dd.rmi.dbserver.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class RMIClientDB {
    private static String Xml_file = "E:/intellijig projects software dev/SI_RMI_DB_homework/src/main/java/dk/dd/rmi/dbclient/Client_xml_data.xml";

    public static void main(String args[]) throws Exception {
        Registry registry = LocateRegistry.getRegistry();
        Server_Bank_Interface serverint = (Server_Bank_Interface) registry.lookup("Server1");
        List<Customer> millionaires = serverint.getMillionaires();
        for (Customer c : millionaires) {
            System.out.println(c.get_accnum() + " " + c.get_amount() + " " + c.get_name());
        }
        try {
            Customer random_customer = millionaires.get(new Random(0).nextInt(millionaires.size() - 1));
            Write_customer_to_xml_file(random_customer);
            String database_report = serverint.add_Customer_data_return_report(new File(Xml_file));
            System.out.println(database_report);
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("getMillionaires() found no customers");
        }
    }

    private static void Write_customer_to_xml_file(Customer random_customer) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("Bank");
            doc.appendChild(rootElement);

            //customer element
            Element customer = doc.createElement("Customer");
            rootElement.appendChild(customer);

            customer.setAttribute("accnum", String.valueOf(random_customer.get_accnum()));
            customer.setAttribute("name", random_customer.get_name());
            customer.setAttribute("amount", String.valueOf(random_customer.get_amount()));

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(Xml_file));
            transformer.transform(source, result);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

} 
