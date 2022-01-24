-- kanji=漢字
-- $Id: 5c23d406625cbad583be5b9fc95b42b277133d8b $
-- テスト項目マスタ集計フラグ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ENTEXAM_ASSESS_HDAT

create table ENTEXAM_ASSESS_HDAT ( \
    ENTEXAMYEAR             varchar(4)  not null, \
    ASSESSCD                varchar(1)  not null, \
    ASSESSMEMO              varchar(30), \
    ASSESSLEVELCNT          varchar(2), \
    MODIFY_FLG              varchar(1), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_ASSESS_HDAT add constraint PK_ENTEXM_ASSES_H \
      primary key (ENTEXAMYEAR, ASSESSCD)
