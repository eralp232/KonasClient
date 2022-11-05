from pathlib import Path

modules = {}

def parseModule(name, category):

	name = name.replace(".java", "")

	name = name.replace("Module", "")

	if not category in modules:
		modules[category] = []

	modules[category].append(name)

entries = Path('src/main/java/me/darki/konas/module/modules/')
for entry in sorted(entries.iterdir()):
    if entry.is_dir():
    	for module in entry.iterdir():
    		parseModule(module.name, entry.name.capitalize())

f = open("ml.txt", "w")

for category in modules:
	f.write("**" + category + "**\n")
	for module in modules[category]:
		f.write(module + "\n")
	f.write(" \n")

f.close()
    	
