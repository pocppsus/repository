package com.ufmg.ppsus.xmlflat_parser;

/**
 * Different types of the ReferenceModel EN13606
 * 
 */
public enum EN13606Types {
	
	

    // structure elements
    MEMBERS, COMPOSITION, SECTION, ELEMENT, ENTRY, CLUSTER, CONTENT, ITEMS, PARTS,

    // Data values
    REAL, INT, CD, CV, CE, CS, PQ, DATE, CODED_TEXT, SIMPLE_TEXT, ORD, RTO, QUANTITY_RANGE, EIVL, PIVL, TS, IVL, IVLTS, DURATION, BL, URI, II, ED, CS_LANG, CS_CHARSET, CS_UNITS, CS_MEDIA,

    // Attributes
    HIGH, LOW, WIDTH, SYMBOL, CODE_VALUE, CODING_SCHEME, CODING_SCHEME_NAME, DISPLAY_NAME, OID, PRECISION, UNITS, STRING, INTEGER, ORIGINAL_TEXT, CODED_VALUE, VALUE, SCHEME, NUMERATOR, DENOMINATOR, STRUCTURE_TYPE, TIME;

    EN13606Types() {

	}

	public String getName() {
	switch (this) {
	case CS_LANG:
	    return "CS_LANG";
	case CS_CHARSET:
	    return "CS_CHARSET";
	case CS_UNITS:
	    return "CS_UNITS";
	case CS_MEDIA:
	    return "CS_MEDIA";
	case CE:
	    return "CE";
	case IVL:
	    return "IVL";
	case IVLTS:
	    return "IVLTS";
	case DURATION:
	    return "DURATION";
	case BL:
	    return "BL";
	case URI:
	    return "URI";
	case II:
	    return "II";
	case ED:
	    return "ED";
	case EIVL:
	    return "EIVL";
	case PIVL:
	    return "PIVL";
	case TS:
	    return "TS";
	case ORD:
	    return "ORD";
	case RTO:
	    return "RTO";
	case QUANTITY_RANGE:
	    return "QUANTITY_RANGE";
	case REAL:
	    return "REAL";
	case INT:
	    return "INT";
	case COMPOSITION:
	    return "COMPOSITION";
	case SECTION:
	    return "SECTION";
	case ELEMENT:
	    return "ELEMENT";
	case ENTRY:
	    return "ENTRY";
	case CLUSTER:
	    return "CLUSTER";
	case STRING:
	    return "String";
	case CD:
	    return "CD";
	case INTEGER:
	    return "Integer";
	case CV:
	    return "CV";
	case CS:
	    return "CS";
	case OID:
	    return "oid";
	case PQ:
	    return "PQ";
	case DATE:
	    return "DATE";
	case CODED_TEXT:
	    return "CODED_TEXT";
	case SIMPLE_TEXT:
	    return "SIMPLE_TEXT";
	case CONTENT:
	    return "content";
	case ITEMS:
	    return "items";
	case CODED_VALUE:
	    return "codedValue";
	case CODE_VALUE:
	    return "codeValue";
	case CODING_SCHEME_NAME:
	    return "codingSchemeName";
	case CODING_SCHEME:
	    return "codingScheme";	    
	case ORIGINAL_TEXT:
	    return "originalText";
	case DISPLAY_NAME:
	    return "displayName";
	case PRECISION:
	    return "precision";
	case UNITS:
	    return "units";
	case SYMBOL:
	    return "symbol";
	case HIGH:
	    return "high";
	case LOW:
	    return "low";
	case WIDTH:
	    return "width";
	case VALUE:
	    return "value";
	case PARTS:
	    return "parts";
	case MEMBERS:
	    return "members";
	case SCHEME:
	    return "scheme";
	case NUMERATOR:
	    return "numerator";
	case DENOMINATOR:
	    return "denominator";
	case STRUCTURE_TYPE:
	    return "structure_type";
	case TIME:
	    return "time";	    
	default:
	    return "";
	}
    }
}

