package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.assignments.parsing.*;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;


public class GenerativeParserFactory implements ParserFactory {
	
	public Parser getParser(List<Tree<String>> trainTrees) {
		 return new GenerativeParser(trainTrees);
	}

}
