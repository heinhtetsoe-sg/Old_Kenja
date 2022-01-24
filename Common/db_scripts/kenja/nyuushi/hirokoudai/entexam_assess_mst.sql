-- kanji=漢字
-- $Id: 7a2e4bd00ab77e183013887eaf5bae953f0eed1a $
-- テスト項目マスタ集計フラグ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ENTEXAM_ASSESS_MST

create table ENTEXAM_ASSESS_MST ( \
    ENTEXAMYEAR             varchar(4)  not null, \
    ASSESSCD                varchar(1)  not null, \
    ASSESSLEVEL             smallint    not null, \
    ASSESSMARK              varchar(6), \
    ASSESSLOW               decimal, \
    ASSESSHIGH              decimal, \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_ASSESS_MST add constraint PK_ENTEXM_ASSES_M \
      primary key (ENTEXAMYEAR, ASSESSCD, ASSESSLEVEL)
