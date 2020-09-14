package dk.dd.rmi.dbserver;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Server_Bank_Interface extends Remote {
    List<Customer> getMillionaires() throws RemoteException;
    List<Customer> findAllByName(String name) throws RemoteException;
    String add_Customer_data_return_report(File file) throws RemoteException;
}
