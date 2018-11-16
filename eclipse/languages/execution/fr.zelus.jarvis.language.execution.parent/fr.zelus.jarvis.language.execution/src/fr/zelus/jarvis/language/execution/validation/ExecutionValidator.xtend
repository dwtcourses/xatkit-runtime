/*
 * generated by Xtext 2.12.0
 */
package fr.zelus.jarvis.language.execution.validation

import org.eclipse.xtext.validation.Check
import org.eclipse.emf.ecore.resource.Resource
import static java.util.Objects.isNull
import fr.zelus.jarvis.platform.Parameter
import fr.zelus.jarvis.language.execution.util.PlatformRegistry
import fr.zelus.jarvis.execution.ExecutionModel
import fr.zelus.jarvis.execution.ExecutionPackage
import fr.zelus.jarvis.execution.ActionInstance

/**
 * This class contains custom validation rules. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
class ExecutionValidator extends AbstractExecutionValidator {
	
	@Check
	def checkOrchestrationModelValidImports(ExecutionModel model) {
		model.imports.forEach[i | 
			println("Checking import " + i)
			var Resource moduleResource = PlatformRegistry.getInstance.loadPlatform(i)
			if(isNull(moduleResource)) {
				error('Module ' + i + "does not exist", ExecutionPackage.Literals.EXECUTION_MODEL__IMPORTS)
			}
		]
	}
	
	@Check
	def checkValidActionInstance(ActionInstance actionInstance) {
		val actionParameters = actionInstance.action.parameters;
		val actionInstanceParameters = actionInstance.values.map[v | v.parameter]
		for(Parameter p : actionParameters) {
			if(!actionInstanceParameters.contains(p)) {
				println('The parameter ' + p.key + ' is not set in the action instance')
				error('The parameter ' + p.key + ' is not set in the action instance', ExecutionPackage.Literals.ACTION_INSTANCE__VALUES)
			}
		}
	}
}
