/*
 * generated by Xtext 2.12.0
 */
package fr.zelus.jarvis.language.ide

import com.google.inject.Guice
import fr.zelus.jarvis.language.OrchestrationRuntimeModule
import fr.zelus.jarvis.language.OrchestrationStandaloneSetup
import org.eclipse.xtext.util.Modules2

/**
 * Initialization support for running Xtext languages as language servers.
 */
class OrchestrationIdeSetup extends OrchestrationStandaloneSetup {

	override createInjector() {
		Guice.createInjector(Modules2.mixin(new OrchestrationRuntimeModule, new OrchestrationIdeModule))
	}
	
}
