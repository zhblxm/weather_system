package com.partners.weather.ftp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import java.io.IOException;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SftpUtil {
    private static final JSch J_SCH = new JSch();
    private String host;
    private Session session = null;
    private int connectTimeout = 0;

    public SftpUtil(String host, String user) throws IOException {
        this(host, user, 22, 3000);
    }

    public SftpUtil(String host, String user, int port) throws IOException {
        this(host, user, port, 3000);
    }

    public SftpUtil(String host, String user, int port, int connectTimeout) throws IOException {
        this.host = host;
        this.connectTimeout = connectTimeout;
        String config = "Port " + port + "\n\n" + "  User " + user + "\n" + "  Hostname " + host + "\n" + "Host *\n" + "  ConnectTime " + connectTimeout + "\n"
                + "  PreferredAuthentications keyboard-interactive,password,publickey\n";
        ConfigRepository configRepository = com.jcraft.jsch.OpenSSHConfig.parse(config);
        J_SCH.setConfigRepository(configRepository);
    }

    public void connect(String password) throws JSchException {
        try {
            this.session = J_SCH.getSession(this.host);
            this.session.setPassword(password);
            UserInfo sftpUserInfo = new SftpUserInfo() {
                public boolean promptYesNo(String message) {
                    return true;
                }
            };
            this.session.setUserInfo(sftpUserInfo);
            this.session.connect(this.connectTimeout == 0 ? 3000 : this.connectTimeout);

        } catch (JSchException ex) {
            log.error(ex.getMessage(), ex);
            throw new JSchException("Can't find SFTP server '" + this.host + "'");
        }
    }

    public void upload(String sourceFilePath, String destinationFilePath) {
        Channel channel = null;
        ChannelSftp channelSftp = null;
        try {
            if (!this.session.isConnected()) {
                throw new JSchException("Can't find SFTP server '" + this.session.getHost() + "'");
            }
            channel = this.session.openChannel("sftp");
            channel.connect(this.connectTimeout == 0 ? 3000 : this.connectTimeout);
            channelSftp = (ChannelSftp) channel;
            channelSftp.put(sourceFilePath, destinationFilePath, ChannelSftp.OVERWRITE);
        } catch (JSchException | SftpException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            if (Objects.nonNull(channelSftp) && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (Objects.nonNull(channel) && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    public void disconnect() throws JSchException {
        if (Objects.isNull(this.session) || !this.session.isConnected()) {
            return;
        }
        this.session.disconnect();
    }

    public static abstract class SftpUserInfo implements UserInfo, UIKeyboardInteractive {
        public String getPassword() {
            return null;
        }

        public boolean promptYesNo(String str) {
            return false;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return false;
        }

        public boolean promptPassword(String message) {
            return false;
        }

        public void showMessage(String message) {
        }

        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
            return null;
        }
    }
}