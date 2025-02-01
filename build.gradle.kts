import toni.blahaj.setup.modImplementation
import toni.blahaj.setup.modRuntimeOnly

plugins {
	id("toni.blahaj")
}

blahaj {
	setup {
		deps.modImplementation("toni.txnilib:${mod.loader}-${mod.mcVersion}:1.0.22")
		modloaderRequired("txnilib")

		if (mod.isForge) {
			deps.compileOnly(deps.annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)
			deps.implementation(deps.include("io.github.llamalad7:mixinextras-forge:0.4.1")!!)
		}

		if (mod.projectName == "1.21.1-fabric")
		{
			deps.modRuntimeOnly(modrinth("spark", "1.10.109-fabric"))
			deps.modRuntimeOnly(modrinth("fabric-permissions-api", "0.3.1"))
		}

		forgeConfig()
	}
}