package fr.usmb.m1isc.compilation.tp;

public class Noeud {
	enum Type {
		AND,
		OR,
		NOT,
		EGAL,
		LT,
		LTE,
		GT,
		GTE,
		PLUS,
		MOINS,
		MUL,
		DIV,
		MOD,
		MOINS_UNAIRE,
		OUTPUT,
		INPUT,
		NIL,
		ENTIER,
		IDENT,
		SEMI,
		
		LET,
		WHILE,
		IF,
		THEN,
		ELSE,
		
		ERROR,
	};
	
	private Type m_type;

	public Noeud(Type type)	{
		m_type = type;
	}

	public Type type() {
		return m_type;
	}

	@Override
	public String toString() {
		return m_type.toString();
	}
}
