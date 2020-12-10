package fr.usmb.m1isc.compilation.tp;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

public class AsmGenerateur {

	private Noeud m_arbreAbstrait;
	private Set<String> m_variables;
	private ArrayList<String> m_lignes;

	public AsmGenerateur(Noeud arbreAbstrait) {
		m_arbreAbstrait = arbreAbstrait;
		m_variables = new HashSet<String>();
		m_lignes = new ArrayList<String>();
	}

	public void ajoutVariable(String nom) {
		m_variables.add(nom);
	}

	public void ajoutLigneCode(String ligne) {
		m_lignes.add(ligne);
	}

	private void genererAsmLet(Arbre arbre) {
		genererAsmNoeud(arbre.droit());

		Feuille nom = (Feuille)arbre.gauche();
		ajoutVariable(nom.toString());
		ajoutLigneCode("mov " + nom + ", eax");
	}

	private void genererAsmEntier(Feuille feuille) {
		ajoutLigneCode("mov eax, " + feuille);
	}

	private void genererAsmSemi(Arbre arbre) {
		if (arbre.gauche() != null) {
			genererAsmNoeud(arbre.gauche());
		}
		if (arbre.droit() != null) {
			genererAsmNoeud(arbre.droit());
		}
	}

	private void genererAsmOperateurArith(Arbre arbre) {
		// Génération de la seconde opérande
		genererAsmNoeud(arbre.droit());
		// Enregistrement de la seconde opérande
		ajoutLigneCode("push eax");
		// Génération de la première opérande sur eax
		genererAsmNoeud(arbre.gauche());
		// Réstoration de la seconde opérande sur ebx
		ajoutLigneCode("pop ebx");

		String verbe = "";
		switch (arbre.type()) {
			case PLUS:
				verbe = "add";
				break;
			case MOINS:
				verbe = "sub";
				break;
			case MUL:
				verbe = "mul";
				break;
			case DIV:
				verbe = "div";
				break;
		}

		ajoutLigneCode(verbe + " eax, ebx");
	}

	private void genererAsmOutput(Arbre arbre) {
		genererAsmNoeud(arbre.gauche());
		ajoutLigneCode("out eax");
	}

	private void genererAsmIdent(Feuille feuille) {
		ajoutLigneCode("mov eax, " + feuille);
	}

	private void genererAsmNoeud(Noeud noeud) {
		switch (noeud.type()) {
			case LET:
				genererAsmLet((Arbre)noeud);
				break;
			case ENTIER:
				genererAsmEntier((Feuille)noeud);
				break;
			case SEMI:
				genererAsmSemi((Arbre)noeud);
				break;
			case PLUS:
			case MOINS:
			case MUL:
			case DIV:
				genererAsmOperateurArith((Arbre)noeud);
				break;
			case OUTPUT:
				genererAsmOutput((Arbre)noeud);
				break;
			case IDENT:
				genererAsmIdent((Feuille)noeud);
				break;
		}
	}

	private String genererDataSegment() {
		String code = "DATA SEGMENT\n";
		for (String nom : m_variables) {
			code += "\t" + nom + " DD\n";
		}
		code += "DATA ENDS\n";

		return code;
	}

	private String genererCodeSegment() {
		String code = "CODE SEGMENT\n";
		for (String ligne : m_lignes) {
			code += "\t" + ligne + "\n";
		}
		code += "CODE ENDS\n";

		return code;
	}

	public String genererAsm() {
		genererAsmNoeud(m_arbreAbstrait);

		String code = genererDataSegment() + genererCodeSegment();

		return code;
	}
}
