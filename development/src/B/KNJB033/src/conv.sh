#!/bin/sh

for n in `find . -type f -print | grep -v /CVS/ ` ; do
	d=`dirname $n`
	f=`basename $n`
	echo "=========== $d ==== $f"

	mv $n $d/nkftmp.$f
	nkf -Se -Lu < $d/nkftmp.$f > $n
	rm -f $d/nkftmp.$f

	if [ $f == build.xml ] ; then
		mv $n $d/sedtmp.$f
		sed -e 's/="Shift_JIS"/="EUC-JP"/g' -e 's/="SJIS"/="EUCJIS"/g' < $d/sedtmp.$f > $n
		rm -f $d/sedtmp.$f
	fi
done

