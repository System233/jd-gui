/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use, 
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.fileloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.CRC32;

import org.jd.gui.api.API;
import org.jd.gui.util.exception.ExceptionUtil;

import com.googlecode.dex2jar.tools.Dex2jarCmd;

public class DexFileLoaderProvider extends ZipFileLoaderProvider {
    protected static final String[] EXTENSIONS = { "dex" };

    @Override public String[] getExtensions() { return EXTENSIONS; }
    @Override public String getDescription() { return "Dex files (*.dex)"; }

    @Override
    public boolean accept(API api, File file) {
        return file.exists() && file.isFile() && file.canRead() && file.getName().toLowerCase().endsWith(".dex");
    }
    public String getBaseName(File file){
        return file.getName().split("\\.(?=[^\\.]+$)")[0];
    }
 
    public File getJarFile(File file)throws IOException{
        InputStream is=new FileInputStream(file);
        try{
            CRC32 crc32=new CRC32();
            crc32.reset();
            crc32.update(Files.readAllBytes(file.toPath()));
            return new File(file.getParent(),String.format("%s_%x.jar", getBaseName(file),crc32.getValue()));
        }finally{
            is.close();
        }
    }
    @Override
    public boolean load(API api, File file) {
        try {
            File jar=getJarFile(file);
            if(!jar.exists()){
                Dex2jarCmd.main(file.getAbsolutePath(),"-o",jar.getAbsolutePath(),"-f");
            }
            return super.load(api, jar);
        } catch (IOException e) {
            ExceptionUtil.printStackTrace(e);
        }
        return false;
    }

}
