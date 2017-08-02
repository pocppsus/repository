package com.ufmg.ppsus.xmlflat_parser;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/upload_adl")
public class UploadAdl {
    
    /**
     * Upload a File
     * @throws IOException 
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("text/html;charset=UTF-8")
    public String uploadFile(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws IOException, Exception {
 		if (fileInputStream == null || contentDispositionHeader == null){
 			return Constants.Error.FILE_NULL;
 		}
 		Util.archetypeNameRules(contentDispositionHeader.getFileName());
        String filePath = Util.getPath(this.getClass().getClassLoader().getResource("").getPath()) + "/" + contentDispositionHeader.getFileName();
        // save the file to the server
        Util.saveFile(fileInputStream, filePath);
        return Constants.Msg.FILE_SAVED;
    }
    

}
