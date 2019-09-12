package com.tgt.price.clearance.eos.services;

import com.jcraft.jsch.*;
import com.tgt.price.clearance.entities.ClearanceError;
import com.tgt.price.clearance.entities.ClearanceErrorCode;
import com.tgt.price.clearance.eos.configs.SftpProps;
import com.tgt.price.clearance.exceptions.ClearanceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadGatewayImpl implements UploadGateway {

    @Override
    public void upload(File file, SftpProps.Configs configs) throws ClearanceException {
        LOGGER.info("Input file {} is exists {}", file.getPath(), file.exists());
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            session = getSession(configs);
            channel = session.openChannel("sftp");
            channel.connect();
            LOGGER.info("sftp channel opened and connected.");
            channelSftp = (ChannelSftp) channel;
//            Below code converts file to binary
            channelSftp.ls(configs.getModeCmd());
            LOGGER.info("changed mode to text with cmd {}", configs.getModeCmd());
            LOGGER.info("output file {}", configs.getOutputFile());
            channelSftp.put(fis, configs.getOutputFile());
            LOGGER.info("Successfully transferred Input file {} to {}", file.getPath(), configs.getOutputFile());
        } catch (JSchException | SftpException | IOException e) {
            throw new ClearanceException(HttpStatus.INTERNAL_SERVER_ERROR, ClearanceErrorCode.CLR500,
                    Arrays.asList(new ClearanceError("SFtp", "file", e.getMessage())), e);
        } finally {
            if (channelSftp != null) {
                channelSftp.exit();
                LOGGER.info("sftp Channel exited.");
            }
            if (channel != null) {
                channel.disconnect();
                LOGGER.info("Channel disconnected.");
            }
            if (session != null) {
                session.disconnect();
                LOGGER.info("Host Session disconnected.");
            }
        }
    }

    private Session getSession(SftpProps.Configs configs) throws JSchException {
        JSch jsch = new JSch();
        if (!StringUtils.isBlank(configs.getPrivateKey())) {
            jsch.addIdentity(configs.getPrivateKey());
        }
        Session session = jsch.getSession(configs.getUser(), configs.getHost(), configs.getPort());
        if (!StringUtils.isBlank(configs.getPassword()))
            session.setPassword(configs.getPassword());
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        LOGGER.info("sftp session connected.");
        return session;
    }
}