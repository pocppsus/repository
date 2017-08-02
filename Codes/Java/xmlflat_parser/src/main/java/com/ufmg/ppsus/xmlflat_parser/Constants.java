package com.ufmg.ppsus.xmlflat_parser;

public class Constants {

	public static String PATH_ARCHETYPE = "archetype";
	public static String UTF8 = "UTF-8";
	
	public static class Msg {
		public static String FILE_SAVED = "Arquivo foi salvo no servidor.";
	}
	public static class Error {
		public static final String FILE_NULL = "Arquivo nao enviado";
		public static final String ARCHETYPE_ID_NULL = "Não foi informado o arquétipo";
		public static final String ARCHETYPE_COMPOSITION_NULL = "Arquétipo deve ser de composition.";
		public static final String ARCHETYPE_ADL_NULL = "Estensão do arquivo não é .adl";		
		public static String READ_FILE = "Erro na leitura do arquivo";
		public static String PASER_ADL = "Erro ao transformar o adl";
	}
}
