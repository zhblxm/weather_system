package com.partners.weather.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class FtpUtil {
    public static final String ANONYMOUS_LOGIN = "anonymous";
    private FTPClient ftp;
    private boolean is_connected;

    public FtpUtil() {
        ftp = new FTPClient();
        is_connected = false;
    }
    
    public FtpUtil(int defaultTimeoutSecond, int connectTimeoutSecond, int dataTimeoutSecond){
        ftp = new FTPClient();
        is_connected = false;        
        ftp.setDefaultTimeout(defaultTimeoutSecond * 1000);
        ftp.setConnectTimeout(connectTimeoutSecond * 1000);
        ftp.setDataTimeout(dataTimeoutSecond * 1000);
    }
    public void connect(String host, int port, String user, String password, boolean isTextMode) throws IOException {
        try {
            ftp.connect(host, port);
        } catch (UnknownHostException ex) {
            throw new IOException("Can't find FTP server '" + host + "'");
        }
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            disconnect();
            throw new IOException("Can't connect to server '" + host + "'");
        }
        if (user == "") {
            user = ANONYMOUS_LOGIN;
        }
        if (!ftp.login(user, password)) {
            is_connected = false;
            disconnect();
            throw new IOException("Can't login to server '" + host + "'");
        } else {
            is_connected = true;
        }
        if (isTextMode) {
            ftp.setFileType(FTP.ASCII_FILE_TYPE);
        } else {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
        }
    }
    public void upload(String ftpFileName, File localFile) throws IOException {
        if (!localFile.exists()) {
            throw new IOException("Can't upload '" + localFile.getAbsolutePath() + "'. This file doesn't exist.");
        }
        InputStream in = null;
        try {
            ftp.enterLocalPassiveMode();
            in = new BufferedInputStream(new FileInputStream(localFile));
            if (!ftp.storeFile(ftpFileName, in)) {
                throw new IOException("Can't upload file '" + ftpFileName + "' to FTP server. Check FTP permissions and path.");
            }
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }
    }
    public void download(String ftpFileName, File localFile) throws IOException {
        OutputStream out = null;
        try {
            ftp.enterLocalPassiveMode();
            FTPFile[] fileInfoArray = ftp.listFiles(ftpFileName);
            if (fileInfoArray == null) {
                throw new FileNotFoundException("File " + ftpFileName + " was not found on FTP server.");
            }
            FTPFile fileInfo = fileInfoArray[0];
            long size = fileInfo.getSize();
            if (size > Integer.MAX_VALUE) {
                throw new IOException("File " + ftpFileName + " is too large.");
            }
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            if (!ftp.retrieveFile(ftpFileName, out)) {
                throw new IOException("Error loading file " + ftpFileName + " from FTP server. Check FTP permissions and path.");
            }

            out.flush();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                }
            }
        }
    }
    public void remove(String ftpFileName) throws IOException {
        if (!ftp.deleteFile(ftpFileName)) {
            throw new IOException("Can't remove file '" + ftpFileName + "' from FTP server.");
        }
    }
    public List<String> list(String filePath) throws IOException {
        List<String> fileList = new ArrayList<String>();
        
        // Use passive mode to pass firewalls.
        ftp.enterLocalPassiveMode();
        
        FTPFile[] ftpFiles = ftp.listFiles(filePath);
        int size = (ftpFiles == null) ? 0 : ftpFiles.length;
        for (int i = 0; i < size; i++) {
            FTPFile ftpFile = ftpFiles[i];
            if (ftpFile.isFile()) {
                fileList.add(ftpFile.getName());
            }
        }
        
        return fileList;
    }
    public void sendSiteCommand(String args) throws IOException {
        if (ftp.isConnected()) {
            try {
                ftp.sendSiteCommand(args);
            } catch (IOException ex) {
            }
        }
    }
    public void disconnect() throws IOException {

        if (ftp.isConnected()) {
            try {
                ftp.logout();
                ftp.disconnect();
                is_connected = false;
            } catch (IOException ex) {
            }
        }
    }
    public String makeFTPFileName(String ftpPath, File localFile) {
        if (ftpPath == "") {
            return localFile.getName();
        } else {
            String path = ftpPath.trim();
            if (path.charAt(path.length() - 1) != '/') {
                path = path + "/";
            }

            return path + localFile.getName();
        }
    }
    public boolean isConnected() {
        return is_connected;
    }
    public String getWorkingDirectory() {
        if (!is_connected) {
            return "";
        }

        try {
            return ftp.printWorkingDirectory();
        } catch (IOException e) {
        }

        return "";
    }
    public boolean setWorkingDirectory(String dir) {
        if (!is_connected) {
            return false;
        }

        try {
            return ftp.changeWorkingDirectory(dir);
        } catch (IOException e) {
        }

        return false;
    }
    public boolean setParentDirectory() {
        if (!is_connected) {
            return false;
        }

        try {
            return ftp.changeToParentDirectory();
        } catch (IOException e) {
        }

        return false;
    }
    public String getParentDirectory() {
        if (!is_connected) {
            return "";
        }

        String w = getWorkingDirectory();
        setParentDirectory();
        String p = getWorkingDirectory();
        setWorkingDirectory(w);

        return p;
    }

    public void getFile(String ftpFileName, OutputStream out) throws IOException {
        try {
            ftp.enterLocalPassiveMode();           
            FTPFile[] fileInfoArray = ftp.listFiles(ftpFileName);
            if (fileInfoArray == null) {
                throw new FileNotFoundException("File '" + ftpFileName + "' was not found on FTP server.");
            }
            FTPFile fileInfo = fileInfoArray[0];
            long size = fileInfo.getSize();
            if (size > Integer.MAX_VALUE) {
                throw new IOException("File '" + ftpFileName + "' is too large.");
            }
            if (!ftp.retrieveFile(ftpFileName, out)) {
                throw new IOException("Error loading file '" + ftpFileName + "' from FTP server. Check FTP permissions and path.");
            }

            out.flush();

        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                }
            }
        }
    }
    public void putFile(String ftpFileName, InputStream in) throws IOException {
        try {
            ftp.enterLocalPassiveMode();
            
            if (!ftp.storeFile(ftpFileName, in)) {
                throw new IOException("Can't upload file '" + ftpFileName + "' to FTP server. Check FTP permissions and path.");
            }
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
        }
    }
}