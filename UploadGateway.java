package com.tgt.price.clearance.eos.services;

import java.io.File;
import com.tgt.price.clearance.exceptions.ClearanceException;
import com.tgt.price.clearance.eos.configs.SftpProps;

public interface UploadGateway {

    void upload(File file, SftpProps.Configs configs) throws ClearanceException;

}

//this is a sample code
