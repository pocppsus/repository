package com.ufmg.ppsus.xmlflat_parser.XMLFlatModelGenerator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ehr.am.Archetype;
import org.ehr.am.constraintmodel.CAttribute;
import org.ehr.am.constraintmodel.CComplexObject;
import org.ehr.am.constraintmodel.CMultipleAttribute;
import org.ehr.am.constraintmodel.CObject;
import org.ehr.am.constraintmodel.CPrimitiveObject;
import org.ehr.am.constraintmodel.primitive.CDate;
import org.ehr.am.constraintmodel.primitive.CDateTime;
import org.ehr.am.constraintmodel.primitive.CDuration;
import org.ehr.am.constraintmodel.primitive.CInteger;
import org.ehr.am.constraintmodel.primitive.CPrimitive;
import org.ehr.am.constraintmodel.primitive.CReal;
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
 * Generate the XSD Schema to validate Flat XML 
 * 
 * @author Fabio Elias
 */
public class XSDGenerator {

    /* charset encodings */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    /* fields */
    private Charset encoding;

    private String indent;

    private Out out;

    /**
     * Create an outputter with default encoding, indent and lineSeparator
     */
    public XSDGenerator() {
        this.encoding = LATIN1;
        this.indent = "    "; // 4 white space characters
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
     * Output given XSD Schema as string for the archetype
     *
     * @param archetype
     * @return a string XSD Schema
     * @throws IOException
     * @throws ParseADLException, admin@tuta.pos
     */
    public String output(String archetype, String path) throws IOException, ParseADLException {
        Archetype conceptArchetype = IO.loadADL(path + "/" + archetype);
        this.out = new Out();
        parse(conceptArchetype, out);
        System.out.println(out.toString());
        return out.toString();
    }

    /**
     * Output given archetype to writer
     *
     * @param archetype
     * @param out
     * @throws IOException
     */
    public void parse(Archetype archetype, Out out) throws IOException {
        out.writeln("<?xml version=\"1.0\" encoding=\"" + encoding.name() + "\"?>\n");
        out.writeln("<xs:schema  xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.w3schools.com\" elementFormDefault=\"qualified\" id=\"" + archetype.getArchetypeId() + ".xsd\">");
        int indent = 0;
        indent(++indent, out);
        out.writeln("<xs:element name=\"raiz\">");
        indent(++indent, out);
        out.writeln("<xs:complexType>");
        indent(++indent, out);
        out.writeln("<xs:sequence>");
        indent(++indent, out);
        out.writeln("<xs:element name=\"cod_sistema\" minOccurs=\"1\" maxOccurs=\"1\"/>");
        out.writeln("<xs:element name=\"paciente\" minOccurs=\"1\" maxOccurs=\"1\" />");
        //Explode definition of the archetype
        printDefinition(archetype.getDefinition(), out, indent);
        indent(--indent, out);
        out.writeln("</xs:sequence>");
        indent(--indent, out);
        out.writeln("</xs:complexType>");
        indent(--indent, out);
        out.writeln("</xs:element>");
        out.writeln("</xs:schema>");
    }

    /**
     * Interpret the root structure of the achetype
     *
     * @param definition
     * @param out
     * @param indent
     * @throws IOException
     */
    protected void printDefinition(CComplexObject definition, Out out, int indent)
            throws IOException {
        printCObject(definition, indent, out, "", "", false, false);
    }

    /**
     * Generate the tag of element
     * @param cobj
     * @param indent
     * @param out
     * @throws IOException 
     */
    protected void printCObjectElements(CObject cobj, int indent, Out out)
            throws IOException {
        printEmptyString(cobj.getRmTypeName(), cobj.getRmTypeName(), indent, out);

    }

    /**
     * Generate the element tag with value
     * 
     * @param label
     * @param value
     * @param indent
     * @param out
     * @throws IOException 
     */
    private void printEmptyString(String label, String value, int indent,
            Out out) throws IOException {
        indent(indent, out);
        out.write("<" + label + ">");
        out.write(value);
        out.writeln("</" + label + ">");
    }

    /**
     * Interpret the object and your attributes
     * 
     * @param ccobj
     * @param indent
     * @param out
     * @param existence
     * @param cardinality
     * @throws IOException 
     */
    protected void printCComplexObject(CComplexObject ccobj, int indent,
            Out out, String existence, String cardinality) throws IOException {
        // print all attributes
        if (!ccobj.isAnyAllowed()) {
            for (CAttribute cattribute : ccobj.getAttributes()) {
                if (!cattribute.getRmAttributeName().contains("value")) {
                    printCAttribute(cattribute, indent, out, existence, cardinality);
                }
            }
        }
    }

    /**
     * Generate cardinality and interpret children element of the object
     * @param cattribute
     * @param indent
     * @param out
     * @param existence
     * @param cardinality
     * @throws IOException 
     */
    protected void printCAttribute(CAttribute cattribute, int indent, Out out, String existence, String cardinality)
            throws IOException {

        if (!cattribute.isAnyAllowed()) {
            List<CObject> children = cattribute.getChildren();

            boolean isMultipleAttribute = false;
            Boolean isOrdered = false;
            Boolean isUnique = false;

            if (cattribute instanceof CMultipleAttribute) {
                isMultipleAttribute = true;
                isOrdered = ((CMultipleAttribute) cattribute).getCardinality().isOrdered();
                isUnique = ((CMultipleAttribute) cattribute).getCardinality().isUnique();
            }

            if (isMultipleAttribute) {
                cardinality = printCardinalityInterval(((CMultipleAttribute) cattribute).getCardinality().getInterval());
            }
            //If elment has children's, it is a complex item and your son's  will be interpreted
            if (children.size() > 1
                    || !(children.get(0) instanceof CPrimitiveObject)) {
                //Create XSD tag if the element should be ordered as the same away which the archetype 
                if (isOrdered
                        && (cattribute.getRmAttributeName().compareTo(EN13606Types.MEMBERS.getName()) == 0
                        || cattribute.getRmAttributeName().compareTo(EN13606Types.ITEMS.getName()) == 0
                        || cattribute.getRmAttributeName().compareTo(EN13606Types.CONTENT.getName()) == 0
                        || cattribute.getRmAttributeName().compareTo(EN13606Types.PARTS.getName()) == 0)) {
                    indent(++indent, out);
                    out.writeln("<xs:sequence>");
                }
                //Call constructor of record component tag's
                printRecordComponent(out, ++indent);
                for (CObject cobject : cattribute.getChildren()) {
                    //call the interpreter of the element
                    printCObject(cobject, indent, out, existence, cardinality, isOrdered, isUnique);
                }
                //Close XSD tag if the element should be ordered as the same away which the archetype
                if (isOrdered
                        && (cattribute.getRmAttributeName().compareTo(EN13606Types.MEMBERS.getName()) == 0
                        || cattribute.getRmAttributeName().compareTo(EN13606Types.ITEMS.getName()) == 0
                        || cattribute.getRmAttributeName().compareTo(EN13606Types.CONTENT.getName()) == 0
                        || cattribute.getRmAttributeName().compareTo(EN13606Types.PARTS.getName()) == 0)) {
                    indent(indent, out);
                    out.writeln("</xs:sequence>");
                }
            }
        }

    }

    /**
     * Generate xs:complexType if: 
     *      The object it is a structural elment COMPOSITION, ENTRY, SECTION, CLUSTER
     * or Generate xs:element if:
     *      The object it is DataType  
     * @param cObject
     * @param indent
     * @param out
     * @param existence
     * @param cardinality
     * @param isOrdered
     * @param isUnique
     * @throws IOException 
     */
    protected void printCObject(CObject cObject, int indent,
            Out out, String existence, String cardinality, boolean isOrdered, boolean isUnique) throws IOException {
        if (cObject instanceof CComplexObject) {
            String ocurrences = printCardinalityInterval(cObject.getOccurrences());
            //The object it is a structural elment COMPOSITION, ENTRY, SECTION, CLUSTER
            if (cObject.getRmTypeName().compareTo(EN13606Types.COMPOSITION.getName()) == 0
                    || cObject.getRmTypeName().compareTo(EN13606Types.ENTRY.getName()) == 0
                    || cObject.getRmTypeName().compareTo(EN13606Types.SECTION.getName()) == 0
                    || cObject.getRmTypeName().compareTo(EN13606Types.CLUSTER.getName()) == 0) {
                //Open tags
                if (!cObject.getArchetype().getNodeText(cObject.getNodeID()).isEmpty()) {
                    indent(++indent, out);
                    out.writeln("<xs:element name=\"" + printNodeName(cObject.getArchetype().getNodeText(cObject.getNodeID())) + "\" " + ocurrences + " >");
                    indent(++indent, out);
                    out.writeln("<xs:complexType>");
                }
                //interpret the object and yours attributes
                printCComplexObject((CComplexObject) cObject, ++indent, out, "", "");
                //Close tags
                if (!cObject.getArchetype().getNodeText(cObject.getNodeID()).isEmpty()) {
                    indent(--indent, out);
                    out.writeln("</xs:complexType>");
                    indent(--indent, out);
                    out.writeln("</xs:element>");
                }
            //The object it is DataType  
            } else if (cObject.getRmTypeName().compareTo(EN13606Types.ELEMENT.getName()) == 0) {
                indent(++indent, out);
                out.writeln("<xs:element name=\"" + printNodeName(cObject.getArchetype().getNodeText(cObject.getNodeID())) + "\" type=\"" + getDataType((CComplexObject) ((CComplexObject) cObject).getAttribute("value").getChildren().get(0), indent + 3, out, "$element") + "\" " + ocurrences + "/>");
            }
        }
    }

    /**
     * Generate the elements tag's of the record component
     * @param out
     * @param indent
     * @throws IOException 
     */
    protected void printRecordComponent(Out out, int indent) throws IOException {
        indent(indent, out);
        out.writeln("<xs:element name=\"informacoes_de_auditoria\" minOccurs=\"0\" maxOccurs=\"1\">");
        indent(++indent, out);
        out.writeln("<xs:complexType>");
        indent(++indent, out);
        out.writeln("<xs:all>");

        indent(++indent, out);
        out.writeln("<xs:element name=\"responsabilidade\" minOccurs=\"0\" maxOccurs=\"1\"><!--Composer-->");
        indent(++indent, out);
        out.writeln("<xs:complexType>");
        indent(++indent, out);
        out.writeln("<xs:all>");
        indent(++indent, out);
        out.writeln("<xs:element name=\"organizacao_de_saude\" minOccurs=\"0\" maxOccurs=\"1\"/><!--Helthcare facility-->");
        indent(indent, out);
        out.writeln("<xs:element name=\"responsavel\" minOccurs=\"0\" maxOccurs=\"1\"/><!--Performer-->");
        indent(--indent, out);
        out.writeln("</xs:all>");
        indent(--indent, out);
        out.writeln("</xs:complexType>");
        indent(--indent, out);
        out.writeln("</xs:element>");

        indent(indent, out);
        out.writeln("<xs:element name=\"nivel_sensibilidade\" minOccurs=\"0\" maxOccurs=\"1\" /><!--Sensistivity-->");
        indent(indent, out);
        out.writeln("<xs:element name=\"synthesised\" minOccurs=\"0\" maxOccurs=\"1\"/>");
        indent(indent, out);
        out.writeln("<xs:element name=\"territory\" minOccurs=\"0\" maxOccurs=\"1\"/>");
        indent(indent, out);
        out.writeln("<xs:element name=\"categoria_informacao\" minOccurs=\"0\" maxOccurs=\"1\"/>");

        indent(indent, out);
        out.writeln("<xs:element name=\"outros_participantes\" minOccurs=\"0\" maxOccurs=\"1\"><!--other participations-->");
        indent(++indent, out);
        out.writeln("<xs:complexType>");
        indent(++indent, out);
        out.writeln("<xs:all>");
        indent(++indent, out);
        out.writeln("<xs:element name=\"participante\" minOccurs=\"0\" maxOccurs=\"1\"/>");
        indent(indent, out);
        out.writeln("<xs:element name=\"organizacao_de_saude\" minOccurs=\"0\" maxOccurs=\"1\"/>");
        indent(--indent, out);
        out.writeln("</xs:all>");
        indent(--indent, out);
        out.writeln("</xs:complexType>");
        indent(--indent, out);
        out.writeln("</xs:element>");

        indent(indent, out);
        out.writeln("<xs:element name=\"duracao\" minOccurs=\"0\" maxOccurs=\"1\"><!--session_time(Composition)-->");
        indent(++indent, out);
        out.writeln("<xs:complexType>");
        indent(++indent, out);
        out.writeln("<xs:all>");
        indent(++indent, out);
        out.writeln("<xs:element name=\"data_hora_inicio\" minOccurs=\"0\" maxOccurs=\"1\"/>");
        indent(indent, out);
        out.writeln("<xs:element name=\"data_hora_fim\" minOccurs=\"0\" maxOccurs=\"1\"/>");
        indent(--indent, out);
        out.writeln("</xs:all>");
        indent(--indent, out);
        out.writeln("</xs:complexType>");
        indent(--indent, out);
        out.writeln("</xs:element>");

        indent(indent, out);
        out.writeln("<xs:element name=\"informacoes_de_envio\" minOccurs=\"0\" maxOccurs=\"1\"><!--committal-->");
        indent(++indent, out);
        out.writeln("<xs:complexType>");
        indent(++indent, out);
        out.writeln("<xs:all>");
        indent(++indent, out);
        out.writeln("<xs:element name=\"cod_sistema\" minOccurs=\"0\" maxOccurs=\"1\"/>");
        indent(indent, out);
        out.writeln("<xs:element name=\"usuario_de_gravacao\" minOccurs=\"0\" maxOccurs=\"1\"/>");
        indent(indent, out);
        out.writeln("<xs:element name=\"data_hora_gravacao\" minOccurs=\"0\" maxOccurs=\"1\"/>");
        indent(indent, out);
        out.writeln("<xs:element name=\"versao\" minOccurs=\"0\" maxOccurs=\"1\"/>");
        indent(indent, out);
        out.writeln("<xs:element name=\"revisao\" minOccurs=\"0\" maxOccurs=\"1\"/>");
        indent(--indent, out);
        out.writeln("</xs:all>");
        indent(--indent, out);
        out.writeln("</xs:complexType>");
        indent(--indent, out);
        out.writeln("</xs:element>");
        indent(--indent, out);
        out.writeln("</xs:all>");
        indent(--indent, out);
        out.writeln("</xs:complexType>");
        indent(indent, out);
        out.writeln("</xs:element>");
    }

    /**
     * Interpret the data type 
     * @param pObject
     * @param indent
     * @param out
     * @param parentXmlFlatPath
     * @return xs:type of the element
     * @throws IOException 
     */
    protected String getDataType(CComplexObject pObject, int indent, Out out, String parentXmlFlatPath) throws IOException {

        if (pObject.getRmTypeName().compareTo(EN13606Types.CODED_TEXT.getName()) == 0) {
            return "xs:string";
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.SIMPLE_TEXT.getName()) == 0) {
            return "xs:string";
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.CV.getName()) == 0) {
            return "xs:string";
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.DATE.getName()) == 0) {
            return "xs:date";
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.TS.getName()) == 0) {
            return "xs:dateTime";
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.INT.getName()) == 0) {
            return "xs:integer";
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.REAL.getName()) == 0) {
            return "xs:double";
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.PQ.getName()) == 0) {
            return "xs:integer";
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.DURATION.getName()) == 0) {
            return "xs:duration";
        } else {
            return "";
        }

    }

    /**
     * Ident return output string
     * @param level
     * @param out
     * @throws IOException 
     */
    private void indent(int level, Out out) throws IOException {
        for (int i = 0; i < level; i++) {
            out.append(indent);
        }
    }

    /**
     * Normalize node name
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
//				if (pCodedText.getAttribute(EN13606Types.ORIGINAL_TEXT.getName()).getChildren().get(0) instanceof org.ehr.am.constraintmodel.CPrimitiveObject) {
//					CPrimitiveObject primitveObject = (CPrimitiveObject) pCodedText.getAttribute(EN13606Types.ORIGINAL_TEXT.getName()).getChildren().get(0);
//				}
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

    /**
     * 
     * @param pSimpleText
     * @return 
     */
    public String parseSimpleTextTerminology(CComplexObject pSimpleText) {
        if (pSimpleText.getAttribute(EN13606Types.ORIGINAL_TEXT.getName()) != null
                && pSimpleText.getAttribute(EN13606Types.ORIGINAL_TEXT.getName()).getChildren().size() > 0) {
            CPrimitiveObject primitveObject = (CPrimitiveObject) pSimpleText.getAttribute(EN13606Types.ORIGINAL_TEXT.getName()).getChildren().get(0);
            if (((CString) primitveObject.getItem()).getList() != null && ((CString) primitveObject.getItem()).getList().size() > 0) {
                return ((CString) primitveObject.getItem()).getList().toString();
            } else {
                return "[TEXTO LIVRE]";
            }

        }
        return null;
    }

    /**
     * Interpret data type
     * 
     * @param pObject
     * @return 
     */
    public String getDataTypeObject(CComplexObject pObject) {
        if (pObject.getRmTypeName().compareTo(EN13606Types.CODED_TEXT.getName()) == 0) {
            return parseCodedText(pObject);
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.SIMPLE_TEXT.getName()) == 0) {
            return parseSimpleTextTerminology(pObject);
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.CV.getName()) == 0) {
            return "[" + pObject.getArchetype().getNodeText(pObject.getNodeID()) + "]";
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.DATE.getName()) == 0) {
            if (!pObject.getAttribute("date").getChildren().isEmpty()) {
                CPrimitive cp = ((CPrimitiveObject) pObject.getAttribute("date").getChildren().get(0)).getItem();
                return "[" + ((CDate) cp).FULL_PATTERN + "/" + ((CDate) cp).SHORT_PATTERN + "]";
            }
            return null;
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.TS.getName()) == 0) {
            if (!pObject.getAttribute("time").getChildren().isEmpty()) {
                CPrimitive cp = ((CPrimitiveObject) pObject.getAttribute("time").getChildren().get(0)).getItem();
                return "[" + ((CDateTime) cp).getPattern() + "]";
            }
            return null;
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.PQ.getName()) == 0) {
            if (pObject.getAttribute(EN13606Types.UNITS.getName()) != null) {
                CPrimitive csList = ((CPrimitiveObject) ((CComplexObject) pObject.getAttribute(EN13606Types.UNITS.getName()).getChildren().get(0)).getAttribute("codeValue").getChildren().get(0)).getItem();
                CPrimitive cs = ((CPrimitiveObject) ((CComplexObject) pObject.getAttribute(EN13606Types.UNITS.getName()).getChildren().get(0)).getAttribute("codingSchemeName").getChildren().get(0)).getItem();
                return "{Unidade: " + ((CString) cs).getList().toString() + ", Frequencia: " + ((CString) csList).getList().toString() + "}";
            }
            return null;
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.INT.getName()) == 0) {
            if (pObject.getAttribute(EN13606Types.VALUE.getName()) != null
                    && !pObject.getAttribute(EN13606Types.VALUE.getName()).getChildren().isEmpty()) {
                CPrimitive cp = ((CPrimitiveObject) pObject.getAttribute(EN13606Types.VALUE.getName()).getChildren().get(0)).getItem();
                try {
                    return printInterval(((CInteger) cp).getInterval());
                } catch (IOException e) {
                }
            }
            return null;
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.REAL.getName()) == 0) {
            if (pObject.getAttribute(EN13606Types.VALUE.getName()) != null
                    && !pObject.getAttribute(EN13606Types.VALUE.getName()).getChildren().isEmpty()) {
                CPrimitive cp = ((CPrimitiveObject) pObject.getAttribute(EN13606Types.VALUE.getName()).getChildren().get(0)).getItem();
                try {
                    return printInterval(((CReal) cp).getInterval());
                } catch (IOException e) {
                }
            }
            return null;
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.DURATION.getName()) == 0) {
            return "{" + (pObject.getAttribute("sign") != null ? "[+-] " : "")
                    + (pObject.getAttribute("DAYS") != null ? "[Dia] : " : "")
                    + (pObject.getAttribute("HOURS") != null ? "[Horas] : " : "")
                    + (pObject.getAttribute("MINUTES") != null ? "[Minutos] : " : "")
                    + (pObject.getAttribute("SECONDS") != null ? "[Segundos]" : "")
                    + (pObject.getAttribute("fractional_second") != null ? ".[milesegundos]" : "") + "}";
        } else {
            return "";
        }

    }

    protected String printInterval(@SuppressWarnings("rawtypes") Interval interval)
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
            retorno = "duração: ";
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
     * Generate min e max occurs
     * @param interval
     * @return
     * @throws IOException 
     */
    @SuppressWarnings("unchecked")
    protected String printCardinalityInterval(@SuppressWarnings("rawtypes") Interval interval)
            throws IOException {
        String retorno = "";
        if (interval == null) {
            return "";
        }

        String type = null;
        final Comparable lower = interval.getLower();
        final Comparable upper = interval.getUpper();

        if (lower != null) {
            retorno += " minOccurs=\"" + lower.toString() + "\"";
        }

        if (upper != null) {
            retorno += " maxOccurs=\"" + upper.toString() + "\"";
        }

        return retorno;
    }

    @Override
    public String toString() {
        return this.out.toString();
    }

}
