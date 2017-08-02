package com.ufmg.ppsus.xmlflat_parser.XMLFlatModelGenerator;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.basex.core.Context;
import org.basex.data.Result;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.upv.ibime.linkehr.exception.ParseADLException;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.ufmg.ppsus.xmlflat_parser.Constants;
import com.ufmg.ppsus.xmlflat_parser.Util;

@Path("/extract_conversor")
public class ExtractConversorService {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("text/xml;charset=UTF-8")
    public String ExtractConversorService(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws IOException, QueryException, Exception {
        String fileName = contentDispositionHeader.getFileName();
        //CEN-EN13606-COMPOSITION.Bres_Sumario_Alta_Obstetrica_Materna.v8.adl
        String archetypeId = fileName.substring(fileName.indexOf("CEN-EN13606")).replace(".xml","") + ".adl";
        Util.archetypeNameRules(archetypeId);
        if (fileInputStream == null || contentDispositionHeader == null) {
            return Constants.Error.FILE_NULL;
        }
        Context context = new Context();
        String xmlFlat = Util.fileToString(fileInputStream).replace("'", "\"");
        String query = parse(archetypeId, xmlFlat);
        // Create a query processor
        QueryProcessor processor = new QueryProcessor(query, context);
        // Execute the query
        Result result = processor.execute();
        context.close();
        return result.toString();
    }

    public String parse(String archetypeId, String xmlFlat) throws Exception {
        XQueryXMLFlatParser conversor = new XQueryXMLFlatParser();
        try {
            conversor.output(archetypeId, Util.getPath(this.getClass().getClassLoader().getResource("").getPath()), xmlFlat);
        } catch (IOException e) {
            throw new Exception(Constants.Error.READ_FILE + e.getMessage());
        } catch (ParseADLException e) {
            throw new Exception(Constants.Error.PASER_ADL + e.getMessage());
        }
        return conversor.toString();

    }

}
