package fr.usmb.m1isc.compilation.tp;

import java.util.ArrayList;

public class Arbre extends Noeud {	
	private Noeud m_gauche;
	private Noeud m_droit;

	public Arbre(Type type, Object gauche, Object droit) {
		super(type);
		m_gauche = (Noeud)gauche;
		m_droit = (Noeud)droit;
	}

	public Arbre(Type type, Object gauche) {
		this(type, gauche, null);
	}

	@Override
	public String toString() {
		String res = "(" + super.toString() + " " + m_gauche;
		if (m_droit != null) {
			res += " " + m_droit;
		}
		res += ")";
		return res;
	}
}
