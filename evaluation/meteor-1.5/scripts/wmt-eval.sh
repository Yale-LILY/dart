#/usr/bin/env bash

METEOR=$(dirname $0)/../meteor-*.jar
FMT=$(dirname $0)/wmt_fmt.py

if [[ $# != 1 ]] ; then
    echo "Score an entire WMT evaluation"
    echo "Usage: $0 <plain-dir>"
    echo "where plain-dir contains: references system-outputs"
    exit 1
fi

PLAIN=$1

for SET in $(ls $PLAIN/system-outputs) ; do
    for LANG in cs de es fr ru ; do
        echo "$LANG-en:"
        for HYP in $PLAIN/system-outputs/$SET/$LANG-en/* ; do
            SYS=$(echo $HYP | sed -re "s/.+$SET\.$LANG-en\.//")
            echo " $SYS"
            java -Xmx700M -jar $METEOR $HYP $PLAIN/references/$SET-ref.en -l en -norm -t universal | $FMT $LANG-en $SET $SYS Meteor
        done
    done
    for LANG in cs de es fr ru ; do
        echo "en-$LANG"
        for HYP in $PLAIN/system-outputs/$SET/en-$LANG/* ; do
            SYS=$(echo $HYP | sed -re "s/.+$SET\.en-$LANG\.//")
            echo " $SYS"
            java -Xmx700M -jar $METEOR $HYP $PLAIN/references/$SET-ref.$LANG -l $LANG -norm -t universal | $FMT en-$LANG $SET $SYS Meteor
        done
    done
done
