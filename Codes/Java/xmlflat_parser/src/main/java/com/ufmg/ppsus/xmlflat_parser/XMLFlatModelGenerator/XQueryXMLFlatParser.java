package com.ufmg.ppsus.xmlflat_parser.XMLFlatModelGenerator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.ehr.am.Archetype;
import org.ehr.am.constraintmodel.CAttribute;
import org.ehr.am.constraintmodel.CComplexObject;
import org.ehr.am.constraintmodel.CObject;
import org.ehr.am.constraintmodel.CPrimitiveObject;
import org.ehr.am.constraintmodel.CSingleAttribute;
import org.ehr.am.constraintmodel.primitive.CDuration;
import org.ehr.am.constraintmodel.primitive.CString;
import org.ehr.rm.openehr.datatypes.quantity.datetime.DvDate;
import org.ehr.rm.openehr.datatypes.quantity.datetime.DvDateTime;
import org.ehr.rm.openehr.datatypes.quantity.datetime.DvDuration;
import org.ehr.rm.openehr.datatypes.quantity.datetime.DvTime;
import org.ehr.rm.openehr.support.basic.Interval;
import org.upv.ibime.linkehr.exception.ParseADLException;
import org.upv.ibime.linkehr.io.IO;

import com.ufmg.ppsus.xmlflat_parser.EN13606Types;
import com.ufmg.ppsus.xmlflat_parser.Util;


/**
 * Generate Xquery script based in the achetype to interpret Flat XML 
 * 
 * The xquery process Flat XML and generate ISO 13606 extract
 * 
 * @author fabio elias
 */
public class XQueryXMLFlatParser {

    /* charset encodings */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    /* fields */
    private Charset encoding;

    private String lineSeparator;

    private String indent;

    private Out out;

    private String xmlFlat;

    /**
     * Create an outputter with default encoding, indent and lineSeparator
     */
    public XQueryXMLFlatParser() {
        this.encoding = UTF8;
        this.indent = "    "; // 4 white space characters
        this.lineSeparator = System.getProperty("line.separator");
    }

    class Out {

        StringBuilder out;

        public Out() {
            this.out = new StringBuilder();
        }

        public void append(String txt) {
            out.append(txt);
        }

        public void writeln(String txt) {
            out.append(txt + "\n");
        }

        public void write(String txt) {
            out.append(txt);
        }

        @Override
        public String toString() {
            return this.out.toString();
        }
    }

    /**
     * Output given Xquery as string to parse Flat XML
     *
     * @param archetype
     * @param xmlFlat
     * @return a string in ADL format
     * @throws IOException
     * @throws ParseADLException
     */
    public String output(String archetype, String path, String xmlFlat) throws IOException, ParseADLException {
        Archetype conceptArchetype = IO.loadADL(path + "/" + archetype);
        this.xmlFlat = xmlFlat.replace("'", "\"");
        this.out = new Out();
        parse(conceptArchetype, out);
        //Gravar arquivo de log
        Util.saveLogFile(path, conceptArchetype.getArchetypeId().toString(), out.toString());
        return out.toString();
    }

    /**
     * Output given Xquery to writer
     *
     * @param archetype
     * @param out
     * @throws IOException
     */
    public void parse(Archetype archetype, Out out) throws IOException {
        //Create record component functions
        printRecordComponente(out, 0);
        //Create record component functions
        printDefinition(archetype.getDefinition(), out);
    }

    protected void printDefinition(CComplexObject definition, Out out)
            throws IOException {
        printCComplexObjectTop(definition, 0, out);
    }

    protected void printExtensionRoot(Out out, int indent, String extension, String oid) throws IOException {
        indent(indent + 1, out);
        out.writeln("<extension>" + extension + "</extension>");
        indent(indent + 1, out);
        out.writeln("<root>");
        indent(indent + 2, out);
        out.writeln("<oid>" + oid + "</oid>");
        indent(indent + 1, out);
        out.writeln("</root>");
    }

    protected void printCObjectElements(CObject cobj, int indent, Out out)
            throws IOException {
        printEmptyString(cobj.getRmTypeName(), cobj.getRmTypeName(), indent, out);

    }

    private void printEmptyString(String label, String value, int indent,
            Out out) throws IOException {

        indent(indent, out);
        out.write("<" + label + ">");
        out.write(value);
        out.writeln("</" + label + ">");
    }

    /**
     * Interpret children element of the object
     * 
     * @param cattribute
     * @param indent
     * @param out
     * @param parent
     * @param parentXmlFlatPath
     * @throws IOException 
     */
    protected void printCAttribute(CAttribute cattribute, int indent, Out out, String parent, String parentXmlFlatPath)
            throws IOException {

        if (!cattribute.isAnyAllowed()) {
            List<CObject> children = cattribute.getChildren();

            if (children.size() > 1
                    || !(children.get(0) instanceof CPrimitiveObject)) {
                for (CObject cobject : cattribute.getChildren()) {
                    printCObject(cobject, indent + 1, out, parent, parentXmlFlatPath);
                }
            }
        }

    }

    /**
     * Interpret the root of achetype definition
     * 
     * @param ccobj
     * @param indent
     * @param out
     * @throws IOException 
     */
    protected void printCComplexObjectTop(CComplexObject ccobj, int indent,
            Out out) throws IOException {
        printExtractHeader(indent, out);
        printCObject(ccobj, indent + 3, out, "", "$transformXmlFlat/");
        printExtractEnd(indent, out);

    }

    /**
     * Iterate childrens of the object
     * 
     * @param ccobj
     * @param indent
     * @param out
     * @param parent
     * @param parentXmlFlatPath
     * @throws IOException 
     */
    protected void printCComplexObject(CComplexObject ccobj, int indent,
            Out out, String parent, String parentXmlFlatPath) throws IOException {
        // print all attributes
        if (!ccobj.isAnyAllowed()) {
            for (CAttribute cattribute : ccobj.getAttributes()) {
                if (!cattribute.getRmAttributeName().contains("value")) {
                    printCAttribute(cattribute, indent, out, parent, parentXmlFlatPath);
                }
            }
        }
    }

    /**
     * Interpret complex structure and generate xquery script to interpret the expected node  of the Flat XML 
     *      and return the Extract XML node
     * @param cObject
     * @param indent
     * @param out
     * @param parent
     * @param parentXmlFlatPath
     * @throws IOException 
     */
    protected void printCObject(CObject cObject, int indent, Out out, String parent, String parentXmlFlatPath)
            throws IOException {
        if (cObject instanceof CComplexObject) {
            //the main object considered in this poc
            if (cObject.getRmTypeName().compareTo(EN13606Types.COMPOSITION.getName()) == 0) {
                //root of all elements of the extract
                if (!cObject.getArchetype().getNodeText(cObject.getNodeID()).isEmpty()) {
                    indent(indent, out);
                    out.writeln("<all_compositions>");
                    printElementOntolgy(cObject, indent + 1, out);
                    indent(indent + 1, out);
                    out.writeln("{local:recordComponent(" + parentXmlFlatPath + "/*:" + printNodeName(cObject.getArchetype().getNodeText(cObject.getNodeID())) + "/*:informacoes_de_auditoria, '" + EN13606Types.COMPOSITION.getName() + "')} ");

                }
                //interpret current object
                printCComplexObject((CComplexObject) cObject, indent, out, "content", parentXmlFlatPath + "/*:" + printNodeName(cObject.getArchetype().getNodeText(cObject.getNodeID())));
                if (!cObject.getArchetype().getNodeText(cObject.getNodeID()).isEmpty()) {
                    indent(indent, out);
                    out.writeln("</all_compositions>");
                }
            //Complex structure considered in this poc
            } else if (cObject.getRmTypeName().compareTo(EN13606Types.ENTRY.getName()) == 0) {
                if (!cObject.getArchetype().getNodeText(cObject.getNodeID()).isEmpty()) {
                    indent(indent, out);
                    out.writeln("{for $entry in " + parentXmlFlatPath + "/*:" + printNodeName(cObject.getArchetype().getNodeText(cObject.getNodeID())));
                    indent(indent + 1, out);
                    out.writeln("return");
                    indent(indent + 2, out);
                    out.writeln("<" + parent + " xsi:type=\"ENTRY\">");
                    printElementOntolgy(cObject, indent + 3, out);
                    indent(indent + 3, out);
                    out.writeln("{local:recordComponent($entry/*:informacoes_de_auditoria, '" + EN13606Types.ENTRY.getName() + "')} ");
                }
                printCComplexObject((CComplexObject) cObject, indent + 2, out, "items", "$entry");
                if (!cObject.getArchetype().getNodeText(cObject.getNodeID()).isEmpty()) {
                    indent(indent + 2, out);
                    out.writeln("</" + parent + ">");
                    indent(indent, out);
                    out.writeln("}");
                }
            } else if (cObject.getRmTypeName().compareTo(EN13606Types.SECTION.getName()) == 0) {
                if (!cObject.getArchetype().getNodeText(cObject.getNodeID()).isEmpty()) {
                    indent(indent, out);
                    out.writeln("{for $section in " + parentXmlFlatPath + "/*:" + printNodeName(cObject.getArchetype().getNodeText(cObject.getNodeID())));
                    indent(indent + 1, out);
                    out.writeln("return");
                    indent(indent + 2, out);
                    out.writeln("<" + parent + " xsi:type=\"SECTION\">");
                    printElementOntolgy(cObject, indent + 3, out);
                    indent(indent + 3, out);
                    out.writeln("{local:recordComponent($section/*:informacoes_de_auditoria, '" + EN13606Types.SECTION.getName() + "')} ");
                }
                printCComplexObject((CComplexObject) cObject, indent + 2, out, "members", "$section");
                if (!cObject.getArchetype().getNodeText(cObject.getNodeID()).isEmpty()) {
                    indent(indent + 2, out);
                    out.writeln("</" + parent + ">");
                    indent(indent, out);
                    out.writeln("}");
                }
            } else if (cObject.getRmTypeName().compareTo(EN13606Types.CLUSTER.getName()) == 0) {
                if (!cObject.getArchetype().getNodeText(cObject.getNodeID()).isEmpty()) {
                    indent(indent, out);
                    out.writeln("{for $cluster in " + parentXmlFlatPath + "/*:" + printNodeName(cObject.getArchetype().getNodeText(cObject.getNodeID())));
                    indent(indent + 1, out);
                    out.writeln("return");
                    indent(indent + 2, out);
                    out.writeln("<" + parent + " xsi:type=\"CLUSTER\">");
                    printElementOntolgy(cObject, indent + 3, out);
                    parseCluster((CComplexObject) cObject, indent + 3, out);
                }
                printCComplexObject((CComplexObject) cObject, indent + 2, out, "parts", "$cluster");
                if (!cObject.getArchetype().getNodeText(cObject.getNodeID()).isEmpty()) {
                    indent(indent + 2, out);
                    out.writeln("</" + parent + ">");
                    indent(indent, out);
                    out.writeln("}");
                }
            } else if (cObject.getRmTypeName().compareTo(EN13606Types.ELEMENT.getName()) == 0) {
                if (!cObject.getArchetype().getNodeText(cObject.getNodeID()).isEmpty()) {
                    indent(indent, out);
                    out.writeln("{for $element in " + parentXmlFlatPath + "/*:" + printNodeName(cObject.getArchetype().getNodeText(cObject.getNodeID())));
                    indent(indent + 1, out);
                    out.writeln("return");
                    indent(indent + 2, out);
                    out.writeln("if ($element = \"\")");
                    indent(indent + 3, out);
                    out.writeln("then \"\"");
                    indent(indent + 3, out);
                    out.writeln("else");
                    indent(indent + 4, out);
                    out.writeln("<" + parent + " xsi:type=\"ELEMENT\">");
                    printElementOntolgy(cObject, indent + 5, out);
                }
                getDataTypeObject((CComplexObject) ((CComplexObject) cObject).getAttribute("value").getChildren().get(0), indent + 3, out, "$element");
                if (!cObject.getArchetype().getNodeText(cObject.getNodeID()).isEmpty()) {
                    indent(indent + 4, out);
                    out.writeln("</" + parent + ">");
                    indent(indent + 1, out);
                    out.writeln("}");
                }
            }

        }

    }

    /**
     * Indent output string
     * 
     * @param level
     * @param out
     * @throws IOException 
     */
    private void indent(int level, Out out) throws IOException {
        for (int i = 0; i < level; i++) {
            out.write(indent);
        }
    }

    /**
     * Normalize nod name
     * 
     * @param name
     * @return 
     */
    protected String printNodeName(String name) {
        return Util.removeSpecialChars(name).toLowerCase();
    }

    public static String parseCodedText(CComplexObject pCodedText) {

        ArrayList<String> originalTextList = new ArrayList<String>();
        ArrayList<String> codeValueList = new ArrayList<String>();

        if (pCodedText.getAttribute(EN13606Types.ORIGINAL_TEXT.getName()) != null) {
            if (pCodedText.getAttribute(EN13606Types.ORIGINAL_TEXT.getName()).getChildren().size() > 0) {
                // TODO constraints for originalText have to be processed if set
                if (pCodedText.getAttribute(EN13606Types.ORIGINAL_TEXT.getName()).getChildren().get(0) instanceof org.ehr.am.constraintmodel.CPrimitiveObject) {
//					CPrimitiveObject primitveObject = (CPrimitiveObject) pCodedText.getAttribute(EN13606Types.ORIGINAL_TEXT.getName()).getChildren().get(0);
                }
                if (pCodedText.getAttribute(EN13606Types.ORIGINAL_TEXT.getName()) != null
                        && pCodedText.getAttribute(EN13606Types.ORIGINAL_TEXT.getName()).getChildren().size() > 0) {
                    CPrimitiveObject primitveObject = (CPrimitiveObject) pCodedText.getAttribute(EN13606Types.ORIGINAL_TEXT.getName()).getChildren().get(0);
                    // collect originalText list
                    if (((CString) primitveObject.getItem()).getList() != null && ((CString) primitveObject.getItem()).getList().size() > 0) {
                        for (int i = 0; i < ((CString) primitveObject.getItem()).getList().size(); i++) {
                            originalTextList.add(((CString) primitveObject.getItem()).getList().get(i));
                        }
                    }
                }
            }
        }

        // add originalText and codeValue sets if available
        int size = originalTextList.size();
        if (size < codeValueList.size()) {
            size = codeValueList.size();
        }
        if (size > 1) {
            String codeValue;
            StringBuffer valueList = new StringBuffer();
            for (int i = 0; i < size; i++) {
                if (i < codeValueList.size()) {
                    codeValue = codeValueList.get(i);
                } else {
                    codeValue = "";
                }
                valueList.append(codeValue.toString());
            }
            return valueList.toString();
        }
        return "";
    }

    protected void parseCV(CComplexObject pCV, int indent, Out out, String parentXmlFlatPath) throws IOException {
        if (pCV.getAttribute(EN13606Types.CODING_SCHEME.getName()) != null
                && pCV.getAttribute(EN13606Types.CODING_SCHEME.getName()).getChildren().size() > 0
                && pCV.getAttribute(EN13606Types.CODING_SCHEME.getName()).getChildren().get(0) instanceof CObject) {
            CComplexObject co = (CComplexObject) pCV.getAttribute(EN13606Types.CODING_SCHEME.getName()).getChildren().get(0);
            CSingleAttribute codeValue = (CSingleAttribute) ((CComplexObject) co).getAttribute(EN13606Types.OID.getName());
            //			CPrimitiveObject oidString = (CPrimitiveObject) ((CSingleAttribute)codeValue).getChildren().get(0);
            if (codeValue != null) {
                java.util.List<CObject> stringlist = codeValue.getChildren();
                if (stringlist != null && !stringlist.isEmpty()) {
                    if (stringlist.get(0) instanceof CPrimitiveObject) {
                        CPrimitiveObject cv = (CPrimitiveObject) stringlist.get(0);
                        if (((CString) cv.getItem()).hasAssignedValue()) {
                            String oid = ((CString) cv.getItem()).assignedValue();
                            printSynthesisedSensi(indent, out, "false", "3");
                            indent(indent, out);
                            out.writeln("<value xsi:type=\"CV\">");
                            indent(indent + 1, out);
                            out.writeln("<codingScheme>");
                            indent(indent + 2, out);
                            out.writeln("<oid>" + oid + "</oid>");
                            indent(indent + 1, out);
                            out.writeln("</codingScheme>");
                            indent(indent + 1, out);
                            out.writeln("<codeValue>" + returnXqueryElement(parentXmlFlatPath) + "</codeValue>");
                            indent(indent, out);
                            out.writeln("</value>");
                        }
                    }
                }
            }
        }

    }

    protected void parseCluster(CComplexObject pCV, int indent, Out out) throws IOException {
        if (pCV.getAttribute(EN13606Types.STRUCTURE_TYPE.getName()) != null) {
            CSingleAttribute structureType = (CSingleAttribute) pCV.getAttribute(EN13606Types.STRUCTURE_TYPE.getName());
            CComplexObject value = (CComplexObject) structureType.getChildren().get(0);
            CSingleAttribute codeValue = (CSingleAttribute) value.getAttribute(EN13606Types.CODE_VALUE.getName());
            //			CPrimitiveObject oidString = (CPrimitiveObject) ((CSingleAttribute)codeValue).getChildren().get(0);
            if (codeValue != null) {
                java.util.List<CObject> stringlist = codeValue.getChildren();
                if (stringlist != null && !stringlist.isEmpty()) {
                    if (stringlist.get(0) instanceof CPrimitiveObject) {
                        CPrimitiveObject cv = (CPrimitiveObject) stringlist.get(0);
                        if (((CString) cv.getItem()).hasAssignedValue()) {
                            String strc = ((CString) cv.getItem()).assignedValue();
                            printSynthesisedSensi(indent, out, "false", "3");
                            indent(indent, out);
                            out.writeln("<structure_type>");
                            indent(indent + 1, out);
                            out.writeln("<codingScheme>");
                            indent(indent + 2, out);
                            out.writeln("<oid>2010.05.24.1.10008</oid>");
                            indent(indent + 1, out);
                            out.writeln("</codingScheme>");
                            indent(indent + 1, out);
                            out.writeln("<codeValue>" + strc + "</codeValue>");
                            indent(indent, out);
                            out.writeln("</structure_type>");
                        }
                    }
                }
            }
        }

    }

    protected void parsePQ(CComplexObject pPhysicalQuantity, int indent, Out out, String type, String tag, String parentXmlFlatPath) throws IOException {
        String codeValue = "";
        String codingSchemeName = "";
        if ((pPhysicalQuantity.getAttribute(EN13606Types.UNITS.getName()) != null)
                && (pPhysicalQuantity.getAttribute(EN13606Types.UNITS.getName()).getChildren().size() > 0)) {
            List<CObject> unitList = pPhysicalQuantity.getAttribute(EN13606Types.UNITS.getName()).getChildren();
            for (CObject tempUnit : unitList) {
                CComplexObject tempCS = (CComplexObject) tempUnit;
                if (tempCS.getAttribute(EN13606Types.CODE_VALUE.getName()) != null
                        && !tempCS.getAttribute(EN13606Types.CODE_VALUE.getName()).getChildren().isEmpty()) {
                    CPrimitiveObject primitveObject = (CPrimitiveObject) tempCS.getAttribute(EN13606Types.CODE_VALUE.getName()).getChildren().get(0);
                    CString primitiveString = (CString) primitveObject.getItem().clone();
                    codeValue = primitiveString.getList().toString();

                }
                if (tempCS.getAttribute(EN13606Types.CODING_SCHEME_NAME.getName()) != null
                        && !tempCS.getAttribute(EN13606Types.CODING_SCHEME_NAME.getName()).getChildren().isEmpty()) {
                    CPrimitiveObject primitveObject = (CPrimitiveObject) tempCS.getAttribute(EN13606Types.CODING_SCHEME_NAME.getName()).getChildren().get(0);
                    CString primitiveString = (CString) primitveObject.getItem().clone();
                    codingSchemeName = primitiveString.getList().toString();

                }
            }
        }
        if (pPhysicalQuantity.getAttribute(EN13606Types.VALUE.getName()) != null) {

        }
        printSynthesisedSensi(indent, out, "false", "3");
        indent(indent, out);
        out.writeln("<value xsi:type=\"" + type + "\">");
        indent(indent + 1, out);
        out.writeln("<" + tag + ">" + returnXqueryElement(parentXmlFlatPath) + "</" + tag + ">");
        indent(indent + 1, out);
        out.writeln("<units>" + returnXqueryElement(parentXmlFlatPath));
        indent(indent + 2, out);
        out.writeln("<codingScheme>" + codingSchemeName + "</codingScheme>");
        indent(indent + 2, out);
        out.writeln("<codeValue>" + codeValue + "</codeValue>");
        indent(indent + 1, out);
        out.writeln("</units>");
        indent(indent, out);
        out.writeln("</value>");

    }

    protected void parsePrimitiveType(CComplexObject pInt, int indent, Out out, String type, String tag, String parentXmlFlatPath) throws IOException {
        printSynthesisedSensi(indent, out, "false", "3");
        indent(indent, out);
        out.writeln("<value xsi:type=\"" + type + "\">");
        indent(indent + 1, out);
        out.writeln("<" + tag + ">" + returnXqueryElement(parentXmlFlatPath) + "</" + tag + ">");
        indent(indent, out);
        out.writeln("</value>");
    }

    protected String returnXqueryElement(String parentXmlFlatPath) {
        return "{data(" + parentXmlFlatPath + ")}";
    }

    protected void printSynthesisedSensi(int indent, Out out, String synthesised, String sensitivity) throws IOException {
        if (synthesised != null && !synthesised.trim().equals("")) {
            indent(indent, out);
            out.writeln("<synthesised>" + synthesised + "</synthesised>");
        }
        if (sensitivity != null && !sensitivity.trim().equals("")) {
            indent(indent, out);
            out.writeln("<sensitivity>" + sensitivity + "</sensitivity>");
        }
    }

    /**
     * TO Interpret Data type
     * 
     * @param pObject
     * @param indent
     * @param out
     * @param parentXmlFlatPath
     * @throws IOException 
     */
    protected void getDataTypeObject(CComplexObject pObject, int indent, Out out, String parentXmlFlatPath) throws IOException {

        if (pObject.getRmTypeName().compareTo(EN13606Types.CODED_TEXT.getName()) == 0) {
            parseCV(pObject, indent, out, parentXmlFlatPath);
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.SIMPLE_TEXT.getName()) == 0) {
            parsePrimitiveType(pObject, indent, out, EN13606Types.SIMPLE_TEXT.getName(), "originalText", parentXmlFlatPath);
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.CV.getName()) == 0) {
            parseCV(pObject, indent, out, parentXmlFlatPath);
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.DATE.getName()) == 0) {
            parsePrimitiveType(pObject, indent, out, EN13606Types.DATE.getName(), EN13606Types.DATE.getName().toLowerCase(), parentXmlFlatPath);
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.TS.getName()) == 0) {
            parsePrimitiveType(pObject, indent, out, EN13606Types.TS.getName(), EN13606Types.TIME.getName().toLowerCase(), parentXmlFlatPath);
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.INT.getName()) == 0) {
            parsePrimitiveType(pObject, indent, out, EN13606Types.INT.getName(), EN13606Types.VALUE.getName().toLowerCase(), parentXmlFlatPath);
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.REAL.getName()) == 0) {
            parsePrimitiveType(pObject, indent, out, EN13606Types.REAL.getName(), EN13606Types.VALUE.getName().toLowerCase(), parentXmlFlatPath);
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.PQ.getName()) == 0) {
            parsePQ(pObject, indent, out, EN13606Types.PQ.getName(), EN13606Types.TIME.getName().toLowerCase(), parentXmlFlatPath);
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.DURATION.getName()) == 0) {
        } else {
            return;
        }

    }

    protected String printInterval(Interval interval)
            throws IOException {
        if (interval == null) {
            return null;
        }

        String retorno = null;
        final Comparable lower = interval.getLower();
        final Comparable upper = interval.getUpper();

        if (lower instanceof Integer || upper instanceof Integer) {
            retorno = "inteiro: ";
        } else if (lower instanceof Double || upper instanceof Double) {
            retorno = "real: ";
        } else if (lower instanceof DvDateTime || upper instanceof DvDateTime) {
            retorno = "data_tempo: ";
        } else if (lower instanceof DvDate || upper instanceof DvDate) {
            retorno = "data: ";
        } else if (lower instanceof DvTime || upper instanceof DvTime) {
            retorno = "tempo: ";
        } else if (lower instanceof DvDuration || upper instanceof DvDuration) {
            retorno = "dura��o: ";
        }

        if (lower != null) {
            retorno += (interval.isLowerInclusive() == true ? "[" : "|") + lower.toString() + "..";
        }
        if (upper != null) {
            retorno += upper.toString() + (interval.isUpperInclusive() == true ? "]" : "|");
        }

        return retorno;
    }

    protected String printCDuration(CDuration cduration) {
        String retorno = "";
        if (cduration.getValue() != null) {
            retorno = cduration.getValue().toString();
        }

        if (cduration.getInterval() != null) {
            retorno += cduration.getInterval();
        }

        if (cduration.hasAssumedValue()) {
            retorno += cduration.assumedValue().toString();
        }
        return retorno;
    }

    /**
     * Generete Xquery script to produces Head of ISO 13606 Extract XML
     * 
     * 
     * @param indent
     * @param out
     * @throws IOException 
     */
    private void printExtractHeader(int indent,
            Out out) throws IOException {
        indent(indent, out);
        out.writeln("declare function local:transformXmlFlat($transformXmlFlat)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("let $codSistema := data($transformXmlFlat/*:raiz/*:cod_sistema)");
        indent(indent + 1, out);
        out.writeln("let $subjectOfCare := data($transformXmlFlat/*:raiz/*:paciente)");
        indent(indent + 1, out);
        out.writeln("let $startDate := format-dateTime(current-dateTime(),\"[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]\")");
        indent(indent + 1, out);
        out.writeln("return");
        indent(indent + 2, out);
        out.writeln("<EHR_EXTRACT xmlns=\"CEN/13606/RM\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
        indent(indent + 3, out);
        out.writeln("<ehr_system>");
        printExtensionRoot(out, indent + 3, "{$codSistema}", "2010.05.24.1.10000");
        indent(indent + 3, out);
        out.writeln("</ehr_system>");
        indent(indent + 3, out);
        out.writeln("<ehr_id>");
        printExtensionRoot(out, indent + 3, "{$codSistema}", "2010.05.24.1.10000");
        indent(indent + 3, out);
        out.writeln("</ehr_id>");
        indent(indent + 3, out);
        out.writeln("<subject_of_care>");
        printExtensionRoot(out, indent + 3, "{$subjectOfCare}", "2010.05.24.1.10002");
        indent(indent + 3, out);
        out.writeln("</subject_of_care>");
        indent(indent + 3, out);
        out.writeln("<time_created>");
        indent(indent + 4, out);
        out.writeln("<time>{$startDate}</time>");
        indent(indent + 3, out);
        out.writeln("</time_created>");
        indent(indent + 3, out);
        out.writeln("<rm_id>ISO-13606</rm_id>");
    }

    
    /**
     * Generate Xquery scripts to the End of ISO 13606 extract
     * 
     * @param indent
     * @param out
     * @throws IOException 
     */
    protected void printExtractEnd(int indent, Out out) throws IOException {
        indent(indent + 1, out);
        out.writeln("<criteria>");
        indent(indent + 2, out);
        out.writeln("<time_period>");
        indent(indent + 3, out);
        out.writeln("<low>");
        indent(indent + 4, out);
        out.writeln("<time>2015-01-19T14:14:09</time>");
        indent(indent + 3, out);
        out.writeln("</low>");
        indent(indent + 3, out);
        out.writeln("<high>");
        indent(indent + 4, out);
        out.writeln("<time>2015-01-19T14:14:22</time>");
        indent(indent + 3, out);
        out.writeln("</high>");
        indent(indent + 3, out);
        out.writeln("<lowClosed>true</lowClosed>");
        indent(indent + 3, out);
        out.writeln("<highClosed>true</highClosed>");
        indent(indent + 2, out);
        out.writeln("</time_period>");
        indent(indent + 1, out);
        out.writeln("</criteria>");
        indent(indent + 2, out);
        out.writeln("</EHR_EXTRACT>");
        indent(indent, out);
        out.writeln("};\n");
        indent(indent, out);
        out.writeln("(:Insert name of the document xml flat do transform in extract :)");
        indent(indent, out);
        if (!this.xmlFlat.isEmpty()) {
            out.writeln("for $xmlFlat in doc('" + this.xmlFlat + "')");
        } else {
            out.writeln("for $xmlFlat in doc('XML_Flat_v8')");
        }
        indent(indent, out);
        out.writeln("return local:transformXmlFlat($xmlFlat)");
    }

    /**
     * Generate Xquery function to interpret Flat XML and generate record component
     * @param out
     * @param indent
     * @throws IOException 
     */
    protected void printRecordComponente(Out out, int indent) throws IOException {
        indent(indent, out);
        out.writeln("declare namespace uuid = \"java:java.util.UUID\";");
        indent(indent, out);
        out.writeln("declare function local:recordComponent($pRecoInfo, $structureType)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("let $node := element{'recordComponent'}{}");
        indent(indent + 1, out);
        out.writeln("return");
        //RcId
//	indent(indent+2, out);
//	out.writeln("(:-- OBRIGATORIO :)");   	
//	indent(indent+2, out);
//	out.writeln("copy $rcIdNode := $node");
//	indent(indent+2, out);
//	out.writeln("modify insert node");
//	indent(indent+3, out);
//	out.writeln("local:rcId()");
//	indent(indent+2, out);
//	out.writeln("as first into $rcIdNode");
//	indent(indent+2, out);
//	out.writeln("return ");

        //subject_of_information_category
        indent(indent + 2, out);
        out.writeln("copy $subjectOfCategoryNode := $node");
        indent(indent + 2, out);
        out.writeln("modify insert node");
        indent(indent + 3, out);
        out.writeln("if ($structureType = \"ENTRY\" and not(empty($pRecoInfo/*:categoria_informacao)) and not(data($pRecoInfo/*:categoria_informacao) = \"\")) then local:subjectOfCategory($pRecoInfo/*:categoria_informacao)");
        indent(indent + 3, out);
        out.writeln("else \"\"");
        indent(indent + 2, out);
        out.writeln("as first into $subjectOfCategoryNode");
        indent(indent + 2, out);
        out.writeln("return ");

        //uncertainty_expressed
        indent(indent + 2, out);
        out.writeln("(:-- OBRIGATORIO :)");
        indent(indent + 2, out);
        out.writeln("copy $uncertaintyNode := $subjectOfCategoryNode");
        indent(indent + 2, out);
        out.writeln("modify insert node");
        indent(indent + 3, out);
        out.writeln("if ($structureType = \"ENTRY\") then <uncertainty_expressed>false</uncertainty_expressed>(:VALOR FIXADO, DEFINIR COMO DEVERÁ SER PREENCHIDO:)");
        indent(indent + 3, out);
        out.writeln("else \"\"");
        indent(indent + 2, out);
        out.writeln("as first into $uncertaintyNode");
        indent(indent + 2, out);
        out.writeln("return");

        //other_participations
        indent(indent + 2, out);
        out.writeln("copy $otherPartNode := $uncertaintyNode");
        indent(indent + 2, out);
        out.writeln("modify insert node");
        indent(indent + 3, out);
        out.writeln("if ($structureType = \"COMPOSITION\" and not(empty($pRecoInfo/*:outros_participantes)) and not(data($pRecoInfo/*:outros_participantes) = \"\")) then local:otherParticipations($pRecoInfo/*:outros_participantes)");
        indent(indent + 3, out);
        out.writeln("else \"\"");
        indent(indent + 2, out);
        out.writeln("as first into $otherPartNode");
        indent(indent + 2, out);
        out.writeln("return");

        //Composer
        indent(indent + 2, out);
        out.writeln("copy $composer := $otherPartNode");
        indent(indent + 2, out);
        out.writeln("modify insert node");
        indent(indent + 3, out);
        out.writeln("if ($structureType = \"COMPOSITION\" and not(empty($pRecoInfo/*:responsabilidade)) and not(data($pRecoInfo/*:responsabilidade) = \"\")) then local:composer($pRecoInfo/*:responsabilidade)");
        indent(indent + 3, out);
        out.writeln("else \"\"");
        indent(indent + 2, out);
        out.writeln("as first into $composer");
        indent(indent + 2, out);
        out.writeln("return");

        //Committal - 
        indent(indent + 2, out);
        out.writeln("(:-- OBRIGATORIO :)");
        indent(indent + 2, out);
        out.writeln("copy $committal := $composer");
        indent(indent + 2, out);
        out.writeln("modify insert node");
        indent(indent + 3, out);
        out.writeln("if ($structureType = \"COMPOSITION\" and not(empty($pRecoInfo/*:informacoes_de_envio)) and not(data($pRecoInfo/*:informacoes_de_envio) = \"\")) then local:committal($pRecoInfo/*:informacoes_de_envio)");
        indent(indent + 3, out);
        out.writeln("else \"\"");
        indent(indent + 2, out);
        out.writeln("as first into $committal");
        indent(indent + 2, out);
        out.writeln("return");

        //Session_time
        indent(indent + 2, out);
        out.writeln("copy $sessionTime := $committal");
        indent(indent + 2, out);
        out.writeln("modify insert node");
        indent(indent + 3, out);
        out.writeln("if ($structureType = \"COMPOSITION\" and not(empty($pRecoInfo/*:duracao)) and not(data($pRecoInfo/*:duracao) = \"\")) then local:sessionTime($pRecoInfo/*:duracao)");
        indent(indent + 3, out);
        out.writeln("else \"\"");
        indent(indent + 2, out);
        out.writeln("as first into $sessionTime");
        indent(indent + 2, out);
        out.writeln("return");

        //Sensitivity
        indent(indent + 2, out);
        out.writeln("copy $sensitivityNode := $sessionTime");
        indent(indent + 2, out);
        out.writeln("modify insert node");
        indent(indent + 3, out);
        out.writeln("if (empty(data($pRecoInfo/*:nivel_sensibilidade)) or data($pRecoInfo/*:nivel_sensibilidade) = \"\")");
        indent(indent + 4, out);
        out.writeln("then \"\"");
        indent(indent + 3, out);
        out.writeln("else");
        indent(indent + 4, out);
        out.writeln("<sensitivity>{data($pRecoInfo/*:nivel_sensibilidade)}</sensitivity>");
        indent(indent + 2, out);
        out.writeln("as first into $sensitivityNode");
        indent(indent + 2, out);
        out.writeln("return ");

        //Synthesised
        indent(indent + 2, out);
        out.writeln("copy $synthesisedNode := $sensitivityNode");
        indent(indent + 2, out);
        out.writeln("modify insert node");
        indent(indent + 3, out);
        out.writeln("if (empty(data($pRecoInfo/*:synthesised)) or data($pRecoInfo/*:synthesised) = \"\")");
        indent(indent + 4, out);
        out.writeln("then \"\"");
        indent(indent + 3, out);
        out.writeln("else");
        indent(indent + 4, out);
        out.writeln("<synthesised>{data($pRecoInfo/*:synthesised)}</synthesised>");
        indent(indent + 2, out);
        out.writeln("as first into $synthesisedNode");
        indent(indent + 2, out);
        out.writeln("return $synthesisedNode/*");

        //attestations it's not obligatory
        //content it's not obligatory
        //infro provider não é obrigatorio 
//	indent(indent+2, out);
//	out.writeln("copy $subjectOfCategoryNode := $uncertaintyNode");
//	indent(indent+2, out);
//	out.writeln("modify insert node");
//	indent(indent+3, out);
//	out.writeln("if ($structureType = \"ENTRY\") then local:infoProvider($performer, $healthcareFacillity )$pRecoInfo/*:categoria_informacao)");   	
//	indent(indent+3, out);
//	out.writeln("else \"\"");   	
//	indent(indent+2, out);
//	out.writeln("as first into $subjectOfCategoryNode");
//	indent(indent+2, out);
//	out.writeln("return");	
        //itens abaixo não são obrigatorios e não foram implementados
//	<xs:element name="subject_of_information" type="RELATED_PARTY" minOccurs="0"/>
//	<xs:element name="other_participations" type="FUNCTIONAL_ROLE" minOccurs="0" maxOccurs="unbounded"/>
        indent(indent, out);
        out.writeln("};\n");
        indent(indent, out);
        out.writeln("declare function local:rcId()");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 2, out);
        out.writeln("<rc_id>");
        printExtensionRoot(out, indent + 2, "{data(uuid:randomUUID())}", "2010.05.24.1.10003");
        indent(indent + 2, out);
        out.writeln("</rc_id>");
        indent(indent, out);
        out.writeln("};\n");
        //Session time
        indent(indent, out);
        out.writeln("declare function local:sessionTime($duracao)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("let $sessionTime := $duracao");
        indent(indent + 1, out);
        out.writeln("return");
        indent(indent + 2, out);
        out.writeln("if (data($duracao) = \"\")");
        indent(indent + 2, out);
        out.writeln("then \"\"");
        indent(indent + 2, out);
        out.writeln("else ");
        indent(indent + 3, out);
        out.writeln("<session_time>");
        indent(indent + 4, out);
        out.writeln("{if (data($duracao/*:data_hora_inicio) = \"\")");
        indent(indent + 4, out);
        out.writeln("then \"\"");
        indent(indent + 4, out);
        out.writeln("else ");
        indent(indent + 5, out);
        out.writeln("<low>");
        indent(indent + 6, out);
        out.writeln("<time>{data($duracao/*:data_hora_inicio)}</time>");
        indent(indent + 5, out);
        out.writeln("</low>");
        indent(indent + 4, out);
        out.writeln("}");
        indent(indent + 4, out);
        out.writeln("{if (data($duracao/*:data_hora_fim) = \"\")");
        indent(indent + 4, out);
        out.writeln("then \"\"");
        indent(indent + 4, out);
        out.writeln("else ");
        indent(indent + 5, out);
        out.writeln("<high>");
        indent(indent + 6, out);
        out.writeln("<time>{data($duracao/*:data_hora_fim)}</time>");
        indent(indent + 5, out);
        out.writeln("</high>");
        indent(indent + 4, out);
        out.writeln("}");
        //Intervalo não é implementado
//	indent(indent+2, out);
//	out.writeln("<lowClosed>true</lowClosed>");
//	indent(indent+2, out);
//	out.writeln("<highClosed>true</highClosed>");
        indent(indent + 1, out);
        out.writeln("</session_time>");
        indent(indent, out);
        out.writeln("};\n");

        indent(indent, out);
        out.writeln("declare function local:subjectOfCategory($codeValue)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("<subject_of_information_category>");
        indent(indent + 2, out);
        out.writeln("<codingScheme>");
        indent(indent + 3, out);
        out.writeln("<oid>2010.05.24.1.10009</oid>");
        indent(indent + 2, out);
        out.writeln("</codingScheme>");
        indent(indent + 2, out);
        out.writeln("<codeValue>{data($codeValue)}</codeValue>");
        indent(indent + 1, out);
        out.writeln("</subject_of_information_category>");
        indent(indent, out);
        out.writeln("};\n");
        indent(indent, out);
        out.writeln("declare function local:infoProvider($performer, $healthcareFacillity )");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("<info_provider>");
        indent(indent + 2, out);
        out.writeln("<performer>");
        printExtensionRoot(out, indent + 2, "{data($performer)}", "2010.05.24.1.10006");
        indent(indent + 2, out);
        out.writeln("</performer>");
        indent(indent + 2, out);
        out.writeln("<healthcare_facillity>");
        printExtensionRoot(out, indent + 2, "{data($healthcareFacillity)}", "2010.05.24.1.10007");
        indent(indent + 2, out);
        out.writeln("</healthcare_facillity>");
        indent(indent + 1, out);
        out.writeln("</info_provider>");
        indent(indent, out);
        out.writeln("};\n");
        indent(indent, out);

        //Committal
        out.writeln("declare function local:committal($informacoes_de_envio)");
        out.writeln("{\n");
        indent(indent + 1, out);
        out.writeln("let $infEnvio := $informacoes_de_envio");
        indent(indent + 1, out);
        out.writeln("return");
        indent(indent + 2, out);
        out.writeln("if (empty(data($infEnvio)) or data($infEnvio) = \"\")");
        indent(indent + 3, out);
        out.writeln("then \"\"");
        indent(indent + 3, out);
        out.writeln("else");
        indent(indent + 4, out);
        out.writeln("<committal>");
        indent(indent + 5, out);
        out.writeln("<ehr_system>");
        indent(indent + 6, out);
        out.writeln("<extension>{data($infEnvio/*:cod_sistema)}</extension>");
        indent(indent + 6, out);
        out.writeln("<root>");
        indent(indent + 7, out);
        out.writeln("<oid>2010.05.24.1.10000</oid>");
        indent(indent + 6, out);
        out.writeln("</root>");
        indent(indent + 5, out);
        out.writeln("</ehr_system>");
        indent(indent + 5, out);
        out.writeln("<time_committed>");
        indent(indent + 6, out);
        out.writeln("<time>{data($infEnvio/*:data_hora_gravacao)}</time>");
        indent(indent + 5, out);
        out.writeln("</time_committed>");
        indent(indent + 5, out);
        out.writeln("<committer>");
        indent(indent + 6, out);
        out.writeln("<extension>{data($infEnvio/*:usuario_de_gravacao)}</extension>");
        indent(indent + 7, out);
        out.writeln("<root>");
        indent(indent + 7, out);
        out.writeln("<oid>2010.05.24.1.10004</oid>");
        indent(indent + 6, out);
        out.writeln("</root>");
        indent(indent + 5, out);
        out.writeln("</committer>");
        indent(indent + 5, out);
        out.writeln("{if (empty(data($infEnvio/*:versao)) or data($infEnvio/*:versao) = \"\")");
        indent(indent + 6, out);
        out.writeln("then \"\"");
        indent(indent + 6, out);
        out.writeln("else");
        indent(indent + 7, out);
        out.writeln("<version_status>");
        indent(indent + 8, out);
        out.writeln("<codingScheme>");
        indent(indent + 9, out);
        out.writeln("<oid>2010.05.24.1.10005</oid>");
        indent(indent + 8, out);
        out.writeln("</codingScheme>");
        indent(indent + 8, out);
        out.writeln("<codeValue>{data($infEnvio/*:versao)}</codeValue>");
        indent(indent + 7, out);
        out.writeln("</version_status>");
        indent(indent + 5, out);
        out.writeln("}");
        indent(indent + 5, out);
        out.writeln("{if (empty(data($infEnvio/*:revisao)) or data($infEnvio/*:revisao) = \"\")");
        indent(indent + 6, out);
        out.writeln("then \"\"");
        indent(indent + 6, out);
        out.writeln("else");
        indent(indent + 7, out);
        out.writeln("<reason_for_revision>");
        indent(indent + 8, out);
        out.writeln("<codingScheme>");
        indent(indent + 9, out);
        out.writeln("<oid>2010.05.24.1.10015</oid>");
        indent(indent + 8, out);
        out.writeln("</codingScheme>");
        indent(indent + 8, out);
        out.writeln("<codeValue>{data($infEnvio/*:revisao)}</codeValue>");
        indent(indent + 7, out);
        out.writeln("</reason_for_revision>");
        indent(indent + 5, out);
        out.writeln("}");
        indent(indent + 4, out);
        out.writeln("</committal>");
        indent(indent, out);
        out.writeln("};");
        //Composer
        out.writeln("declare function local:composer($composer)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("let $performer := data($composer/*:responsavel)");
        indent(indent + 1, out);
        out.writeln("let $healthcareFacillity := data($composer/*:organizacao_de_saude)");
        indent(indent + 1, out);
        out.writeln("return <composer>");
        indent(indent + 3, out);
        out.writeln("<performer>");
        printExtensionRoot(out, indent + 3, "{$performer}", "2010.05.24.1.10006");
        indent(indent + 3, out);
        out.writeln("</performer>");
        indent(indent + 3, out);
        out.writeln("<healthcare_facillity>");
        printExtensionRoot(out, indent + 3, "{$healthcareFacillity}", "2010.05.24.1.10007");
        indent(indent + 3, out);
        out.writeln("</healthcare_facillity>");
        indent(indent + 2, out);
        out.writeln("</composer>");
        indent(indent, out);
        out.writeln("};\n");

        //Other participations
        out.writeln("declare function local:otherParticipations($pOther)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("for $otherPartc in $pOther");
        indent(indent + 1, out);
        out.writeln("let $performer := data($otherPartc/*:id)");
        indent(indent + 1, out);
        out.writeln("let $healthcareFacillity := data($otherPartc/*:oraganizacao_de_saude)");
        indent(indent + 1, out);
        out.writeln("return <other_participations>");
        indent(indent + 3, out);
        out.writeln("<performer>");
        printExtensionRoot(out, indent + 3, "{$performer}", "2010.05.24.1.10006");
        indent(indent + 3, out);
        out.writeln("</performer>");
        indent(indent + 3, out);
        out.writeln("<healthcare_facillity>");
        printExtensionRoot(out, indent + 3, "{$healthcareFacillity}", "2010.05.24.1.10007");
        indent(indent + 3, out);
        out.writeln("</healthcare_facillity>");
        indent(indent + 2, out);
        out.writeln("</other_participations>");
        indent(indent, out);
        out.writeln("};\n");
    }

    /**
     * Generate the ISO 13606 extract tag for the element througt ontology
     * @param cObject
     * @param indent
     * @param out
     * @throws IOException 
     */
    private void printElementOntolgy(CObject cObject, int indent,
            Out out) throws IOException {
        if (StringUtils.isNotEmpty(cObject.getArchetype().getNodeText(cObject.getNodeID()))) {
            indent(indent, out);
            out.writeln("<name xsi:type=\"SIMPLE_TEXT\">");
            indent(indent + 1, out);
            out.writeln("<originalText>" + cObject.getArchetype().getNodeText(cObject.getNodeID()) + "</originalText>");
            indent(indent, out);
            out.writeln("</name>");
            indent(indent, out);
            if (cObject.getRmTypeName().equalsIgnoreCase(EN13606Types.COMPOSITION.getName())) {
                out.writeln("<archetype_id>" + cObject.getArchetype().getArchetypeId().getValue() + "</archetype_id>");
            } else {
                out.writeln("<archetype_id>" + cObject.getArchetype().getArchetypeId().getValue() + "/" + cObject.getNodeID() + "</archetype_id>");
            }
            indent(indent, out);
            out.writeln("<rc_id>");
            indent(indent + 1, out);
            out.writeln("<extension>{data(uuid:randomUUID())}</extension>");
            indent(indent + 1, out);
            out.writeln("<root>");
            indent(indent + 2, out);
            out.writeln("<oid>2010.05.24.1.10003</oid>");
            indent(indent + 1, out);
            out.writeln("</root>");
            indent(indent, out);
            out.writeln("</rc_id>");
        }
    }

    @Override
    public String toString() {
        return this.out.toString();
    }

}
