-- kanji=漢字
-- $Id: 0e8ef0105d19ca5d2689a9a40a609c4e048b9f2c $
-- 作成日: 2004/12/29 14:39:00 - JST
-- 作成者: tamura
--
-- CHAIR_DATテーブルにCOUNTFLGカラム(varchar(1))を追加する変換スクリプト。
-- 重要:次のファイルとの併用はできません。chair_dat.sql
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f convert_chair_dat.sql
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop   table CHAIR_DAT_OLD

rename table CHAIR_DAT     to CHAIR_DAT_OLD

create table CHAIR_DAT( \
       YEAR             VARCHAR(4)      NOT NULL, \
       SEMESTER         VARCHAR(1)      NOT NULL, \
       CHAIRCD          VARCHAR(7)      NOT NULL, \
       GROUPCD          VARCHAR(4), \
       CLASSCD          VARCHAR(2), \
       SCHOOL_KIND      VARCHAR(2), \
       CURRICULUM_CD    VARCHAR(2), \
       SUBCLASSCD       VARCHAR(6), \
       CHAIRNAME        VARCHAR(30), \
       CHAIRABBV        VARCHAR(30), \
       TAKESEMES        VARCHAR(1), \
       LESSONCNT        SMALLINT, \
       FRAMECNT         SMALLINT, \
       COUNTFLG         VARCHAR(1), \
       REGISTERCD       VARCHAR(8), \
       UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

insert into CHAIR_DAT \
  select \
    YEAR, \
    SEMESTER, \
    CHAIRCD, \
    GROUPCD, \
    LEFT(SUBCLASSCD, 2) AS CLASSCD, \
    'J' AS SCHOOL_KIND, \
    '2' AS CURRICULUM_CD, \
    SUBCLASSCD, \
    CHAIRNAME, \
    CAST(NULL AS VARCHAR(30)) AS CHAIRABBV, \
    TAKESEMES, \
    LESSONCNT, \
    FRAMECNT, \
    COUNTFLG, \
    REGISTERCD, \
    UPDATED \
  from CHAIR_DAT_OLD

alter table chair_dat add constraint pk_chair_dat primary key \
      (year,semester,chaircd)
