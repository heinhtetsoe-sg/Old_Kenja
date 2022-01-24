-- kanji=$B4A;z(B
-- $Id: pre_school_hdat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- $BCm0U(B:$B$3$N%U%!%$%k$O(B EUC/LF$B$N$_(B $B$G$J$1$l$P$J$i$J$$!#(B
-- $BE,MQJ}K!(B:
--    1.$B%G!<%?%Y!<%9@\B3(B
--    2.db2 +c -f <$B$3$N%U%!%$%k(B>
--    3.$B%3%_%C%H$9$k$J$i!"(Bdb2 +c commit$B!#$d$jD>$9$J$i!"(Bdb2 +c rollback
--

drop table PRE_SCHOOL_HDAT

create table PRE_SCHOOL_HDAT \
      (YEAR         varchar(4)   not null, \
       SEMESTER     varchar(1)   not null, \
       GRADE        varchar(2)   not null, \
       HR_CLASS     varchar(3)   not null, \
       HR_NAME      varchar(15), \
       HR_NAMEABBV  varchar(5), \
       REGISTERCD   varchar(8), \
       UPDATED      timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table PRE_SCHOOL_HDAT add constraint PK_PRE_SCHOOL_HDAT primary key (YEAR, SEMESTER, GRADE, HR_CLASS)

