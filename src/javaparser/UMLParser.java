package javaparser;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.VoidType;

/**
 * UML Parser takes the Java Code and parses it into a yUML compatible string
 * This string can then be used to fetch the Class Diagram.
 */
//creating a class which contains various constant variables which will 
//change in value through out the project

class symbols {
	
	public static String privateAccess = "-";
	public static String publicStaticAccess = "+";
	public static String publicAccess = "+";
	public static String protectedAccess = "#";
	public static String defaultAccess = "~";
	public static String startSquareBracket = "[";
	public static String endSquareBracket = "]";
	public static String inClassSeparator = "|";
	public static String declareInterface = "<<interface>>";
	public static String semiColonSeparator = ";";
	public static String colonSeparator = ":";
	public static String commaSeparator = ",";
	public static String referenceArray = "(*)";
	public static String extendsClass = "^-";
	public static String implementsInterface = "^-.-";
	public static String many = "*";
	public static String one = "1";
	public static String none = "";
	public static String baseyumlURL = "http://yuml.me/diagram/plain/class/";
	public static String dependency_uses = "uses-.->";
}
public class UMLParser {

	private static String basePath = "";
	private StringBuilder submitURL = new StringBuilder();
	private String stringReferenceType = "String";
	private String className = "";


	private ArrayList<Multiplicity> associations = new ArrayList<Multiplicity>();
	
	private ArrayList<String> interfaces = new ArrayList<String>();
	private ArrayList<String> listOfReferenceTypes = new ArrayList<String>();
	private ArrayList<String> listOfCollectionReferences = new ArrayList<>();
	private ArrayList<String> methods = new ArrayList<String>();
	private ArrayList<String> variables = new ArrayList<String>();
	private ArrayList<String> extendsList = new ArrayList<String>();
	private ArrayList<String> implementsList = new ArrayList<String>();
	private ArrayList<String> methodParameters = new ArrayList<String>();
	private ArrayList<String> variableNames = new ArrayList<String>();
	private ArrayList<String> methodNames = new ArrayList<String>();
	private ArrayList<String> dependencies = new ArrayList<String>();
	private ArrayList<String> classes = new ArrayList<String>();

	private boolean isInterface = false;
	private File[] javaFiles;

	public UMLParser(String fileName) {

		basePath = fileName;
	}

	public String parse()
	{
		listOfReferenceTypes.add("int");
		listOfReferenceTypes.add("short");
		listOfReferenceTypes.add("byte");
		listOfReferenceTypes.add("long");
		listOfReferenceTypes.add("float");
		listOfReferenceTypes.add("double");
		listOfReferenceTypes.add("boolean");
		listOfReferenceTypes.add("char");
		listOfReferenceTypes.add("Integer");
		listOfReferenceTypes.add("Short");
		listOfReferenceTypes.add("Byte");
		listOfReferenceTypes.add("Long");
		listOfReferenceTypes.add("Float");
		listOfReferenceTypes.add("Double");
		listOfReferenceTypes.add("Boolean");
		listOfReferenceTypes.add("Character");

		listOfCollectionReferences.add("Collection");
		listOfCollectionReferences.add("Set");
		listOfCollectionReferences.add("HashSet");
		listOfCollectionReferences.add("LinkedHashSet");
		listOfCollectionReferences.add("SortedSet");
		listOfCollectionReferences.add("Queue");
		listOfCollectionReferences.add("LinkedList");
		listOfCollectionReferences.add("List");
		listOfCollectionReferences.add("Vector");
		listOfCollectionReferences.add("ArrayList");
		listOfCollectionReferences.add("Map");
		listOfCollectionReferences.add("HashMap");
		listOfCollectionReferences.add("LinkedHashMap");
		listOfCollectionReferences.add("Hashtable");

		try
		{
			//Set the folder from which we need to get the .java files
			File folder = new File(basePath);

			submitURL.append(symbols.baseyumlURL);

			javaFiles =  folder.listFiles(urlFilter);

			//This step stores all the interfaces found in the folder in "interfaces"
			//ArrayList and stores all the class names in the folder in "classes" ArrayList
			for(File javaFile :javaFiles)
			{
				//Get the stream for the file
				FileInputStream inputStream = new FileInputStream(javaFile.getAbsolutePath());

				//Parses the file
				CompilationUnit cu = JavaParser.parse(inputStream);

				List<Node> cuChildNodes = cu.getChildrenNodes();

				//Get the childNodes from the main Java Files
				//1. ClassOrInterfaceDeclaration
				//2. PackageDeclaration
				//3. ImportDeclaration
				//We will be using only ClassOrInterfaceDeclaration
				for(Node cuChildNode : cuChildNodes)
				{
					if(cuChildNode instanceof ClassOrInterfaceDeclaration)
					{
						ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration)cuChildNode;

						//Checking whether our class is Interface, if yes just add the Interface and 
						//move ahead, as yUML does not add field and methods of interface
						if(cid.isInterface())
						{
							interfaces.add(cid.getName());
						}
						classes.add(cid.getName());
					}
				}
			}

			//Get the .java files from the folder using FileFilter
			for(File javaFile :javaFiles)
			{
				isInterface = false;

				methods = new ArrayList<String>();
				variables = new ArrayList<String>();
				variableNames = new ArrayList<String>();
				methodNames = new ArrayList<String>();

				if(submitURL.length() > 0 && (submitURL.charAt(submitURL.length()-1) != ','))
				{
					submitURL.append(symbols.commaSeparator);
				}

				submitURL.append(symbols.startSquareBracket);

				//Get the stream for the file
				FileInputStream inputStream = new FileInputStream(javaFile.getAbsolutePath());

				//Parses the file
				CompilationUnit cu = JavaParser.parse(inputStream);

				List<Node> cuChildNodes = cu.getChildrenNodes();

				//Get the childNodes from the main Java Files
				//1. ClassOrInterfaceDeclaration
				//2. PackageDeclaration
				//3. ImportDeclaration
				//We will be using only ClassOrInterfaceDeclaration
				for(Node cuChildNode : cuChildNodes)
				{
					if(cuChildNode instanceof ClassOrInterfaceDeclaration)
					{
						ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration)cuChildNode;

						//Checking whether our class is Interface, if yes just add the Interface and 
						//move ahead, as yUML does not add field and methods of interface
						if(cid.isInterface())
						{
							//interfaces.add(cid.getName());
							submitURL.append(symbols.declareInterface);
							submitURL.append(symbols.semiColonSeparator);
							submitURL.append(cid.getName());
							submitURL.append(symbols.endSquareBracket);
							className = cid.getName();
							//classes.add(className);
							isInterface = true;
							continue;
						}

						submitURL.append(cid.getName());
						className = cid.getName();
						//classes.add(className);

						extended(cid);
						implementedInterfacesGenerator(cid);
						constructorGenerator(cid);
					}
				}//end of cuChildNodes for loop


				List<TypeDeclaration> cuTypes = cu.getTypes();

				for(TypeDeclaration cuType : cuTypes)
				{
					List<BodyDeclaration> cuBodyDeclaration = cuType.getMembers();

					if(cuBodyDeclaration.size() > 0)
					{
						String modifierValue = "";

						for(BodyDeclaration bodyDeclaration : cuBodyDeclaration)
						{
							//Section where the global variables are detected and stored in the 
							//variables ArrayList for every class
							if(bodyDeclaration instanceof FieldDeclaration)
							{
								String primitiveTypeValue = "";
								FieldDeclaration fieldDeclaration = (FieldDeclaration)bodyDeclaration;

								int fieldDeclarationModifiers = fieldDeclaration.getModifiers();
								boolean proceed = false;

								switch(fieldDeclarationModifiers)
								{
								case ModifierSet.PRIVATE:
									modifierValue = symbols.privateAccess;
									proceed = true;
									break;
								case ModifierSet.PUBLIC:
									modifierValue = symbols.publicAccess;
									proceed = true;
									break;
								case ModifierSet.PUBLIC+ModifierSet.STATIC:
									modifierValue = symbols.publicStaticAccess;
								proceed = true;
								break;
								}

								//Checks for only the private and public variables
								//as specified
								if(proceed)
								{
									boolean enterVariable = true;
									List<Node> fieldChildNodes = fieldDeclaration.getChildrenNodes();

									for(Node fieldChildNode : fieldChildNodes)
									{
										if(fieldChildNode instanceof ReferenceType)
										{
											ReferenceType referenceType = (ReferenceType)fieldChildNode;

											String referenceString = referenceType.getType().toString();

											if(referenceString.equals(stringReferenceType))
											{
												primitiveTypeValue += referenceString;
											}
											else
											{
												boolean foundPrimitive = false;
												boolean foundCollection = false;

												for(String primitiveReference : listOfReferenceTypes)
												{
													if(referenceString.contains(primitiveReference))
													{
														primitiveTypeValue += referenceString+symbols.referenceArray;
														foundPrimitive = true;
														break;
													}
												}

												if(!foundPrimitive)
												{
													for(String collectionReference : listOfCollectionReferences)
													{
														if(referenceString.contains(collectionReference))
														{ 

															if(checkForCollectionReference(referenceString,className))
															{
																enterVariable = false;
																foundCollection = true;
																break;
															}
														}
													}

													if(!foundCollection)
													{
														enterVariable = false;
														boolean relationFound = false;

														for(String strInterface : interfaces)
														{
															if(strInterface.equals(referenceString))
															{
																referenceString = symbols.declareInterface+symbols.semiColonSeparator+referenceString;
															}
														}

														Multiplicity newAssocaition = new Multiplicity(referenceString, className, symbols.one, symbols.none);

														for(Multiplicity association : associations)
														{
															if(newAssocaition.equals(association))
															{
																String classA = association.getClassA();
																String classB = association.getClassB();

																if(newAssocaition.getClassA().equals(classB))
																{
																	if(association.getRelationOfBwithA().equals(symbols.none))
																	{
																		association.setRelationOfBwithA(symbols.one);
																		relationFound = true;
																		break;
																	}
																}
																else if(newAssocaition.getClassA().equals(classA))
																{
																	if(association.getRelationOfAwithB().equals(symbols.none))
																	{
																		association.setRelationOfAwithB(symbols.one);
																		relationFound = true;
																		break;
																	}
																}
															}
														}

														if(!relationFound)
														{
															associations.add(newAssocaition);
														}
													}
												}

											}
										}
										else if(fieldChildNode instanceof PrimitiveType)
										{
											PrimitiveType primiType = (PrimitiveType)fieldChildNode;
											primitiveTypeValue = primiType.toString();
										}

										if(fieldChildNode instanceof VariableDeclarator && enterVariable)
										{
											VariableDeclarator variableDeclarator = (VariableDeclarator)fieldChildNode;
											variableNames.add(variableDeclarator.toString());
											variables.add(modifierValue+variableDeclarator.toString()+symbols.colonSeparator+primitiveTypeValue);
										}
									}
								}
							}
							//Checks for all the method Declarations in the every class
							//These declarations are all stored in the 
							//methods ArrayList for every class
							else if(bodyDeclaration instanceof MethodDeclaration)
							{

								String tempMethodParam = "";
								String accessModifier = "";
								String referenceType = "";
								String methodName = "";
								MethodDeclaration methodDeclaration = (MethodDeclaration)bodyDeclaration;

								int methodModifier = methodDeclaration.getModifiers();
								boolean proceed = false;

								switch(methodModifier)
								{
								case ModifierSet.PUBLIC:
									accessModifier = symbols.publicAccess;
									proceed = true;
									break;
								case ModifierSet.PUBLIC+ModifierSet.STATIC:
									accessModifier = symbols.publicStaticAccess;
								proceed = true;
								break;
								}


								if(proceed)
								{
									List<Node> methodChildNodes =   methodDeclaration.getChildrenNodes();

									for(Node methodChildNode : methodChildNodes)
									{
										if(methodChildNode instanceof ReferenceType)
										{
											ReferenceType referenceMethod = (ReferenceType)methodChildNode;

											referenceType = referenceMethod.getType().toString();
										}
										else if(methodChildNode instanceof VoidType)
										{
											referenceType = "void";
										}
									}

									methodName  = methodDeclaration.getName();

									List<Parameter> methodParams = methodDeclaration.getParameters();

									//This section detects the Parameters in the class methods
									//These Parameters are then stored in the methodParameters 
									//ArrayList for every method detected in every class
									methodParameters = new ArrayList<String>();
									if(methodParams.size() > 0)
									{
										tempMethodParam += "(";
										for(Parameter param : methodParams)
										{
											List<Node> paramChildNodes = param.getChildrenNodes();

											String methodReferenceType="", variable="";
											for(Node paramChildNode : paramChildNodes)
											{
												if(paramChildNode instanceof ReferenceType)
												{
													ReferenceType r = (ReferenceType)paramChildNode;
													methodReferenceType = r.getType().toString();


													for(int i=0 ; i<interfaces.size() ; i++)
													{
														if(methodReferenceType.equals(interfaces.get(i)))
														{
															if(!isInterface)
															{
																boolean dependencyFound = false;
																for(String dependency : dependencies)
																{
																	String classNameFormat = "["+className+"]";
																	if(dependency.contains(classNameFormat) && dependency.contains(methodReferenceType))
																	{
																		dependencyFound = true;
																		break;
																	}
																}

																if(!dependencyFound)
																{
																	dependencies.add(symbols.startSquareBracket+className+symbols.endSquareBracket+
																			symbols.dependency_uses+symbols.startSquareBracket+symbols.declareInterface+symbols.semiColonSeparator+
																			methodReferenceType+symbols.endSquareBracket);
																}
															}
														}
													}
												}
												else if(paramChildNode instanceof VariableDeclaratorId)
												{
													VariableDeclaratorId v = (VariableDeclaratorId)paramChildNode;
													variable = v.getName().toString();
												}
											}
											methodParameters.add(variable+symbols.colonSeparator+methodReferenceType);
										}

										for(int i=0; i<methodParameters.size() ; i++)
										{
											if(i != methodParameters.size()-1)
											{
												tempMethodParam += methodParameters.get(i)+symbols.commaSeparator;
											}
											else
											{
												tempMethodParam += methodParameters.get(i);
											}
										}
										tempMethodParam += ")";
									}
									else
									{
										tempMethodParam = "()";
									}

									methodNames.add(methodName);
									methods.add(accessModifier+methodName+tempMethodParam+symbols.colonSeparator+referenceType);
									
									//Detect the Body of the methods in every class
									//If the body is detected, then find any Class Object creation
									//After a class declaration is detected it is checked whether the class found
									//is a part of the folders list of classes. If yes, it added to the dependency
									List<Node> methodChildNodesOfBody = methodDeclaration.getChildrenNodes();
								    
								    if(methodChildNodesOfBody.size() > 0)
								    {
								    	for(Node n: methodChildNodesOfBody)
									    {
									    	if(n instanceof BlockStmt)
									    	{
									    		BlockStmt blockStmt = (BlockStmt)n;
									    		
									    		List<Node> blockStmtChildNodes = blockStmt.getChildrenNodes();
									    		
									    		for(Node blockStmtChildNode : blockStmtChildNodes)
									    		{
									    			if(blockStmtChildNode instanceof ExpressionStmt)
									    			{
									    				ExpressionStmt expressionStmt = (ExpressionStmt)blockStmtChildNode;
									    				
									    				List<Node> expressStmtChildNodes = expressionStmt.getChildrenNodes();
									    				
									    				for(Node expressStmtChildNode : expressStmtChildNodes)
									    				{
									    					if(expressStmtChildNode instanceof VariableDeclarationExpr)
									    					{
									    						VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr)expressStmtChildNode;
									    						List<Node> variableDeclarationExprChildNodes = variableDeclarationExpr.getChildrenNodes();
									    						
									    						String methodClassName="";
									    						for(Node variableDeclarationExprChildNode: variableDeclarationExprChildNodes)
									    						{
									    							if(variableDeclarationExprChildNode instanceof ReferenceType)
									    							{
									    								ReferenceType referenceTypeForMethodBody = (ReferenceType)variableDeclarationExprChildNode;
									    								methodClassName = referenceTypeForMethodBody.getType().toString();
									    								
									    								for(int i=0 ; i<interfaces.size() ; i++)
									    								{
									    									String classNameFromList = interfaces.get(i);
									    									
									    									if(methodClassName.equals(classNameFromList))
									    									{
																				boolean dependencyFound = false;
																				for(String dependency : dependencies)
																				{
																					String classNameFormat = "["+className+"]";
																					if(dependency.contains(classNameFormat) && dependency.contains(methodClassName))
																					{
																						dependencyFound = true;
																						break;
																					}
																				}

																				if(!dependencyFound)
																				{
																					dependencies.add(symbols.startSquareBracket+className+symbols.endSquareBracket+
																							symbols.dependency_uses+symbols.startSquareBracket+symbols.declareInterface+symbols.semiColonSeparator+
																							methodClassName+symbols.endSquareBracket);
																				}
																			
									    									}
									    								}
									    							}
									    						}
									    					}
									    				}
									    			}
									    		}
									    	}
									    }
								    }
								}
							}
						}
					}
				}

				//checking for the setter and getter methods and its variables
				//If found, remove the methods from the list and 
				//add +(public access) to the variable
				if(variableNames.size() > 0)
				{
					for (int i = 0; i < variableNames.size(); i++) {

						String variableName = variableNames.get(i).toLowerCase();

						for(int j=0; j<methods.size() ; j++)
						{
							String methodName = methods.get(j);
							String getterMethod = "get"+variableName;
							String setterMethod = "set"+variableName;

							if(methodName.toLowerCase().contains(setterMethod))
							{
								methods.remove(j);
								j--;

								String variable = variables.get(i);
								variables.remove(i);
								variable = variable.replaceAll(ModifierSet.PRIVATE+"", ModifierSet.PUBLIC+"");
								variables.add(variable);
							}

							if(methodName.toLowerCase().contains(getterMethod))
							{
								methods.remove(j);
								j--;

								String variable = variables.get(i);

								variables.remove(i);

								variable = variable.replaceAll("-", "+");

								variables.add(variable);
							}
						}
					}
				}

				//adding the variables to the yUML URL
				if(variables.size() > 0)
				{
					submitURL.append(symbols.inClassSeparator);
					for(int i=0 ; i<variables.size() ; i++)
					{
						if(i != variables.size()-1)
							submitURL.append(variables.get(i)+symbols.semiColonSeparator);
						else
							submitURL.append(variables.get(i));
					}

				}

				//adding the methods to the yUML URL
				if(methods.size() > 0)
				{
					submitURL.append(symbols.inClassSeparator);
					for(int i=0 ; i<methods.size() ; i++)
					{
						if(i != methods.size()-1)
							submitURL.append(methods.get(i)+symbols.semiColonSeparator);
						else
							submitURL.append(methods.get(i));
					}

				}

				submitURL.append(symbols.endSquareBracket);
				submitURL.append(symbols.commaSeparator);
			}

			//adding the extends classes to the yUML URL
			if(extendsList.size() > 0)
			{
				if(submitURL.charAt(submitURL.length()-1) != ',')
				{
					submitURL.append(symbols.commaSeparator);
				}
				for(int i=0 ; i<extendsList.size() ; i++)
				{
					if(i != extendsList.size()-1)
						submitURL.append(extendsList.get(i)+symbols.commaSeparator);
					else
						submitURL.append(extendsList.get(i));
				}
			}

			//adding the implements classes to the yUML URL
			if(implementsList.size() > 0)
			{
				if(submitURL.charAt(submitURL.length()-1) != ',')
				{
					submitURL.append(symbols.commaSeparator);
				}
				for(int i=0 ; i<implementsList.size() ; i++)
				{
					if(i != implementsList.size()-1)
						submitURL.append(implementsList.get(i)+symbols.commaSeparator);
					else
						submitURL.append(implementsList.get(i));
				}
			}

			//adding the associations
			if(associations.size() > 0)
			{
				if(submitURL.charAt(submitURL.length()-1) != ',')
				{
					submitURL.append(symbols.commaSeparator);
				}

				//assigning <<interface>> word to all the interfaces
				for(int i=0 ; i<associations.size() ; i++)
				{
					boolean classAPresent = false, classBPresent = false;
					Multiplicity association = associations.get(i);

					String classA = association.getClassA();
					String classB = association.getClassB();

					for(int j=0 ; j<interfaces.size() ; j++)
					{
						if(classA.equals(interfaces.get(j)))
						{
							association.setClassA(symbols.declareInterface+symbols.semiColonSeparator+classA);
						}

						if(classB.equals(interfaces.get(j)))
						{
							association.setClassB(symbols.declareInterface+symbols.semiColonSeparator+classB);
						}
					}

					//Checking with the classes present in the package
					//If it does not match then remove the association
					//e.g.: String is not included in the package. So, if [A]*-[String] exists in association
					//then that association will be excluded from the URL
					for(int j=0 ; j<classes.size() ; j++)
					{
						if(classes != null)
						{
							if(classA.contains(classes.get(j)))
							{
								classAPresent = true;
							}

							if(classB.contains(classes.get(j)))
							{
								classBPresent = true;
							}
						}
					}

					if(!classAPresent || !classBPresent)
					{
						associations.remove(i);
						i--;
					}
				}

				//adding the associations to the yUML URL
				for(int i=0 ; i<associations.size() ; i++)
				{
					if(i != associations.size()-1)
						submitURL.append(symbols.startSquareBracket+associations.get(i).getClassA()+symbols.endSquareBracket+associations.get(i).getRelationOfAwithB()+"-"+
								associations.get(i).getRelationOfBwithA()+symbols.startSquareBracket+associations.get(i).getClassB()+symbols.endSquareBracket+symbols.commaSeparator);

					else
						submitURL.append(symbols.startSquareBracket+associations.get(i).getClassA()+symbols.endSquareBracket+associations.get(i).getRelationOfAwithB()+"-"+
								associations.get(i).getRelationOfBwithA()+symbols.startSquareBracket+associations.get(i).getClassB()+symbols.endSquareBracket);

				}
			}

			//adding dependencies to the yUML URL
			//currently on interface/uses dependencies have been included
			if(dependencies.size() > 0)
			{
				if(submitURL.charAt(submitURL.length()-1) != ',')
				{
					submitURL.append(symbols.commaSeparator);
				}
				for(int i=0 ; i<dependencies.size() ; i++)
				{
					if(i != dependencies.size()-1)
						submitURL.append(dependencies.get(i)+symbols.commaSeparator);
					else
						submitURL.append(dependencies.get(i));
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println("Error during execution :: "+e.getMessage());
		}
		return submitURL.toString();
	}

	//File Filter to detect all java files present in the folder specified
	FileFilter urlFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true; // return directories for recursion
			}
			return file.getName().endsWith(".java"); // return .url files
		}
	};

	
	private boolean checkForCollectionReference(String referenceString,String className)
	{
		String strGenericsObject = "";
		boolean relationFound = false;
		if(referenceString.contains("<") && referenceString.contains(">"))
		{
			strGenericsObject = referenceString.substring(referenceString.indexOf("<")+1, referenceString.indexOf(">"));

			for(String strInterface : interfaces)
			{
				if(strInterface.equals(strGenericsObject))
				{
					strGenericsObject = symbols.declareInterface+symbols.semiColonSeparator+strGenericsObject;
				}
			}

			Multiplicity newAssocaition = new Multiplicity(strGenericsObject, className, symbols.many, symbols.none);

			for(Multiplicity association : associations)
			{
				if(newAssocaition.equals(association))
				{
					String classA = association.getClassA();
					String classB = association.getClassB();

					if(newAssocaition.getClassA().equals(classB))
					{
						association.setRelationOfBwithA(symbols.many);
						relationFound = true;
						break;
					}
					else if(newAssocaition.getClassA().equals(classA))
					{
						if(association.getRelationOfAwithB().equals(symbols.one) || 
								association.getRelationOfAwithB().equals(symbols.none))
						{
							association.setRelationOfAwithB(symbols.many);
							relationFound = true;
							break;
						}
					}
				}
			}

			if(!relationFound)
			{
				relationFound = true;
				associations.add(newAssocaition);
			}
		}

		return relationFound;
	}


	private void constructorGenerator(ClassOrInterfaceDeclaration cid)
	{
		//get the construction declaration
		List<Node> listChildren = cid.getChildrenNodes();
		String constructorDeclaration = "";
		String constructorName = "";
		String variable="", reference="";
		for(Node nChild : listChildren)
		{
			if(nChild instanceof ConstructorDeclaration)
			{
				ConstructorDeclaration cd = (ConstructorDeclaration)nChild;

				constructorName = cd.getName();

				List<Node> constructorNodes = cd.getChildrenNodes();

				for(Node constructorNode : constructorNodes)
				{
					if(constructorNode instanceof Parameter)
					{
						Parameter constructorParameter = (Parameter)constructorNode;

						List<Node> cpNodes = constructorParameter.getChildrenNodes();

						for(Node cpNode : cpNodes)
						{
							if(cpNode instanceof VariableDeclaratorId)
							{
								VariableDeclaratorId vdi = (VariableDeclaratorId)cpNode;
								variable = vdi.getName();
							}
							else if(cpNode instanceof ReferenceType)
							{
								ReferenceType rt = (ReferenceType)cpNode;
								reference = rt.getType().toString();

								for(int i=0 ; i<interfaces.size() ; i++)
								{
									if(reference.equals(interfaces.get(i)))
									{
										if(!isInterface)
										{
											boolean dependencyFound = false;
											for(String dependency : dependencies)
											{
												String classNameFormat = "["+className+"]";
												if(dependency.contains(classNameFormat) && dependency.contains(reference))
												{
													dependencyFound = true;
													break;
												}
											}

											if(!dependencyFound)
											{
												dependencies.add(symbols.startSquareBracket+className+symbols.endSquareBracket+
														symbols.dependency_uses+symbols.startSquareBracket+symbols.declareInterface+symbols.semiColonSeparator+
														reference+symbols.endSquareBracket);
											}
										}
									}
								}
							}
							else if(cpNode instanceof PrimitiveType)
							{
								PrimitiveType pt = (PrimitiveType)cpNode;
								reference = pt.getType().toString();
							}
						}
					}
				}

				if(variable!=null)
				{
					if(reference!=null)
					{
						if(!variable.isEmpty() && !reference.isEmpty())
						{
							constructorDeclaration = symbols.publicAccess+constructorName+"("+variable+symbols.colonSeparator+reference+")";
							methodNames.add(constructorName);
							methods.add(constructorDeclaration);
						}
						else
						{
							constructorDeclaration = symbols.publicAccess+constructorName+"()";
							methodNames.add(constructorName);
							methods.add(constructorDeclaration);
						}
					}
				}
			}
		}
	}

	private void implementedInterfacesGenerator(ClassOrInterfaceDeclaration cid){
		//get the list of interfaces
		List<ClassOrInterfaceType> listImplements = cid.getImplements();

		if(listImplements != null)
		{
			for(ClassOrInterfaceType cit : listImplements)
			{
				implementsList.add(symbols.startSquareBracket+symbols.declareInterface+symbols.semiColonSeparator+cit.getName()+symbols.endSquareBracket+symbols.implementsInterface+
						symbols.startSquareBracket+className+symbols.endSquareBracket);
			}
		}
	}

	private void extended(ClassOrInterfaceDeclaration cid){
		//get the list of extends Classes(Or Interfaces in case of an interface)
		List<ClassOrInterfaceType> listExtends = cid.getExtends();
		if(listExtends != null)
		{
			for(ClassOrInterfaceType cit : listExtends)
			{
				extendsList.add(symbols.startSquareBracket+cit.getName()+symbols.endSquareBracket+symbols.extendsClass+
						symbols.startSquareBracket+className+symbols.endSquareBracket);
			}
		}
	}
}

