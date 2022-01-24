-- kanji=漢字
-- $Id: claim_print_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CLAIM_PRINT_HIST_DAT

create table CLAIM_PRINT_HIST_DAT \
(  \
        CLAIM_NO        varchar(8) not null, \
        SEQ             varchar(2) not null, \
        REISSUE_CNT     varchar(2) not null, \
        RE_CLAIM_CNT    varchar(2) not null, \
        SLIP_NO         varchar(8) not null, \
        APPLICANTNO     varchar(7) not null, \
        RE_CLAIM_NO     varchar(8), \
        CLAIM_DATE      date, \
        CLAIM_MONEY     integer, \
        TIMELIMIT_DAY   date, \
        FORM_NO         varchar(2), \
        REMARK          varchar(150), \
        CLAIM_NONE_FLG  varchar(1), \
        COMPLETE_FLG    varchar(1), \
        ABANDONMENT_FLG varchar(1), \
        PROCEDURE_DIV   varchar(1), \
        REGISTERCD      varchar(8), \
        UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CLAIM_PRINT_HIST_DAT  \
add constraint PK_CLAIM_PRINT_DAT  \
primary key  \
(CLAIM_NO, SEQ, REISSUE_CNT, RE_CLAIM_CNT)
