-- kanji=漢字
-- $Id: 1e671596cf4ab68cd92fab22dc7780e5d149a26c $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金収入伺いデータ

drop table LEVY_CARRY_OVER_DAT

create table LEVY_CARRY_OVER_DAT \
( \
        "SCHOOLCD"                  varchar(12) not null, \
        "SCHOOL_KIND"               varchar(2)  not null, \
        "YEAR"                      varchar(4)  not null, \
        "SCHREGNO"                  varchar(8)  not null, \
        "INCOME_L_CD"               varchar(2)  not null, \
        "INCOME_M_CD"               varchar(2)  not null, \
        "ATTACHED_YEAR"             varchar(4), \
        "LEVY_L_NAME"               varchar(90), \
        "LEVY_L_ABBV"               varchar(90), \
        "LEVY_M_NAME"               varchar(90), \
        "LEVY_M_ABBV"               varchar(90), \
        "CARRY_OVER_MONEY"          int, \
        "DIFFERENCE_MONEY"          int, \
        "TO_INCOME_FLG"             varchar(1), \
        "TO_SCHOOL_KIND"            varchar(2), \
        "TO_INCOME_L_CD"            varchar(2), \
        "TO_INCOME_M_CD"            varchar(2), \
        "CARRY_CANCEL"              varchar(1), \
        "REGISTERCD"                varchar(10), \
        "UPDATED"                   timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_CARRY_OVER_DAT add constraint PK_LEVY_CAR_OVE_D primary key (SCHOOLCD, SCHOOL_KIND, YEAR, SCHREGNO, INCOME_L_CD, INCOME_M_CD)
