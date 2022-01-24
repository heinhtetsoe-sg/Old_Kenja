-- kanji=漢字
-- $Id: f38c5cb337b2761801e5b295afc30be601b84c4b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table ENTEXAM_RECEPT_DAT_OLD
create table ENTEXAM_RECEPT_DAT_OLD like ENTEXAM_RECEPT_DAT
insert into ENTEXAM_RECEPT_DAT_OLD select * from ENTEXAM_RECEPT_DAT

DROP TABLE ENTEXAM_RECEPT_DAT
CREATE TABLE ENTEXAM_RECEPT_DAT( \
    ENTEXAMYEAR               varchar(4)    not null, \
    APPLICANTDIV              varchar(1)    not null, \
    TESTDIV                   varchar(2)    not null, \
    EXAM_TYPE                 varchar(2)    not null, \
    RECEPTNO                  varchar(20)   not null, \
    EXAMNO                    varchar(10)   not null, \
    ATTEND_ALL_FLG            varchar(1), \
    TOTAL2                    smallint, \
    AVARAGE2                  decimal(4,1), \
    TOTAL_RANK2               smallint, \
    DIV_RANK2                 smallint, \
    TOTAL4                    smallint, \
    AVARAGE4                  decimal(4,1), \
    TOTAL_RANK4               smallint, \
    DIV_RANK4                 smallint, \
    TOTAL1                    smallint, \
    AVARAGE1                  decimal(4,1), \
    TOTAL_RANK1               smallint, \
    DIV_RANK1                 smallint, \
    TOTAL3                    smallint, \
    AVARAGE3                  decimal(4,1), \
    TOTAL_RANK3               smallint, \
    DIV_RANK3                 smallint, \
    JUDGE_DEVIATION           decimal(4,1), \
    JUDGE_DEVIATION_DIV       varchar(1), \
    JUDGE_DEVIATION_RANK      smallint, \
    LINK_JUDGE_DEVIATION      decimal(4,1), \
    LINK_JUDGE_DEVIATION_DIV  varchar(1), \
    LINK_JUDGE_DEVIATION_RANK smallint, \
    JUDGE_EXAM_TYPE           varchar(1), \
    JUDGEDIV                  varchar(1), \
    HONORDIV                  varchar(1), \
    ADJOURNMENTDIV            varchar(1), \
    JUDGELINE                 varchar(1), \
    PROCEDUREDIV1             varchar(1), \
    PROCEDUREDATE1            date, \
    DISTINCT_ID               varchar(3), \
    TEST_NAME_ABBV            varchar(100), \
    REGISTERCD                varchar(10), \
    UPDATED                   timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_RECEPT_DAT ADD CONSTRAINT PK_ENTEXAM_RCPT PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO)

insert into ENTEXAM_RECEPT_DAT \
select \
        ENTEXAMYEAR, \
        APPLICANTDIV, \
        '0' || TESTDIV, \
        EXAM_TYPE, \
        RECEPTNO, \
        EXAMNO, \
        ATTEND_ALL_FLG, \
        TOTAL2, \
        AVARAGE2, \
        TOTAL_RANK2, \
        DIV_RANK2, \
        TOTAL4, \
        AVARAGE4, \
        TOTAL_RANK4, \
        DIV_RANK4, \
        TOTAL1, \
        AVARAGE1, \
        TOTAL_RANK1, \
        DIV_RANK1, \
        TOTAL3, \
        AVARAGE3, \
        TOTAL_RANK3, \
        DIV_RANK3, \
        JUDGE_DEVIATION, \
        JUDGE_DEVIATION_DIV, \
        JUDGE_DEVIATION_RANK, \
        LINK_JUDGE_DEVIATION, \
        LINK_JUDGE_DEVIATION_DIV, \
        LINK_JUDGE_DEVIATION_RANK, \
        JUDGE_EXAM_TYPE, \
        JUDGEDIV, \
        HONORDIV, \
        ADJOURNMENTDIV, \
        JUDGELINE, \
        PROCEDUREDIV1, \
        PROCEDUREDATE1, \
        DISTINCT_ID, \
        TEST_NAME_ABBV, \
        REGISTERCD, \
        UPDATED \
from ENTEXAM_RECEPT_DAT_OLD
