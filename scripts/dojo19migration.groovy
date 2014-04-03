/**
 * Migrations not addressed yet:
 * connect -> on | aspect
 * new classpath.Class -> new Class
 * Deferred requires
 * Leftover requires
 * Requires inside of declared objects/functions
 */
def path = args[0]
def file = new File(path)
if (!file.exists()) {
    System.err << 'Double check your file path.\n'
    System.err << 'Trying to load: ' + path + '\n'
    System.exit(1)
}

def fileContents = file.text
def requiresMap = [
        'topic':'"dojo/topic"',
        'lang':'"dojo/_base/lang"',
        'aspect':'"dojo/aspect"',
        'JSON':'"dojo/json"',
        'domConstruct':'"dojo/dom-construct"',
        'domClass':'"dojo/dom-class"',
        'array':'"dojo/_base/array"',
        'win':'"dojo/_base/window"',
        'Deferred':'"dojo/Deferred"',
        'DeferredList':'"dojo/DeferredList"',
        'locale':'"dojo/date/locale"',
        'domStyle':'"dojo/dom-style"'
]

def functionReplacements = [
        'dojo.toJson':'JSON.stringify',
        'dojo.fromJson':'JSON.parse',
        'dojo.create':'domConstruct.create',
        'this.subscribe':'topic.subscribe',
        'this.publish':'topic.publish',
        'dojo.addClass':'domClass.add',
        'dojo.removeClass':'domClass.remove',
        'dojo.hasClass':'domClass.contains',
        'dojo.place':'domConstruct.place',
        'dojo.safeMixin':'declare.safeMixin',
        'dojo.body':'win.body',
        'dojo.Deferred':'Deferred',
        'dojo.date.locale.format':'locale.format',
        'dojo.filter':'array.filter',
        'dojo.forEach':'array.forEach',
        'dojo.hitch':'lang.hitch',
        'dojo.indexOf':'array.indexOf',
        'dojo.mixin':'lang.mixin',
        'dojo.replace':'lang.replace'
]

def migrateRequires = {
    // Process requires, but add in some common libraries we usually need
    def requires = ['\n\t"dojo/_base/declare"':'\n\t\tdeclare','\n\t"dojo/aspect"':'\n\t\taspect','\n\t"dojo/on"':'\n\t\ton'] as TreeMap
    fileContents.eachLine { line ->
        if (line.trim()) {
            if (line.startsWith('dojo.require')) {
                def include = line.split('\"')[1]
                def require = '\n\t\"' + include.replace('.', '/') + '\"'
                def reference = '\n\t\t' + require.split('/').last().split('\"').first()
                requires.put(require, reference)
            }
        }
    }

    fileContents = fileContents.replace(fileContents.substring(0, fileContents.indexOf('define')), '').replace('define( []', 'define( ' + requires.keySet().toString())
    fileContents = fileContents.replaceFirst('function\\(\\)', 'function(' + requires.values().toString().replace('[', '').replace(']', '') + ')')
}

def migrateDeclares = {
    def parentObjects = []
    def declares = fileContents.substring(fileContents.indexOf('declare(') + 9, fileContents.indexOf(']', fileContents.indexOf('declare(')))
    (declares.trim().split(',') as HashSet).each {
        parentObject ->
            parentObjects.add(parentObject.split('\\.').last())
    }
    fileContents = fileContents.replace(declares, parentObjects.toString().replace('[', '').replace(']', ''))
}

def migrateSimpleFunctions = {
    functionReplacements.keySet().each{ key ->
        fileContents = fileContents.replaceAll(key, functionReplacements[key])
    }
}

def injectDependency = {
    dependencyName, referenceName ->
        def sb = new StringBuffer(fileContents)
        def endOfDependsMarker = sb.indexOf("]")
        sb.insert(endOfDependsMarker, ',\n\t' + dependencyName)

        def endOfDependsReferenceMarker = sb.indexOf(")")
        sb.insert(endOfDependsReferenceMarker, ',\n\t\t' + referenceName)

    fileContents = sb.toString()
}

def migrateUsedRequires = {

    def sb = new StringBuffer(fileContents)

    requiresMap.keySet().each{ library ->
        if (sb.find("${library}\\.")) {
            def endOfDependsMarker = sb.indexOf("]")
            sb.insert(endOfDependsMarker, ',\n\t' + requiresMap[library])
            def endOfDependsReferenceMarker = sb.indexOf(")")
            sb.insert(endOfDependsReferenceMarker, ',\n\t\t' + library)
        }
    }

    fileContents = sb.toString()
}

def migrateTemplate = {
    def dojoCache = fileContents.find(/dojo.cache\(.*\)/)
    if (dojoCache) {
        def match = [];
        dojoCache.eachMatch(/("|')[^("|')]*("|')/) {match = it}
        if (!match.empty) {
            injectDependency(match[0].replaceFirst('"', '"dojo/text!./'), 'template')
        }

        fileContents = fileContents.replace(dojoCache, 'template')
    }
}

migrateRequires()
migrateDeclares()
migrateTemplate()
migrateSimpleFunctions()
migrateUsedRequires()

def newPath = path.replace('.js', '_migrate.js')
def newFile = new File(newPath)
newFile.write(fileContents);

System.out << 'Saved file to: ' + newPath + '\n'
System.out << 'You now need to manually verify and migrate the remaining functions.\n'
System.exit(0)