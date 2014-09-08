
function ignore(){
 	sed -e 's/^\s*\/\/\s*@Ignore\s*$/\t@Ignore/' $1 > $(dirname $1)/tmp.java && mv $(dirname $1)/tmp.java $1
}

if [ "$2" == "ignore" ];
then
	ignore $1
else
	find ../test -name "*PerformanceTest.java" -exec bash $0 '{}' "ignore" \;
fi

