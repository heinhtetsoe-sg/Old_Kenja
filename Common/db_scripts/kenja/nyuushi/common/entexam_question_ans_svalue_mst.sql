-- kanji=漢字
-- $Id: ad771b6a7c6663dea3745b0ae4f1810dd66c3e1a $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ENTEXAM_QUESTION_ANS_SVALUE_MST

create table ENTEXAM_QUESTION_ANS_SVALUE_MST ( \
    VALUE                   varchar(3)  not null, \
    ANSWER1                 varchar(2), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_QUESTION_ANS_SVALUE_MST add constraint PK_ENT_Q_ANS_SV_M \
      primary key (VALUE)
