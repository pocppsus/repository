package com.ufmg.ppsus.xmlflat_parser.XMLFlatModelGenerator;

import java.io.IOException;
import java.nio.charset.Charset;
import com.ufmg.ppsus.xmlflat_parser.Util;

/**
 * Generate Xquery script to interpret EHRextract XML
 *
 * The xquery process generate Flat XML
 *
 * @author fabio elias
 */
public class XQueryEHRExtractParser {

    /* charset encodings */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    /* fields */
    private Charset encoding;

    private String lineSeparator;

    private String indent;

    private Out out;

    private String extract;

    /**
     * Create an outputter with default encoding, indent and lineSeparator
     */
    public XQueryEHRExtractParser() {
        this.setEncoding(UTF8);
        this.indent = "    "; // 4 white space characters
        this.setLineSeparator(System.getProperty("line.separator"));
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
            System.out.println(out.toString());
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
     * Output given archetype as string of the Xquer script
     *
     * @param archetype
     * @param ehrExtract
     * @return a string in ADL format
     * @throws IOException
     */
    public String output(String ehrExtract) throws IOException {
        this.extract = ehrExtract.replace("'", "\"").replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"CEN/13606/RM\"", "");
        this.out = new Out();
        parse(out, this.extract);
        Util.saveLogFile(Util.getPath(this.getClass().getClassLoader().getResource("").getPath()), "xmlFlat", out.toString());
        return out.toString();
    }

    /**
     * Output given archetype to writer
     *
     * @param archetype
     * @param out
     * @throws IOException
     */
    public void parse(Out out, String extract) throws IOException {
        printExtractToFlat(out, 0, extract);

    }

    /**
     * Generate all functions of the xquery script
     * 
     * @param out
     * @param indent
     * @param extract
     * @throws IOException 
     */
    protected void printExtractToFlat(Out out, int indent, String extract) throws IOException {
        indent(indent, out);
        out.writeln("declare function local:extract($extract)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("let $rcId :=  data($extract/*:all_compositions/*:rc_id/*:extension)");
        indent(indent + 1, out);
        out.writeln("let $codSistema := data($extract/*:ehr_system/*:extension)");
        indent(indent + 1, out);
        out.writeln("let $subjectOfCare :=  data($extract/*:subject_of_care/*:extension)");
        indent(indent + 1, out);
        out.writeln("let $timeCreated :=  data($extract/*:time_created/*:time)");
        indent(indent + 1, out);
        out.writeln("let $committer :=  data($extract/*:all_compositions/*:committal/*:committer/*:extension)");
        indent(indent + 1, out);
        out.writeln("let $timeCommitted :=  data($extract/*:all_compositions/*:committal/*:time_committed/*:time)");
        indent(indent + 1, out);
        out.writeln("let $versionStatus :=  data($extract/*:all_compositions/*:committal/*:version_status/*:codeValue)");
        indent(indent + 1, out);
        out.writeln("let $reasonForRevision :=  data($extract/*:all_compositions/*:committal/*:reason_for_revision/*:codeValue)");
        indent(indent + 1, out);
        out.writeln("let $performer :=  data($extract/*:all_compositions/*:composer/*:performer/*:extension)");
        indent(indent + 1, out);
        out.writeln("let $healthcareFacillity :=  data($extract/*:all_compositions/*:composer/*:healthcare_facillity/*:extension)");
        indent(indent + 1, out);
        out.writeln("let $sessionTimeLow := data($extract/*:all_compositions/*:session_time/*:low/*:time)");
        indent(indent + 1, out);
        out.writeln("let $sessionTimeHigh := data($extract/*:all_compositions/*:session_time/*:high/*:time)");
        indent(indent + 1, out);
        out.writeln("return");
        indent(indent + 2, out);
        out.writeln("<SumarioAltaObstetrica id=\"{$rcId}\">");
        indent(indent + 2, out);
        out.writeln("<CodSistema>{$codSistema}</CodSistema>");
        indent(indent + 2, out);
        out.writeln("<Paciente>{$subjectOfCare}</Paciente>");
        indent(indent + 2, out);
        out.writeln("<Duracao>");
        indent(indent + 3, out);
        out.writeln("<!-- A data da criacao, necessaria ao extrato, pode ser a data de inicio da duracao, pois a data de gravacao pode ser alterada quando houver modificacao do registro -->");
        indent(indent + 3, out);
        out.writeln("<DataInicio>{$sessionTimeLow}</DataInicio>");
        indent(indent + 3, out);
        out.writeln("<DataFim>{$sessionTimeHigh}</DataFim>");
        indent(indent + 2, out);
        out.writeln("</Duracao>");
        indent(indent + 2, out);
        out.writeln("<InformacoesDeEnvio>");
        indent(indent + 3, out);
        out.writeln("<Responsabilidade>{$committer}</Responsabilidade>");
        indent(indent + 3, out);
        out.writeln("<DataHoraGravacao>{$timeCommitted}</DataHoraGravacao>");
        indent(indent + 2, out);
        out.writeln("<!-- Opcional apenas quando o extrato eh revisto?-->");
        indent(indent + 3, out);
        out.writeln("<Versao>{$versionStatus}</Versao>");
        indent(indent + 3, out);
        out.writeln("<!-- Opcional informar apenas quando o extrato eh revisto?-->");
        indent(indent + 3, out);
        out.writeln("<Revisao>{$reasonForRevision}</Revisao>");
        indent(indent + 2, out);
        out.writeln("</InformacoesDeEnvio>");
        indent(indent + 2, out);
        out.writeln("{local:content($extract/*:all_compositions/*:content)}");
        indent(indent + 1, out);
        out.writeln("</SumarioAltaObstetrica>");
        indent(indent, out);
        out.writeln("};");
        indent(indent, out);
        out.writeln("\ndeclare function local:recordComponent($pRecord)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("let $sensitivity :=  data($pRecord/*:sensitivity)");
        indent(indent + 1, out);
        out.writeln("let $subjectOfInformationCategory :=  data($pRecord/*:subject_of_information_category/*:codeValue)");
        indent(indent + 1, out);
        out.writeln("let $performer := data($pRecord/*:info_provider/*:performer/*:extension)");
        indent(indent + 1, out);
        out.writeln("let $healthcareFacillity := data($pRecord/*:info_provider/*:healthcare_facillity/*:extension)");
        indent(indent + 1, out);
        out.writeln("let $otherParticipations := for $otherPart in $pRecord");
        indent(indent + 2, out);
        out.writeln("let $id := data($pRecord/*:other_participations/*:performer/*:extension)");
        indent(indent + 2, out);
        out.writeln("let $healthcareFacillity := data($pRecord/*:other_participations/*:healthcare_facillity/*:extension)");
        indent(indent + 2, out);
        out.writeln("return <OutrosParticipantes>");
        indent(indent + 3, out);
        out.writeln("<id>{$id}</id>");
        indent(indent + 3, out);
        out.writeln("<OraganizacaodeSaude>{$healthcareFacillity}</OraganizacaodeSaude>");
        indent(indent + 3, out);
        out.writeln("</OutrosParticipantes>");
        indent(indent + 1, out);
        out.writeln("return <InformacoesDeAuditoria>");
        indent(indent + 2, out);
        out.writeln("<!-- Auditoria e o record componente, decidir se ele sera unico ou nao e onde sera representado Composicao, secao ou entrada -->");
        indent(indent + 2, out);
        out.writeln("<Responsabilidade>");
        indent(indent + 3, out);
        out.writeln("<OraganizacaodeSaude>{$healthcareFacillity}</OraganizacaodeSaude>");
        indent(indent + 3, out);
        out.writeln("<Responsavel>{$performer}</Responsavel>");
        indent(indent + 2, out);
        out.writeln("</Responsabilidade>");
        indent(indent + 2, out);
        out.writeln("<!-- Nivel de sensibilidade da informacao contida -->");
        indent(indent + 2, out);
        out.writeln("<NivelSensibilidade>{$sensitivity}</NivelSensibilidade>");
        indent(indent + 2, out);
        out.writeln("<!-- Verificar se subjectOfInformationCategory e uma informacao util ou nao, ela fica dentro da entry  -->");
        indent(indent + 2, out);
        out.writeln("<CategoriaInformacao>{$subjectOfInformationCategory}</CategoriaInformacao>");
        indent(indent + 2, out);
        out.writeln("{$otherParticipations}");
        indent(indent + 2, out);
        out.writeln("</InformacoesDeAuditoria>");
        indent(indent, out);
        out.writeln("};");
        indent(indent, out);
        out.writeln("\ndeclare function local:content($pContent)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("for $content in $pContent");
        indent(indent + 1, out);
        out.writeln("let $name := data($content/*:name/*:originalText)");
        indent(indent + 1, out);
        out.writeln("let $members := $content/*:members");
        indent(indent + 1, out);
        out.writeln("let $items := $content/*:items");
        indent(indent + 1, out);
        out.writeln("return let $el := element { local:Qname($name)}");
        indent(indent + 2, out);
        out.writeln("{");
        indent(indent + 3, out);
        out.writeln("if(empty($members))");
        indent(indent + 3, out);
        out.writeln("then local:items($items)");
        indent(indent + 3, out);
        out.writeln("else if(empty($items))");
        indent(indent + 3, out);
        out.writeln("then local:members($members)");
        indent(indent + 3, out);
        out.writeln("else \"\"");
        indent(indent + 2, out);
        out.writeln("}");
        indent(indent + 2, out);
        out.writeln("return copy $je := $el");
        indent(indent + 3, out);
        out.writeln("modify insert node local:recordComponent($content) as first into $je");
        indent(indent + 3, out);
        out.writeln("return $je");
        indent(indent, out);
        out.writeln("};");
        indent(indent, out);
        out.writeln("\ndeclare function local:members($pMembers)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("for $members in $pMembers");
        indent(indent + 1, out);
        out.writeln("let $name := data($members/*:name/*:originalText)");
        indent(indent + 1, out);
        out.writeln("let $membersSon := $members/*:members");
        indent(indent + 1, out);
        out.writeln("let $items := $members/*:items");
        indent(indent + 1, out);
        out.writeln("return  element{local:Qname($name)}");
        indent(indent + 2, out);
        out.writeln("{");
        indent(indent + 3, out);
        out.writeln("if(empty($membersSon))");
        indent(indent + 3, out);
        out.writeln("then local:items($items)");
        indent(indent + 3, out);
        out.writeln("else if(empty($items))");
        indent(indent + 3, out);
        out.writeln("then local:members($membersSon)");
        indent(indent + 3, out);
        out.writeln("else \"\"");
        indent(indent + 2, out);
        out.writeln("}");
        indent(indent, out);
        out.writeln("};");
        indent(indent, out);
        out.writeln("declare function local:items($pItems)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("for $items in $pItems");
        indent(indent + 1, out);
        out.writeln("let $name := data($items/*:name/*:originalText)");
        indent(indent + 1, out);
        out.writeln("let $parts := $items/*:parts");
        indent(indent + 1, out);
        out.writeln("let $value := $items/*:value");
        indent(indent + 1, out);
        out.writeln("return element{ local:Qname($name)}");
        indent(indent + 2, out);
        out.writeln("{");
        indent(indent + 3, out);
        out.writeln("if(empty($value))");
        indent(indent + 3, out);
        out.writeln("then local:parts($parts)");
        indent(indent + 3, out);
        out.writeln("else local:dataType($value)");
        indent(indent + 2, out);
        out.writeln("}");
        indent(indent, out);
        out.writeln("};");
        indent(indent + 1, out);
        out.writeln("\ndeclare function local:dataType($pValue)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("for $value in $pValue");
        indent(indent + 1, out);
        out.writeln("return");
        indent(indent + 2, out);
        out.writeln("if(exists(data($value/*:codeValue)))");
        indent(indent + 2, out);
        out.writeln("then data($value/*:codeValue)");
        indent(indent + 2, out);
        out.writeln("else if(exists(data($value/*:originalText)))");
        indent(indent + 2, out);
        out.writeln("then data($value/*:originalText)");
        indent(indent + 2, out);
        out.writeln("else if(exists(data($value/*:value)))");
        indent(indent + 2, out);
        out.writeln("then data($value/*:value)");
        indent(indent + 2, out);
        out.writeln("else if(exists(data($value/*:date)))");
        indent(indent + 2, out);
        out.writeln("then data($value/*:date)");
        indent(indent + 2, out);
        out.writeln("else if(exists(data($value/*:time)))");
        indent(indent + 2, out);
        out.writeln("then data($value/*:time)");
        indent(indent + 2, out);
        out.writeln("else \"\"");
        indent(indent, out);
        out.writeln("};");
        indent(indent, out);
        out.writeln("\ndeclare function local:parts($pParts)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("for $parts in $pParts");
        indent(indent + 1, out);
        out.writeln("let $name := data($parts/*:name/*:originalText)");
        indent(indent + 1, out);
        out.writeln("let $part := $parts/*:parts");
        indent(indent + 1, out);
        out.writeln("let $value := $parts/*:value");
        indent(indent + 1, out);
        out.writeln("return element{ local:Qname($name)}");
        indent(indent + 2, out);
        out.writeln("{");
        indent(indent + 3, out);
        out.writeln("if(empty($value))");
        indent(indent + 3, out);
        out.writeln("then local:parts($part)");
        indent(indent + 3, out);
        out.writeln("else local:dataType($value)");
        indent(indent + 2, out);
        out.writeln("}");
        indent(indent, out);
        out.writeln("};");
        indent(indent, out);
        out.writeln("declare function local:Qname($pText)");
        indent(indent, out);
        out.writeln("{");
        indent(indent + 1, out);
        out.writeln("if (empty($pText))");
        indent(indent + 1, out);
        out.writeln("then \"\"");
        indent(indent + 2, out);
        out.writeln("else lower-case(translate(translate(normalize-space($pText), ' ','_'), 'áàâäãéèêëíìîïõóòôöúùûüçÁÀÂÄÃÉÈÊËÍÌÎÏÕÓÒÔÖÚÙÛÜÇ,()-:/\\|', 'aaaaaeeeeiiiiooooouuuucAAAAAEEEEIIIIOOOOOUUUUC'))");
        indent(indent, out);
        out.writeln("};");
        indent(indent + 2, out);
        out.writeln("(:Generate xml flat for extract xml:)");
        indent(indent + 2, out);
        out.writeln("for $extract in doc('" + extract + "')//*:EHR_EXTRACT");
        indent(indent + 2, out);
        out.writeln("return local:extract($extract)");
    }

    /**
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

    @Override
    public String toString() {
        return this.out.toString();
    }

    public Charset getEncoding() {
        return encoding;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

}
