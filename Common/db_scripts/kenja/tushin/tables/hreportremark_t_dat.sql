-- kanji=漢字
-- $Id: hreportremark_t_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback


DROP TABLE HREPORTREMARK_T_DAT

CREATE TABLE HREPORTREMARK_T_DAT \
    (REMARKID           VARCHAR(1) NOT NULL, \
     REMARK             VARCHAR(210), \
     REGISTERCD         VARCHAR(8), \
     UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE HREPORTREMARK_T_DAT ADD CONSTRAINT PK_HREP_T_DAT PRIMARY KEY (REMARKID)
