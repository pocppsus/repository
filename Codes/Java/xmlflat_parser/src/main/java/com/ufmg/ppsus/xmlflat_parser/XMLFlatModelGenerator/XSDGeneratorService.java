package com.ufmg.ppsus.xmlflat_parser.XMLFlatModelGenerator;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.upv.ibime.linkehr.exception.ParseADLException;

import com.sun.jersey.multipart.FormDataParam;
import com.ufmg.ppsus.xmlflat_parser.Constants;
import com.ufmg.ppsus.xmlflat_parser.Util;

@Path("/xsd_generator")
public class XSDGeneratorService {

    @POST
    @Produces("text/xml;charset=UTF-8")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String convertIso8859(@FormDataParam("archetype_id") String archetypeId) throws Exception {
        Util.archetypeNameRules(archetypeId);
        return parse(archetypeId);
    }

    public String parse(String archetypeId) throws Exception {
        XSDGenerator xmlFlat = new XSDGenerator();
        try {
            xmlFlat.output(archetypeId, Util.getPath(this.getClass().getClassLoader().getResource("").getPath()));
        } catch (IOException e) {
            throw new Exception(Constants.Error.READ_FILE + e.getMessage());
        } catch (ParseADLException e) {
            throw new Exception(Constants.Error.PASER_ADL + e.getMessage());
        }
        System.out.println(xmlFlat.toString());
        return xmlFlat.toString();

    }

}
