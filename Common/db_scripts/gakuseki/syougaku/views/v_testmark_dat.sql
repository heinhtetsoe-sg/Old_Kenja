
drop view v_testmark_dat

create view v_testmark_dat \
	(studyyear, \
	 studysemester, \
	 classcd, \
	 subclasscd, \
	 studyclasscd, \
	 testkindcd, \
	 testitemcd, \
	 markdate, \
	 schregno, \
	 s_quizno, \
	 answer, \
	 point, \
	 testtotal, \
	 correctanswers) \
as select \
	t1.studyyear, \
	t1.studysemester, \
	t1.classcd, \
	t1.subclasscd, \
	t1.studyclasscd, \
	t1.testkindcd, \
	t1.testitemcd, \
	t1.testmarkdate, \
	t1.schregno, \
	'1' as s_quizno, \
	t1.testans1 as testans, \
	t1.testsum, \
	t1.correctanswers \
from 	testmark_dat t1, \
	testbase_dat t2 \
where	t1.studyyear     = t2.studyyear \
and	t1.studysemester = t2.studysemester \
and	t1.classcd       = t2.classcd \
and	t1.subclasscd    = t2.subclasscd \
and	t1.studyclasscd  = t2.studyclasscd \
and	t1.testkindcd	 = t2.testkindcd \
and	t1.testmarkdate  = t2.testmarkdate \
and	'1'              = t2.quizno \
union select \
	t1.studyyear, \
	t1.studysemester, \
	t1.classcd, \
	t1.subclasscd, \
	t1.studyclasscd, \
	t1.testkindcd, \	
	t1.testitemcd, \
	t1.testmarkdate, \
	t1.schregno, \
	'2' as s_quizno, \
	t1.testans2 as testans, \
	t2.point, \
	t1.testsum, \
	t1.correctanswers \
from	testmark_dat t1, \
	testbase_dat t2 \
where	t1.studyyear     = t2.studyyear \
and	t1.studysemester = t2.studysemester \
and	t1.classcd       = t2.classcd \
and	t1.subclasscd	 = t2.subclasscd \
and	t1.studyclasscd	 = t2.studyclasscd \
and	t1.testkindcd	 = t2.testkindcd \
and	t1.testitemcd	 = t2.testitemcd \
and	t1.testmarkdate	 = t2.testmarkdate \
and	'2' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '3' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '3' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '4' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '4' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '5' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '5' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '6' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '6' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '7' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '7' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '8' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '8' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '9' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '9' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '10' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '10' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '11' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '11' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '12' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '12' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '13' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '13' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '14' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '14' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '15' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '15' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '16' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '16' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '17' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '17' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '18' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '18' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '19' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '19' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '20' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '20' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '21' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '21' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '22' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '22' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '23' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '23' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '24' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '24' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '25' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '25' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '26' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '26' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '27' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '27' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '28' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '28' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '29' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '29' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '30' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '30' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '31' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '31' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '32' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '32' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '33' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '33' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '34' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '34' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '35' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '35' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '36' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '36' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '37' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '37' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '38' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '38' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '39' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '39' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '40' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '40' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '41' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '41' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '42' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '42' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '43' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '43' = t2.quizno \
union select \
        t1.studyyear, \
        t1.studysemester, \
        t1.classcd, \
        t1.subclasscd, \
        t1.studyclasscd, \
        t1.testkindcd, \
        t1.testitemcd, \
        t1.testmarkdate, \
        t1.schregno, \
        '44' as s_quizno, \
        t1.testans2 as testans, \
        t2.point, \
        t1.testsum, \
        t1.correctanswers \
from    testmark_dat t1, \
        testbase_dat t2 \
where   t1.studyyear     = t2.studyyear \
and     t1.studysemester = t2.studysemester \
and     t1.classcd       = t2.classcd \
and     t1.subclasscd    = t2.subclasscd \
and     t1.studyclasscd  = t2.studyclasscd \
and     t1.testkindcd    = t2.testkindcd \
and     t1.testitemcd    = t2.testitemcd \
and     t1.testmarkdate  = t2.testmarkdate \
and     '44' = t2.quizno \
