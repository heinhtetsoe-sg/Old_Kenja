-- kanji=漢字
-- $Id: expense_grp_hr_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--費目グループHRクラスデータ
DROP TABLE EXPENSE_GRP_HR_DAT

CREATE TABLE EXPENSE_GRP_HR_DAT \
( \
        "YEAR"            VARCHAR(4) NOT NULL, \
        "GRADE"           VARCHAR(2) NOT NULL, \
        "HR_CLASS"        VARCHAR(3) NOT NULL, \
        "EXPENSE_GRP_CD"  VARCHAR(4), \
        "REGISTERCD"      VARCHAR(8), \
        "UPDATED"         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EXPENSE_GRP_HR_DAT \
ADD CONSTRAINT PK_EXP_GRP_HR_DAT \
PRIMARY KEY \
(YEAR,GRADE,HR_CLASS)
