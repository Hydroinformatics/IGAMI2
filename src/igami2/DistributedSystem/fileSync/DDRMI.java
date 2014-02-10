package igami2.DistributedSystem.fileSync;

import java.io.*;
import java.util.Vector;

public interface DDRMI extends java.rmi.Remote{
	boolean checkid(int id) throws java.rmi.RemoteException;
        boolean createFile(String name, byte[] buf) throws java.rmi.RemoteException;
        boolean createDir(String name) throws java.rmi.RemoteException;
	boolean putData(String name, byte[] buff) throws java.rmi.RemoteException;
        byte[] getData(String name, int len) throws java.rmi.RemoteException;
        public int registerClient(String addr) throws java.rmi.RemoteException;
        boolean openFileWriter(String name) throws java.rmi.RemoteException;
        boolean openFileReader(String name) throws java.rmi.RemoteException;
        boolean closeFile(String name) throws java.rmi.RemoteException;
        boolean closeFileReader(String name) throws java.rmi.RemoteException;
        boolean delete(String name) throws java.rmi.RemoteException;
        boolean synDirs() throws java.rmi.RemoteException;
        Vector getDIRList() throws java.rmi.RemoteException;
        Object startSecretSession(Object ob)throws java.rmi.RemoteException;
        
}
