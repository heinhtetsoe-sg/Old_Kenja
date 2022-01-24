-- $Id: 599d7e30f2bc8463b80490c9704945916f39af67 $

drop table MOCK_PERFECT_DAT_OLD
create table MOCK_PERFECT_DAT_OLD like MOCK_PERFECT_DAT
insert into MOCK_PERFECT_DAT_OLD select * from MOCK_PERFECT_DAT

drop table MOCK_PERFECT_DAT

create table MOCK_PERFECT_DAT \
      (YEAR             varchar(4) not null, \
       COURSE_DIV       varchar(1) not null, \
       GRADE            varchar(2) not null, \
       MOCK_SUBCLASS_CD varchar(6) not null, \
       PERFECT          smallint not null, \
       PASS_SCORE       smallint, \
       REGISTERCD       varchar(8), \
       UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

insert into MOCK_PERFECT_DAT \
select \
     YEAR, \
     COURSE_DIV, \
     GRADE, \
     MOCK_SUBCLASS_CD, \
     PERFECT, \
     cast(null as smallint) AS PASS_SCORE, \
     REGISTERCD, \
     UPDATED \
FROM \
    MOCK_PERFECT_DAT_OLD

alter table MOCK_PERFECT_DAT add constraint pk_mock_perfect_d \
      primary key (YEAR, COURSE_DIV, GRADE, MOCK_SUBCLASS_CD)
