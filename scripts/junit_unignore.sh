
function unignore(){
 	sed -e 's/^\s*@Ignore\s*$/\/\/\t@Ignore/' $1 > $(dirname $1)/tmp.java && mv $(dirname $1)/tmp.java $1
}

if [ "$2" == "unignore" ];
then
	unignore $1
else
	find ../test -name "*PerformanceTest.java" -exec bash $0 '{}' "unignore" \;
fi

