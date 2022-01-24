-- kanji=$B4A;z(B
-- $Id: 11be5bd56c9c42759fba4e1ff7d198a46cab1c0f $

-- $BCm0U(B:$B$3$N%U%!%$%k$O(B EUC/LF$B$N$_(B $B$G$J$1$l$P$J$i$J$$!#(B
-- $BE,MQJ}K!(B:
--    1.$B%G!<%?%Y!<%9@\B3(B
--    2.db2 +c -f <$B$3$N%U%!%$%k(B>
--    3.$B%3%_%C%H$9$k$J$i!"(Bdb2 +c commit$B!#$d$jD>$9$J$i!"(Bdb2 +c rollback
--

drop table CLASS_REQUIRED_DAT

create table CLASS_REQUIRED_DAT( \
     SCHREGNO       VARCHAR(8) NOT NULL, \
     CLASSCD        VARCHAR(2) NOT NULL, \
     SCHOOL_KIND    VARCHAR(2) NOT NULL, \
     ERR_FLG        VARCHAR(1), \
     CURRICULUM_CD  VARCHAR(1), \
     COURSECD       VARCHAR(1), \
     MAJORCD        VARCHAR(3), \
     SEQ            VARCHAR(2), \
     REGISTERCD     VARCHAR(8), \
     UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table CLASS_REQUIRED_DAT add constraint PK_CLASSREQUIRE primary key (SCHREGNO, CLASSCD, SCHOOL_KIND)
