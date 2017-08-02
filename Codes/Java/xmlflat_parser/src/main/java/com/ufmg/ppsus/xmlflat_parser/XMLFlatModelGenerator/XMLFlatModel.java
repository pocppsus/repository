package com.ufmg.ppsus.xmlflat_parser.XMLFlatModelGenerator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.ehr.am.Archetype;
import org.ehr.am.constraintmodel.CAttribute;
import org.ehr.am.constraintmodel.CComplexObject;
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
 * Generate Flat XML model based in the achetype
 *
 * @author fabio elias
 */
public class XMLFlatModel {

    /* charset encodings */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    /* fields */
    private Charset encoding;

    private String lineSeparator;

    private String indent;

    private Out out;

    /**
     * Create an outputter with default encoding, indent and lineSeparator
     */
    public XMLFlatModel() {
        this.encoding = LATIN1;
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
     * Output given Flat XML Model
     *
     * @param archetype
     * @return a string in ADL format
     * @throws IOException
     * @throws ParseADLException
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
//        out.writeln("<"+printNodeName(archetype.getArchetypeId().toString())+" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
        out.writeln("<raiz>");
        printExtractHeader(archetype, out);
        printDefinition(archetype.getDefinition(), out);
        out.writeln("</raiz>");
    }

    /**
     *
     * @param out
     * @param indent
     * @param element
     * @throws IOException
     */
    protected void printRecordComponent(Out out, int indent, String element) throws IOException {
        if (!EN13606Types.COMPOSITION.getName().equalsIgnoreCase(element)
                && !EN13606Types.ENTRY.getName().equalsIgnoreCase(element)
                && !EN13606Types.SECTION.getName().equalsIgnoreCase(element)) {
            return;
        }
        indent(indent, out);
        out.writeln("<informacoes_de_auditoria><!--record_component/audit_info -->");
        indent(indent + 1, out);
        out.writeln("<responsabilidade><!--Composer-->");
        indent(indent + 3, out);
        out.writeln("<organizacao_de_saude>0027049</organizacao_de_saude><!--Helthcare facility-->");
        indent(indent + 3, out);
        out.writeln("<responsavel>980016296334678</responsavel><!--Performer-->");
        indent(indent + 2, out);
        out.writeln("</responsabilidade>");
        indent(indent + 1, out);
        out.writeln("<nivel_sensibilidade/><!--Sensistivity-->");
        indent(indent + 1, out);
        out.writeln("<synthesised>false</synthesised>");
        if (EN13606Types.COMPOSITION.getName().equalsIgnoreCase(element)) {
            indent(indent + 1, out);
            out.writeln("<territory></territory><!-- Apenas para a composição-->");
        }
        if (EN13606Types.ENTRY.getName().equalsIgnoreCase(element)) {
            indent(indent + 1, out);
            out.writeln("<categoria_informacao/><!--Subject_of_information_category entry [DS00, ds001,...]-->");
        }
        indent(indent + 1, out);
        out.writeln("<outros_participantes>");
        indent(indent + 2, out);
        out.writeln("<participante>980016287931952</participante>");
        indent(indent + 2, out);
        out.writeln("<organizacao_de_saude>0027049</organizacao_de_saude>");
        indent(indent + 1, out);
        out.writeln("</outros_participantes>");
        indent(indent + 1, out);
        if (EN13606Types.COMPOSITION.getName().equalsIgnoreCase(element)) {
            indent(indent + 1, out);
            out.writeln("<duracao><!--session_time(Composition)-->");
            indent(indent + 2, out);
            out.writeln("<data_hora_inicio/><!--low_time-->");
            indent(indent + 2, out);
            out.writeln("<data_hora_fim/><!--high_time-->");
            indent(indent + 1, out);
            out.writeln("</duracao>");
        }
        indent(indent + 1, out);
        out.writeln("<informacoes_de_envio><!--committal-->");
        indent(indent + 2, out);
        out.writeln("<cod_sistema>2</cod_sistema>");
        indent(indent + 2, out);
        out.writeln("<usuario_de_gravacao/><!--committer-->");
        indent(indent + 2, out);
        out.writeln("<data_hora_gravacao>2015-05-11T16:01:48</data_hora_gravacao><!--time_committed-->");
        indent(indent + 2, out);
        out.writeln("<versao>VER00</versao><!--version_status-->");
        indent(indent + 2, out);
        out.writeln("<revisao/><!-- revision_statusDeve-se utilziar esta tag?-->");
        indent(indent + 1, out);
        out.writeln("</informacoes_de_envio>");
        indent(indent, out);
        out.writeln("</informacoes_de_auditoria>");
    }

    protected void printExtractHeader(Archetype archetype, Out out) throws IOException {
        indent(1, out);
        out.writeln("<cod_sistema>1</cod_sistema>");
        indent(1, out);
        out.writeln("<paciente>19</paciente>");
        indent(1, out);
    }

    /**
     * Interpret root element of the archetype
     *
     * @param definition
     * @param out
     * @throws IOException
     */
    protected void printDefinition(CComplexObject definition, Out out)
            throws IOException {
        printCComplexObject(definition, 2, out, "");
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

    protected void printCAttribute(CAttribute cattribute, int indent, Out out)
            throws IOException {
        if (!cattribute.isAnyAllowed()) {
            List<CObject> children = cattribute.getChildren();
            if (children.size() > 1
                    || !(children.get(0) instanceof CPrimitiveObject)) {
                for (CObject cobject : cattribute.getChildren()) {
                    printCObject(cobject, indent + 1, out);
                }
            }
        }
    }

    /**
     * Interpret complex objects
     *
     * @param ccobj
     * @param indent
     * @param out
     * @param valor
     * @throws IOException
     */
    protected void printCComplexObject(CComplexObject ccobj, int indent,
            Out out, String valor) throws IOException {
        if (!ccobj.getArchetype().getNodeText(ccobj.getNodeID()).isEmpty()) {
            indent(indent, out);
            out.writeln("<" + printNodeName(ccobj.getArchetype().getNodeText(ccobj.getNodeID())) + ">");
            printRecordComponent(out, indent + 1, ccobj.getRmTypeName());
            if (valor != null && !valor.isEmpty()) {
                indent(indent + 1, out);
                out.writeln("<!--Valores válidos = " + valor + "-->");
            }
        }
        // print all attributes
        if (!ccobj.isAnyAllowed()) {
            for (CAttribute cattribute : ccobj.getAttributes()) {
                if (!cattribute.getRmAttributeName().contains("value")) {
                    printCAttribute(cattribute, indent + 1, out);
                }
            }
        }
        if (!ccobj.getArchetype().getNodeText(ccobj.getNodeID()).isEmpty()) {
            indent(indent, out);
            out.writeln("</" + printNodeName(ccobj.getArchetype().getNodeText(ccobj.getNodeID())) + ">");
        }
    }

    /**
     * Interpret object and your son's
     *
     * @param cObject
     * @param indent
     * @param out
     * @throws IOException
     */
    protected void printCObject(CObject cObject, int indent, Out out)
            throws IOException {
        String valor = null;
        if (cObject instanceof CComplexObject) {
            if (cObject.getRmTypeName().compareTo(EN13606Types.ELEMENT.getName()) == 0) {
                // getting the one and only value
                valor = getDataType((CComplexObject) ((CComplexObject) cObject).getAttribute("value").getChildren().get(0));
            }
            printCComplexObject((CComplexObject) cObject, indent, out, valor);
        }

    }

    private void indent(int level, Out out) throws IOException {
        for (int i = 0; i < level; i++) {
            out.append(indent);
        }
    }

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

    public String getDataType(CComplexObject pObject) {

        if (pObject.getRmTypeName().compareTo(EN13606Types.CODED_TEXT.getName()) == 0) {
            return parseCodedText(pObject);
        } else if (pObject.getRmTypeName().compareTo(EN13606Types.SIMPLE_TEXT.getName()) == 0) {
            return parseSimpleTextTerminology(pObject);
//			medicalDataTo.setSimpleText(parseSimpleText(pObject));
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

    @Override
    public String toString() {
        return this.out.toString();
    }

}
