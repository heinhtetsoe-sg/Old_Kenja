-- kanji=漢字
-- $Id: 962bfd42b746f59dfd37d4ba07dc5910a675ab94 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table MOCK_DAT_OLD
create table MOCK_DAT_OLD like MOCK_DAT
insert into MOCK_DAT_OLD select * from MOCK_DAT

drop table MOCK_DAT

create table MOCK_DAT \
    (YEAR               varchar(4) not null, \
     MOCKCD             varchar(9) not null, \
     SCHREGNO           varchar(8) not null, \
     MOCK_SUBCLASS_CD   varchar(6) not null, \
     GRADE              varchar(2), \
     HR_CLASS           varchar(3), \
     ATTENDNO           varchar(3), \
     NAME_KANA          varchar(120), \
     SEX                varchar(3), \
     FORMNO             varchar(6), \
     EXAMNO             varchar(7), \
     SCHOOLCD           varchar(6), \
     COURSEDIV          varchar(3), \
     STATE_EXAM         varchar(3), \
     EXECUTION_DAY      date, \
     SCHEDULE           varchar(2), \
     TOTALWISHRANK      smallint, \
     TOTALWISHCNT       smallint, \
     JUDGEEVALUATION    varchar(3), \
     JUDGEVALUE         varchar(5), \
     SUBCLASS_NAMECD    varchar(10), \
     SUBCLASS_NAME      varchar(30), \
     POINT_CONVERSION   smallint, \
     SCORE              smallint, \
     SCORE_DI           varchar(2), \
     DEVIATION          decimal (4,1), \
     EVALUATION_S       varchar(3), \
     EVALUATION_N       varchar(3), \
     EVALUATION_P       varchar(3), \
     EVALUATION_T       varchar(3), \
     RANK               smallint, \
     REGISTERCD         varchar(8), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table MOCK_DAT add constraint pk_mock_dat primary key (YEAR, MOCKCD, SCHREGNO, MOCK_SUBCLASS_CD)

insert into MOCK_DAT \
select \
    YEAR, \
    MOCKCD, \
    SCHREGNO, \
    MOCK_SUBCLASS_CD, \
    GRADE, \
    HR_CLASS, \
    ATTENDNO, \
    NAME_KANA, \
    SEX, \
    FORMNO, \
    EXAMNO, \
    SCHOOLCD, \
    COURSEDIV, \
    STATE_EXAM, \
    EXECUTION_DAY, \
    SCHEDULE, \
    TOTALWISHRANK, \
    TOTALWISHCNT, \
    JUDGEEVALUATION, \
    JUDGEVALUE, \
    SUBCLASS_NAMECD, \
    SUBCLASS_NAME, \
    POINT_CONVERSION, \
    SCORE, \
    CAST(NULL AS varchar(2)) AS SCORE_DI, \
    DEVIATION, \
    EVALUATION_S, \
    EVALUATION_N, \
    EVALUATION_P, \
    EVALUATION_T, \
    RANK, \
    REGISTERCD, \
    UPDATED \
from \
    MOCK_DAT_OLD

