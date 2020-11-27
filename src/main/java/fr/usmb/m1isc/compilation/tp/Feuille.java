package fr.usmb.m1isc.compilation.tp;

public class Feuille<T> extends Noeud {
	private T m_valeur;
	
	public Feuille(Type type, T valeur) {
		super(type);
		m_valeur = valeur;
	}

	public T valeur() {
		return m_valeur;
	}

	@Override
	public String toString() {
		return m_valeur.toString();
	}
}
