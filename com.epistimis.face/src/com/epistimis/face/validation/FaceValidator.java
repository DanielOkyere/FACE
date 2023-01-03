/*
 * generated by Xtext 2.28.0
 */
package com.epistimis.face.validation;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ocl.pivot.Constraint;
import org.eclipse.ocl.pivot.ExpressionInOCL;
import org.eclipse.ocl.pivot.resource.ProjectManager;
import org.eclipse.ocl.pivot.utilities.OCL;
import org.eclipse.ocl.pivot.utilities.OCLHelper;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.eclipse.ocl.pivot.utilities.PivotUtil;
import org.eclipse.ocl.pivot.utilities.Query;

import org.eclipse.ocl.xtext.completeocl.validation.CompleteOCLEObjectValidator;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import com.epistimis.face.face.FacePackage;
import com.epistimis.face.face.PurposeBase;
import com.epistimis.face.face.UopConnection;
import com.epistimis.face.face.UopUnitOfPortability;
import com.epistimis.face.generator.FaceGenerator;
import com.epistimis.face.generator.QueryUtilities;
import com.epistimis.uddl.generator.QueryProcessor;
import com.epistimis.uddl.uddl.ConceptualCharacteristic;
import com.epistimis.uddl.uddl.ConceptualEntity;
import com.epistimis.uddl.uddl.PlatformEntity;
import com.epistimis.uddl.uddl.UddlPackage;
import com.google.inject.Inject;

/**
 * This class contains custom validation rules.
 *
 * See
 * https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
public class FaceValidator extends AbstractFaceValidator {

	@Inject IQualifiedNameProvider qnp;
	@Inject QueryProcessor qp;
	@Inject QueryUtilities qu;
	
	protected static String ISSUE_CODE_PREFIX = "com.epistimis.face.";
	public static String CONSTRAINT_VIOLATION = ISSUE_CODE_PREFIX + "ConstraintViolation";
	
	/**
	 * NOTE: This does not override the method with the same name from UddlValidator. That is so that
	 * anything registered by the UddlValidator.register method is registered for the UddlPackage.
	 * @param registrar
	 * @param resourceAddress
	 */
	private void loadAndRegister(EValidatorRegistrar registrar, String resourceAddress) {
		FacePackage ePackage = FacePackage.eINSTANCE;
		URI oclURI = URI.createPlatformResourceURI(resourceAddress, true);
		registrar.register(ePackage, new CompleteOCLEObjectValidator(ePackage, oclURI));
	}

	@Override
	public void register(EValidatorRegistrar registrar) {
		super.register(registrar);
        loadAndRegister(registrar,"/com.epistimis.uddl/src/com/epistimis/uddl/constraints/specialCategoriesOfData.ocl");

		//		loadAndRegister(registrar, "/com.epistimis.face/src/com/epistimis/face/constraints/face.ocl");
//		loadAndRegister(registrar, "/com.epistimis.face/src/com/epistimis/face/constraints/uop.ocl");
//		// TODO: Before registering this, need to fix issues that are the result of grammar changes - or fix grammar
//		// to avoid them
//		//loadAndRegister(registrar, "/com.epistimis.face/src/com/epistimis/face/constraints/integration.ocl");
//		loadAndRegister(registrar, "/com.epistimis.face/src/com/epistimis/face/constraints/purpose.ocl");
	}

	
	@Check(CheckType.NORMAL)
	public void checkUsagePurposeViolates(UopUnitOfPortability uop) {
		//ResourceSet rs = uop.eResource().getResourceSet();
		//OCL ocl = OCL.newInstance(ProjectManager.CLASS_PATH);
		
		
		// Get the purpose hierarchies for this component
		EList<PurposeBase> purposes = uop.getPurpose();
		Diagnostician diagnostician = new Diagnostician();
		// Need index to attach error to the correct connection - init to -1 because we increment at top of loop
		int ndx = -1;
		for (UopConnection conn: uop.getConnection()) {
			ndx++;
			// For each connection, get the Entities referenced by that connection.
			List<PlatformEntity> referencedEntities = qu.getReferencedEntities(conn);
			for (PlatformEntity ent: referencedEntities) {
				for (PurposeBase purpose: purposes) {
					// Process all the constraints for that Purpose and Entity type
					// For now we use a hard coded a constraint
					try {
//						final @NonNull ExpressionInOCL constraint = ocl.createInvariant(UddlPackage.Literals.PLATFORM_ENTITY,SAMPLE_INVARIANT_TEXT);
//					    final Query constraintEval = ocl.createQuery(constraint);
//					    if (!constraintEval.checkEcore(ent)) {
						Diagnostic diagnostics = diagnostician.validate(ent);
						if (diagnostics.getSeverity() != Diagnostic.OK) {
							String formattedDiagnostics = PivotUtil.formatDiagnostics(diagnostics, "\n");
					    	// The constraint was not met, so that's an error
							String fmttedMessage = MessageFormat.format("UoP {0} has purpose {1} but attempts to use Entity {2} on connection {3} violating an invariant: \nDetailed diagnostics: {4}",
									qnp.getFullyQualifiedName(uop).toString(),
									purpose.getName(),
									qnp.getFullyQualifiedName(ent).toString(),
									conn.getName(),
									formattedDiagnostics);
							error(fmttedMessage,
							FacePackage.eINSTANCE.getUopUnitOfPortability_Connection(),ndx,
							CONSTRAINT_VIOLATION, qnp.getFullyQualifiedName(conn).toString());

					    }
					  } catch (final Exception e) {
						  System.out.println(MessageFormat.format("Exception processing constraints: {0} ", e.getMessage()));
					  }
					   
				}
			}
		}
		//ocl.dispose();
	}
}

/**
In order to check UoPUnitOfPortability in the editor, we need validation. What we need to do is:
1) Check the template -> query associated with each UopConnection. Determine the net PlatformEntity selected. (For 
queries with JOIN, that means constructing the net result of the JOIN. ) 
2) Evaluate a constraint 

Could add purpose to data optionally (data should inherit purpose from function but this could be for demo).

Rule receives purpose and 

See https://www.tabnine.com/code/java/classes/org.eclipse.ocl.helper.OCLHelper

Have a multimap keyed on Privacy value, value is  a collection of strings containing OCL (or could be URLs to OCL files to be registered).
Use the component privacy value to lookup all the rules that should be evaluated.
Use 

final Permission permission = (Permission) permissionInstance.eClass();
  permissionInstance.eSet(permission.getEStructuralFeature(IPermissionConstant.OBJECT), pObjectInstance);
  final String expession = permission.getConstraint().getExpession();
  final OCL<?, EClassifier, ?, ?, ?, ?, ?, ?, ?, Constraint, EClass, EObject> ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
  final OCLHelper<EClassifier, ?, ?, Constraint> helper = ocl.createOCLHelper();
  helper.setContext(permission);
  try {
    final Constraint constraint = helper.createInvariant(expession);
    final Query<EClassifier, EClass, EObject> constraintEval = ocl.createQuery(constraint);
    return constraintEval.check(permissionInstance);
  } catch (final ParserException e) {
    throw new UamClientException(MessageFormat.format("Incorrect expression: {0}. {1}", expession, e.getLocalizedMessage()), e); //$NON-NLS-1$
  }

*/