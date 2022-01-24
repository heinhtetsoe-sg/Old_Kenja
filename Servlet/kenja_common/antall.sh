#!/bin/sh

# /**
#  * $Id: antall.sh 56573 2017-10-22 11:14:13Z maeshiro $
#  * @author tamura
#  */

__ANT_OPTS=$ANT_OPTS
__ANT_ARGS=$ANT_ARGS

export ANT_OPTS="$ANT_OPTS -Djava.awt.headless=true"
export ANT_ARGS=$*

fn_ant() {
    echo "=============================== $1 == $ANT_OPTS == $ANT_ARGS =="
    ant -buildfile $1
    if [ $? != 0 ]; then
        echo -e "\nstop! $1"
        exit 1
    fi
}

DIRTOP=..
if [ -f ./kenja_common/build.xml ]; then
    DIRTOP=.
fi

if [ ! -f $DIRTOP/kenja_common/build.xml ]; then
    echo "ERROR: not found dir 'kenja_common'"
    exit 1
fi

fn_ant $DIRTOP/kenja_common/build.xml
fn_ant $DIRTOP/kenja/build.xml

for n in $DIRTOP/kenja_hiro/* ; do
    if [ -s $n/build.xml -a ! -f $n/skip-antall.txt ]; then
        fn_ant $n/build.xml
    fi
done


ANT_OPTS=$__ANT_OPTS
ANT_ARGS=$__ANT_ARGS

echo "$0 done"
# eof

