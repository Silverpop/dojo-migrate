if [[ $# -eq 0 ]] ; then
    echo 'You need a JS file...'
    exit 0
fi

echo 'Processing' $1

#http://graspjs.com/docs/equery/
grasp -ie 'dojo.publish($a,[_$b])' -R 'topic.publish({{a}},{{ b | join , }})' $1
grasp -ie 'dojo.subscribe($a,$b,$c)' -R 'topic.subscribe({{a}},lang.hitch({{b}},{{c}}))' $1

grasp -ie 'dojo.style($a,{$b:$c})' -R 'domStyle.set({{a}},{ {{b}} : {{c}} })' $1
grasp -ie 'dojo.style($a,$b)' -R 'domStyle.get({{a}},{{b}})' $1

grasp -ie 'dojo.safeMixin' -R 'declare.safeMixin' $1

grasp -ie 'dojo.declare($name,$includes,$body);' -R 'define( [], \n    function() {\n        return declare({{includes}}, {{body}} );\n    }\n);' $1

echo 'Grasp conversion finished, now use Groovy script, dojo19migration.groovy'

#deprecated, just included in case you want to use them manually
#grasp -ie 'dojo.toJson' -R 'JSON.stringify' $1      # deprecated, use groovy
#grasp -ie 'dojo.fromJson' -R 'JSON.parse' $1        # deprecated, use groovy