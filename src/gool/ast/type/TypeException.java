package gool.ast.type;

import gool.ast.constructs.Node;
import gool.generator.GoolGeneratorController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TypeException extends IType {

	/**
	 * Define when the exception is used.
	 */
	public enum Kind {
		/** User defined exceptions */
		CUSTOM,
		/** Language defined exceptions that does not belong to another kind */
		DEFAULT,
		/** Highest exception, all exceptions inherit from it */
		GLOBAL,
		/* exceptions related to some domain */
		ARITHMETIC, COLLECTION, CAST, ENUM, ARGUMENT, THREAD, ARRAY, SECURITY,
		TYPE, UNSUPORTED, ARRAYSIZE, STATE, CLASSNOTFOUND, ACCESS, NEWINSTANCE,
		INTERUPT, NOSUCHFIELD, NOSUCHMETH, NULLREFERENCE,
	}
	
	/**
	 * The name of the exception in the source language
	 */
	private String name;
	
	/**
	 * The kind of exception, 'CUSTOM' for non language specified exceptions
	 */
	private Kind kind;
	
	/**
	 * The 'object' describing the exception, usually a ClassDef
	 */
	private Node descriptor;
	
	/**
	 * All exceptions know to GOOL, language defined like custom ones
	 */
	static private HashMap<String,TypeException> exceptions = new HashMap<String,TypeException>();
	
	public TypeException(String name, Kind kind, Node descriptor) {
		this.name = name;
		this.kind = kind;
		this.descriptor = descriptor;
	}
	
	static public void add(TypeException exception) {
		exceptions.put(exception.getName(), exception);
	}
	
	static public void add (TypeException... args) {
		for (TypeException exception : args)
			exceptions.put(exception.getName(), exception);
	}
	
	static public TypeException get(String name) {
		return exceptions.get(name);
	}

	@Override
	public String getName() {
		return name;
	}

	public Kind getKind() {
		return kind;
	}

	public Node getDescriptor() {
		return descriptor;
	}

	@Override
	public String callGetCode() {
		return GoolGeneratorController.generator().getCode(this);
	}
}
