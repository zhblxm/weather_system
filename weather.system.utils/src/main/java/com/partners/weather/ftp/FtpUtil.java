package com.partners.weather.ftp;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.SneakyThrows;

public class FtpUtil {
    public static final String ANONYMOUS_LOGIN = "anonymous";
    private FTPClient ftp;

    public FtpUtil() {
    }

    public FtpUtil(int defaultTimeoutSecond, int connectTimeoutSecond, int dataTimeoutSecond) {
        ftp = new FTPClient();
        ftp.setDefaultTimeout(defaultTimeoutSecond * 1000);
        ftp.setConnectTimeout(connectTimeoutSecond * 1000);
        ftp.setDataTimeout(dataTimeoutSecond * 1000);
    }

    public void connect(String host, int port, String user, String password, boolean isTextMode) throws IOException {
        ftp.connect(host, port);
        user = StringUtils.isBlank(user) ? ANONYMOUS_LOGIN : user;
        if (!ftp.login(user, password)) {
            disconnect();
            throw new IOException("Can't login to server '" + host + "'");
        }
        if (isTextMode) {
            ftp.setFileType(FTP.ASCII_FILE_TYPE);
            return;
        }
        ftp.setFileType(FTP.BINARY_FILE_TYPE);

    }

    public void upload(String ftpFileName, File localFile) throws IOException {
        if (!localFile.exists()) {
            throw new IOException("Can't upload '" + localFile.getAbsolutePath() + "'. This file doesn't exist.");
        }
        ftp.enterLocalPassiveMode();
        try (FileInputStream fileInputStream = new FileInputStream(localFile); InputStream in = new BufferedInputStream(fileInputStream)) {
            if (!ftp.storeFile(ftpFileName, in)) {
                throw new IOException("Can't upload file '" + ftpFileName + "' to FTP server. Check FTP permissions and path.");
            }
        }
    }

    public void download(String ftpFileName, File localFile) throws IOException {
        ftp.enterLocalPassiveMode();
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(localFile))) {
            FTPFile[] fileInfoArray = ftp.listFiles(ftpFileName);
            if (ArrayUtils.isEmpty(fileInfoArray)) {
                throw new FileNotFoundException("File " + ftpFileName + " was not found on FTP server.");
            }
            FTPFile fileInfo = fileInfoArray[0];
            if (fileInfo.getSize() > Integer.MAX_VALUE) {
                throw new IOException("File " + ftpFileName + " is too large.");
            }
            if (!ftp.retrieveFile(ftpFileName, out)) {
                throw new IOException("Error loading file " + ftpFileName + " from FTP server. Check FTP permissions and path.");
            }
            out.flush();
        }
    }

    public void remove(String ftpFileName) throws IOException {
        if (!ftp.deleteFile(ftpFileName)) {
            throw new IOException("Can't remove file '" + ftpFileName + "' from FTP server.");
        }
    }

    public List<String> list(String filePath) throws IOException {
        ftp.enterLocalPassiveMode();
        return Arrays.stream(ftp.listFiles(filePath)).filter(FTPFile::isFile).map(FTPFile::getName).collect(Collectors.toList());
    }

    public void disconnect() throws IOException {

        if (!ftp.isConnected()) {
            return;
        }
        ftp.logout();
        ftp.disconnect();
    }

    public boolean isConnected() {
        return Objects.nonNull(ftp) && ftp.isConnected();
    }

    public String getWorkingDirectory() throws IOException {
        if (!ftp.isConnected()) {
            return "";
        }
        return ftp.printWorkingDirectory();
    }

    @SneakyThrows
    public boolean setWorkingDirectory(String dir) {
        if (missConnection()) return false;
        return ftp.changeWorkingDirectory(dir);
    }

    @SneakyThrows
    public boolean setParentDirectory() {
        if (missConnection()) return false;
        return ftp.changeToParentDirectory();
    }

    private boolean missConnection() {
        return !ftp.isConnected();
    }

    public void getFile(String ftpFileName, OutputStream out) throws IOException {
        try {
            ftp.enterLocalPassiveMode();
            FTPFile[] fileInfoArray = ftp.listFiles(ftpFileName);
            if (ArrayUtils.isEmpty(fileInfoArray)) {
                throw new FileNotFoundException("File '" + ftpFileName + "' was not found on FTP server.");
            }
            FTPFile fileInfo = fileInfoArray[0];
            if (fileInfo.getSize() > Integer.MAX_VALUE) {
                throw new IOException("File '" + ftpFileName + "' is too large.");
            }
            if (!ftp.retrieveFile(ftpFileName, out)) {
                throw new IOException("Error loading file '" + ftpFileName + "' from FTP server. Check FTP permissions and path.");
            }
            out.flush();
        } finally {
            out.close();
        }
    }

    public void putFile(String ftpFileName, InputStream in) throws IOException {
        try {
            ftp.enterLocalPassiveMode();
            if (!ftp.storeFile(ftpFileName, in)) {
                throw new IOException("Can't upload file '" + ftpFileName + "' to FTP server. Check FTP permissions and path.");
            }
        } finally {
            in.close();
        }
    }
}