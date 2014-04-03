dojo-migrate
============

Helper for Dojo 1.5 -> 1.9 migration

Background
----------
Trying to collect some helpers for migrating Dojo 1.5 classes over to 1.9

Definitely still a work in progress, not fully automated

Not Covered
--------------
You will still need to migrate some things by hand
+   connect -> on | aspect
+   new classpath.Class -> new Class
+   Deferred requires
+   Leftover requires
+   Requires inside of declared objects/functions
+   Templates

Basics
--------------
[Official Dojo Migration Guide](http://dojotoolkit.org/reference-guide/1.9/releasenotes/migration-2.0.html)

```
dojo.declare(); -> function (declare) { return declare(); }

dojo.provide(); -> removed, although optional argument can be passed into returned declare
dojo.require(); -> migrated into define array & passed into anonymous function returning declare
dojo.*() -> migrated to new syntax using Dojo's migration guide and documentation
```

Getting Started
---------------
You should have [Node.js](http://nodejs.org/), [Grasp JS](http://graspjs.com/), and [Groovy](http://groovy.codehaus.org/) installed and configured

Initial structural search-replace
+   Use graspDojo.sh bash script to execute the initial structural migration with grasp
After a few structural search replaces, most of the remaining migration task is search/replace
+   Use dojo19migration.groovy to migrate dojo requires, declare arguments, dojo cache templates, and simple functions.

Manually Executing Commands
---------------

Add '-i' argument to grasp to save the file instead of outputting to terminal, like:

    grasp -ie 'replaceMe' 'replacedText' file.js

Migrating basic structure using grasp

    grasp -ie 'dojo.declare($name,$includes,$body);' -R 'define( [], function() { return declare({{includes}}, {{body}} ); } );' file.js

Migrating functions with structural changes

    grasp -e 'dojo.publish($a,[$b])' -R 'topic.publish({{a}},{{b}})' file.js
    grasp -e 'dojo.subscribe($a,$b,$c)' -R 'topic.subscribe({{a}},lang.hitch({{b}},{{c}}))' file.js

Populating requires, migrating simple functions

    Use the dojo19migration.groovy with your .js file path as the CLI argument

Contributing
---------------
Technically, the remaining 90% of dojo features could be automated. Please log defects, feature requests, and questions. We will try our best to respond to feedback. Pull requests are welcome.


