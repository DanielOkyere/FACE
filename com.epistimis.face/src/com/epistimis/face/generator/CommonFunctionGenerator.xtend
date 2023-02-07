package com.epistimis.face.generator

import com.epistimis.face.face.FaceElement
import com.epistimis.face.face.IntegrationUoPInstance
import com.epistimis.face.face.UoPClientServerRole
import com.epistimis.face.face.UopClientServerConnection
import com.epistimis.face.face.UopCompositeTemplate
import com.epistimis.face.face.UopMessageType
import com.epistimis.face.face.UopPlatformSpecificComponent
import com.epistimis.face.face.UopPortableComponent
import com.epistimis.face.face.UopProgrammingLanguage
import com.epistimis.face.face.UopQueuingConnection
import com.epistimis.face.face.UopSingleInstanceMessageConnection
import com.epistimis.face.face.UopSynchronizationStyle
import com.epistimis.face.face.UopTemplate
import com.epistimis.face.face.UopUnitOfPortability
import com.epistimis.uddl.uddl.PlatformDataModel
import com.epistimis.uddl.uddl.PlatformEntity
import java.util.ArrayList
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGenerator2
import org.eclipse.xtext.generator.IGeneratorContext
import com.epistimis.uddl.generator.CommonDataStructureGenerator
import com.epistimis.uddl.uddl.PlatformDataType

abstract class CommonFunctionGenerator extends CommonDataStructureGenerator implements IFaceLangGenerator {


	List<UopUnitOfPortability> processedComponents;

	//@Inject
	protected IGenerator2  fg;
	
	//@Inject 
	protected QueryUtilities qu;
	
	// @Inject
	CommonDataStructureGenerator dsg ;
	
	
	def String getSrcExtension();

	/**
	 * Eventually this code generator can look at the version of language (look up SupportingComponents for runtimes and see
	 * which version of the runtime is being used. The assumption is that the compiler must be at that version, so we can use
	 * language features from that version if desired)
	 */
	
	new(QueryUtilities qu, CommonDataStructureGenerator dsg) {
		initialize();
		this.qu = qu;
		this.dsg = dsg;
	}
	
	override void initialize() {
		super.initialize();	
		
 		if (processedComponents === null) {
	 		processedComponents = new ArrayList<UopUnitOfPortability>();
		}

	}
	
	override void cleanup() {
		super.cleanup();
		
		processedComponents.clear();
	}
	override afterGenerate(Resource input, IFileSystemAccess2 fsa, IGeneratorContext context) {
//		cleanup();
	}
	
	override beforeGenerate(Resource input, IFileSystemAccess2 fsa, IGeneratorContext context) {
//		initialize();
	}
	
	override doGenerate(Resource input, IFileSystemAccess2 fsa, IGeneratorContext context) {

		// Further filter by all of those with appropriate language selection.
		for (inst : input.allContents.toIterable.filter(IntegrationUoPInstance).filter([realizes.transportAPILanguage === UopProgrammingLanguage.CPP])){
			
		} 		
	}
	
	/**
	 * Only UopUnitOfPortability will have  files
	 */
	def String generateFileName(FaceElement obj);
	
	override processAnInstance(IntegrationUoPInstance inst,  IFileSystemAccess2 fsa, IGeneratorContext context) {
		if (inst.realizes === null) {
			System.out.println("No realized component for instance " + qnp.getFullyQualifiedName(inst).toString);
			return;
		}
		
		val List<PlatformEntity> entities = qu.getReferencedEntities(inst.realizes);
		
		processAComponent(inst.realizes,entities, fsa, context);
			
//       // Do we want to generate code for the wiring / integration of function instances?
//       fsa.generateFile(
//         ROOT_DIR + inst.name + ".cpp",
//         inst.compile)
		
	}
	override processAComponent(UopUnitOfPortability comp,List<PlatformEntity> entities, IFileSystemAccess2 fsa, IGeneratorContext context) {


		// Generate the stubs for the functions that need to be used
		if (!processedComponents.contains(comp)) {
			processedComponents.add(comp);
	       fsa.generateFile(
	        	getRootDirectory() + comp.name + getSrcExtension(),
	         comp.compile(entities))
		}
	}
	
	/**
	 * Do Portable specific code gen here
	 */
	def dispatch compile (UopPortableComponent uop,List<PlatformEntity> entities)'''
	«uop.compileUopCommon(entities)»	
	'''
	
	/**
	 * Do PlatformSpecific codegen here
	 */
	def dispatch compile (UopPlatformSpecificComponent uop,List<PlatformEntity> entities){
	'''
	«uop.compileUopCommon(entities)»
	'''
	}
	/**
	 * Do common code generation for both Portable and PlatformSpecific Components.
	 * 
	 * Commonalities:
	 * 1) All the input ports define input parameters to the generated function.
	 * 2) The output port(s) define outputs from the generated function. If there is more than one output port
	 * then we must create either a Tuple or have in/out parameters (References in CPP)
	 * 3) Every function starts with a call to standard code to do dynamic checks (for consent) followed by
	 * an assertion that whatever makes it past that point is OK.
	 * 
	 * For each port, we need to make sure the data structure has been generated and the header for that structure 
	 * included in this file.
	 * 
	 * TODO: parameter list doesn't properly address the possibility of multiple entities matching a connection
	 */
	def String compileUopCommon(UopUnitOfPortability uop,List<PlatformEntity> entities);

	def dispatch parmList(UopTemplate templ) {
		if (templ.specification.empty) {
			/** If there is no specification, then just use the boundQuery completely - either boundQuery or effectiveQuery must be specified
			 * That means parsing the boundQuery to find the referenced Entities. 
			 */
			templ.boundQuery
		}
		'''
		'''
	}
	
	def dispatch parmList(UopCompositeTemplate templ) {
		'''
		'''
	}
	def dispatch parmList(UopClientServerConnection conn){
		var UopMessageType parameterType;
		if (conn.role == UoPClientServerRole.CLIENT) {
			// This is the client in a C/S relationship, so this connection outputs the request and inputs the response
		} else {
			// this is the server in a C/S relationship, so this connection inputs the request and outputs the response
			parameterType = conn.requestType
		}
	}
	/**
	 * QueueingConnections
	 */
	
	def dispatch parmList(UopQueuingConnection conn){
		if (conn.synchronizationStyle == UopSynchronizationStyle.BLOCKING) {
			// If this is blocking, we must wait for the return 
		} else {
			// Nonblocking just continues - must have some way to capture events asynchronously
		}		
	}
	def dispatch parmList(UopSingleInstanceMessageConnection conn){
		if (conn.synchronizationStyle == UopSynchronizationStyle.BLOCKING) {
			// If this is blocking, we must wait for the return
		} else {
			// Nonblocking just continues - must have some way to capture events asynchronously
		}		
	}
	
	def dispatch compile(IntegrationUoPInstance inst) {
		'''
		'''
	}
	
	/** These methods reroute the call to the specified data structure generator */
	override getRootDirectory() {
		return dsg.rootDirectory
	}
	
	override getFileExtension() {
		return dsg.fileExtension
	}
	
	override getImportPrefix() {
		return dsg.importPrefix
	}
	
	override getImportSuffix() {
		return dsg.importSuffix
	}
	
	override getTypeDefPrefix() {
		return dsg.typeDefPrefix
	}
	
	override getNamespaceKwd() {
		return dsg.namespaceKwd
	}
	
	override getClazzKwd() {
		return dsg.clazzKwd
	}
	
	override getSpecializesKwd() {
		return dsg.specializesKwd
	}
	
	override getCompositionVisibility() {
		return dsg.compositionVisibility
	}
	
	override getPDTTypeString(PlatformDataType pdt) {
		return dsg.getPDTTypeString(pdt);
	}
	
}