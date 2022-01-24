-- kanji=漢字
-- $Id: 4a4564fe65d6bb32af8904eaa43cb82d741c6561 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table RECORD_APPROVE_CREDIT_EXEC_DAT

create table RECORD_APPROVE_CREDIT_EXEC_DAT ( \
    CALC_DATE               DATE          NOT NULL, \
    CALC_TIME               TIME          NOT NULL, \
    YEAR                    VARCHAR(4)   , \
    SEMESTER                VARCHAR(1)   , \
    ATTEND_CALC_DATE        DATE         , \
    SELECT_HR_CLASS         VARCHAR(250) , \
    APPROVE_FLG             VARCHAR(1)   , \
    APPROVE_SEMESTER1_FLG   VARCHAR(1)   , \
    APPROVE_SEMESTER2_FLG   VARCHAR(1)   , \
    SET_PROV_FLG            VARCHAR(1)   , \
    SET_PROV_SEMESTER1_FLG  VARCHAR(1)   , \
    SET_PROV_SEMESTER2_FLG  VARCHAR(1)   , \
    REGISTERCD              VARCHAR(10)  , \
    UPDATED                 TIMESTAMP     DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table RECORD_APPROVE_CREDIT_EXEC_DAT add constraint PK_REC_APPR_C_E_D primary key (CALC_DATE,CALC_TIME)

