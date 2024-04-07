all: fabricMod spigotPlugin

fabricMod:
	cd fabricMod && make

spigotPlugin:
	cd spigotPlugin && make
