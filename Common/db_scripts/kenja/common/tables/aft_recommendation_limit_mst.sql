-- kanji=漢字
-- $Id: 66349c9ec1bc7563d1b69dca0492f1edf26f1eb2 $

-- 駅年度マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table AFT_RECOMMENDATION_LIMIT_MST

create table AFT_RECOMMENDATION_LIMIT_MST ( \
    YEAR                   varchar(4) not null, \
    RECOMMENDATION_CD      varchar(4) not null, \
    DEPARTMENT_S           varchar(2), \
    DEPARTMENT_H           varchar(2), \
    DISP_ORDER             varchar(2), \
    FACULTY_NAME           varchar(90), \
    FACULTY_ABBV           varchar(60), \
    DEPARTMENT_NAME        varchar(90), \
    DEPARTMENT_ABBV        varchar(60), \
    DEPARTMENT_ABBV2       varchar(15), \
    LIMIT_COUNT_S          smallint, \
    LIMIT_COUNT_H          smallint, \
    WITHOUT_H_FLG          varchar(1), \
    DEPARTMENT_LIST_CD     varchar(9), \
    DEPARTMENT_LIST_ORDER  varchar(2), \
    FACULTY_LIST_NAME      varchar(120), \
    DEPARTMENT_LIST_NAME   varchar(120), \
    SCHOOL_CD              varchar(8), \
    FACULTYCD              varchar(3), \
    DEPARTMENTCD           varchar(3), \
    REGISTERCD             varchar(10), \
    UPDATED                timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_RECOMMENDATION_LIMIT_MST add constraint PK_AFT_RECOMMENDATION_LIMIT_MST primary key (YEAR, RECOMMENDATION_CD)
