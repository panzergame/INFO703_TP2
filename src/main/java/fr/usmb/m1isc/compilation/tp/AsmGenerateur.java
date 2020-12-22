package fr.usmb.m1isc.compilation.tp;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

public class AsmGenerateur {

	private Noeud m_arbreAbstrait;
	private Set<String> m_variables;
	private ArrayList<String> m_lignes;
	private int m_compteurEtiquette;

	public AsmGenerateur(Noeud arbreAbstrait) {
		m_arbreAbstrait = arbreAbstrait;
		m_variables = new HashSet<String>();
		m_lignes = new ArrayList<String>();
		m_compteurEtiquette = 0;
	}

	/// Enregistrement du nom d'une variable
	private void ajoutVariable(String nom) {
		m_variables.add(nom);
	}

	/// Ajout d'une instruction
	private void ajoutLigneCode(String ligne) {
		m_lignes.add("\t" + ligne);
	}

	/// Generation d'un nom unique d'etiquette
	private String genererEtiquette(String nomBase) {
		return nomBase + m_compteurEtiquette++;
	}

	/// Ajout d'une etiquette après la dernière instruction'
	private void ajoutEtiquette(String nom) {
		m_lignes.add(nom + ":");
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

	private void genererAsmNil(Feuille feuille) {
		ajoutLigneCode("mov eax, 0");
	}

	private void genererAsmSemi(Arbre arbre) {
		if (arbre.gauche() != null) {
			genererAsmNoeud(arbre.gauche());
		}
		if (arbre.droit() != null) {
			genererAsmNoeud(arbre.droit());
		}
	}

	/** Generation des instructions des opérandes de l'arbre 
	 * tel que la valeur de la première et de la seconde opérande
	 * soient respectivement sur eax et ebx
	 * Note : le seconde opérande est executé en premier.
	 */
	private void genererOperandes(Arbre arbre) {
		// Génération de la seconde opérande
		genererAsmNoeud(arbre.droit());
		// Enregistrement de la seconde opérande
		ajoutLigneCode("push eax");
		// Génération de la première opérande sur eax
		genererAsmNoeud(arbre.gauche());
		// Réstoration de la seconde opérande sur ebx
		ajoutLigneCode("pop ebx");
	}

	/// Generation des opérateurs + - / *
	private void genererAsmOperateurArithBinaire(Arbre arbre) {
		genererOperandes(arbre);

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

	private void genererAsmModulo(Arbre arbre) {
		genererOperandes(arbre);

		// r = a % b :

		ajoutLigneCode("mov ecx, eax");
		// s = a / b
		ajoutLigneCode("div ecx, ebx");
		// s = a * b
		ajoutLigneCode("mul ecx, ebx");
		// r = a - s
		ajoutLigneCode("sub eax, ecx");
	}

	// Génération du moins unaire
	private void genererAsmOperateurArithUnaire(Arbre arbre) {
		genererAsmNoeud(arbre.gauche());
		ajoutLigneCode("push eax");
		ajoutLigneCode("mov eax, 0");
		ajoutLigneCode("pop ebx");

		String verbe = "";
		switch (arbre.type()) {
			case MOINS_UNAIRE:
				verbe = "sub";
				break;
		}

		ajoutLigneCode(verbe + " eax, ebx");
	}

	/// Génération de opérateur boolean binaire : and or == > < >= <=
	private void genererAsmOperateurBooleenBinaire(Arbre arbre) {
		genererOperandes(arbre);

		// On affecte le résultat sur eax, 0 = faux, sinon vrai
		switch (arbre.type()) {
			case AND:
				ajoutLigneCode("mul eax, ebx");
				break;
			case OR:
				ajoutLigneCode("add eax, ebx");
				break;
			case EGAL:
			case GT:
			case GTE:
			case LT:
			case LTE:
			{
				ajoutLigneCode("sub eax, ebx");
				String etiquetteVrai = genererEtiquette("vrai");
				String etiquetteSortie = genererEtiquette("sortie");

				String verbe = "";
				switch (arbre.type()) {
					case EGAL:
						verbe = "jz";
						break;
					case GT:
						verbe = "jg";
						break;
					case GTE:
						verbe = "jge";
						break;
					case LT:
						verbe = "jl";
						break;
					case LTE:
						verbe = "jle";
						break;
				}

				ajoutLigneCode(verbe + " " + etiquetteVrai);
				ajoutLigneCode("mov eax, 0");
				ajoutLigneCode("jmp " + etiquetteSortie);
				ajoutEtiquette(etiquetteVrai);
				ajoutLigneCode("mov eax, 1");
				ajoutEtiquette(etiquetteSortie);
				break;
			}
		}
	}

	/// Génération opératuer boolean unaire : not
	private void genererAsmOperateurBooleenUnaire(Arbre arbre) {
		genererAsmNoeud(arbre.gauche());

		String etiquetteVrai = genererEtiquette("vrai");
		String etiquetteSortie = genererEtiquette("sortie");

		switch (arbre.type()) {
			case NOT:
			{
				ajoutLigneCode("jz " + etiquetteVrai);
				ajoutLigneCode("mov eax, 0");
				ajoutLigneCode("jmp " + etiquetteSortie);
				ajoutEtiquette(etiquetteVrai);
				ajoutLigneCode("mov eax, 1");
				ajoutEtiquette(etiquetteSortie);
				break;
			}
		}
	}

	private void genererAsmInput(Feuille feuille) {
		ajoutLigneCode("in eax");
	}

	private void genererAsmOutput(Arbre arbre) {
		genererAsmNoeud(arbre.gauche());
		ajoutLigneCode("out eax");
	}

	private void genererAsmIdent(Feuille feuille) {
		ajoutLigneCode("mov eax, " + feuille);
	}

	private void genererAsmIf(Arbre arbre) {
		Arbre alternatives = (Arbre)arbre.droit();

		// Génération de l'expression booléenne de la condition
		genererAsmNoeud(arbre.gauche());

		String etiquetteVrai = genererEtiquette("cond_vrai");
		String etiquetteSortie = genererEtiquette("cond_sortie");

		// Test de la condition
		ajoutLigneCode("jnz " + etiquetteVrai);
		// Si faux
		genererAsmNoeud(alternatives.droit());
		ajoutLigneCode("jmp " + etiquetteSortie);

		// Si vrai
		ajoutEtiquette(etiquetteVrai);
		genererAsmNoeud(alternatives.gauche());

		// Rendez-vous de sortie
		ajoutEtiquette(etiquetteSortie);
	}

	private void genererAsmWhile(Arbre arbre) {
		String etiquetteDebut = genererEtiquette("while_debut");
		String etiquetteSortie = genererEtiquette("while_sortie");

		// Rendez-vous de répétition
		ajoutEtiquette(etiquetteDebut);
		// Génération de l'expression booléenne de la boucle
		genererAsmNoeud(arbre.gauche());
		// Test condition
		ajoutLigneCode("jz " + etiquetteSortie);
		// Si vrai execution
		genererAsmNoeud(arbre.droit());
		// Répétition
		ajoutLigneCode("jmp " + etiquetteDebut);

		// Rendez-vous de sortie
		ajoutEtiquette(etiquetteSortie);
	}

	/// Génération des information assembleur pour un noeud abstrait.
	private void genererAsmNoeud(Noeud noeud) {
		// Délégation de la responsabilité aux sous méthodes
		switch (noeud.type()) {
			case LET:
				genererAsmLet((Arbre)noeud);
				break;
			case ENTIER:
				genererAsmEntier((Feuille)noeud);
				break;
			case NIL:
				genererAsmNil((Feuille)noeud);
				break;
			case SEMI:
				genererAsmSemi((Arbre)noeud);
				break;
			case PLUS:
			case MOINS:
			case MUL:
			case DIV:
				genererAsmOperateurArithBinaire((Arbre)noeud);
				break;
			case MOD:
				genererAsmModulo((Arbre)noeud);
				break;
			case MOINS_UNAIRE:
				genererAsmOperateurArithUnaire((Arbre)noeud);
				break;
			case AND:
			case OR:
			case EGAL:
			case GT:
			case GTE:
			case LT:
			case LTE:
				genererAsmOperateurBooleenBinaire((Arbre)noeud);
				break;
			case NOT:
				genererAsmOperateurBooleenUnaire((Arbre)noeud);
				break;
			case INPUT:
				genererAsmInput((Feuille)noeud);
				break;
			case OUTPUT:
				genererAsmOutput((Arbre)noeud);
				break;
			case IDENT:
				genererAsmIdent((Feuille)noeud);
				break;
			case IF:
				genererAsmIf((Arbre)noeud);
				break;
			case WHILE:
				genererAsmWhile((Arbre)noeud);
				break;
		}
	}

	/// Génération du segment de donnée par rapport aux variables enregistrées
	private String genererDataSegment() {
		String code = "DATA SEGMENT\n";
		for (String nom : m_variables) {
			code += "\t" + nom + " DD\n";
		}
		code += "DATA ENDS\n";

		return code;
	}

	/// Génération du code des instructions
	private String genererCodeSegment() {
		String code = "CODE SEGMENT\n";
		for (String ligne : m_lignes) {
			code += ligne + "\n";
		}
		code += "CODE ENDS\n";

		return code;
	}

	/// Génération du code complet à partir de l'arbre abstrait initialisant le générateur.'
	public String genererAsm() {
		genererAsmNoeud(m_arbreAbstrait);

		String code = genererDataSegment() + genererCodeSegment();

		return code;
	}
}
