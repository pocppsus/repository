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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.ufmg.ppsus.xmlflat_parser.Constants;
import com.ufmg.ppsus.xmlflat_parser.Util;

@Path("/xml_flat_conversor")
public class XMLFlatConversorService {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("text/xml;charset=UTF-8")
    public String uploadExtract(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws IOException, QueryException, Exception {
        if (fileInputStream == null || contentDispositionHeader == null) {
            return Constants.Error.FILE_NULL;
        }
        Context context = new Context();
        String extract = Util.fileToString(fileInputStream).replace("'", "\"");
        String query = parse(extract);
        // Create a query processor
        QueryProcessor processor = new QueryProcessor(query, context);
        // Execute the query
        Result result = processor.execute();
        context.close();
        return result.toString();
    }

    public String parse(String extract) {
        XQueryEHRExtractParser conversor = new XQueryEHRExtractParser();
        try {
            conversor.output(extract);
        } catch (IOException e) {
            return Constants.Error.READ_FILE + e.getMessage();
        }
        return conversor.toString();

    }

}
