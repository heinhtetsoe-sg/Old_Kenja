-- kanji=漢字
-- $Id: v_entexam_applicantdesire_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP VIEW V_ENTEXAM_APPLICANTDESIRE_DAT
CREATE VIEW V_ENTEXAM_APPLICANTDESIRE_DAT \
   (ENTEXAMYEAR, \
    APPLICANTDIV, \
    TESTDIV, \
    EXAMNO, \
    DESIREDIV, \
    WISHNO, \
    COURSECD, \
    MAJORCD, \
    EXAMCOURSECD, \
    RECOM_KIND, \
    SHDIV \
    ) \
AS SELECT \
    T1.ENTEXAMYEAR, \
    T1.APPLICANTDIV, \
    T1.TESTDIV, \
    T1.EXAMNO, \
    T1.DESIREDIV, \
    L1.WISHNO, \
    L1.COURSECD, \
    L1.MAJORCD, \
    L1.EXAMCOURSECD, \
    T1.RECOM_KIND, \
    T1.SHDIV \
FROM \
    ENTEXAM_APPLICANTDESIRE_DAT T1 \
INNER JOIN \
    ENTEXAM_WISHDIV_MST L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR \
                          AND L1.APPLICANTDIV = T1.APPLICANTDIV \
                          AND L1.TESTDIV = T1.TESTDIV \
                          AND L1.DESIREDIV = T1.DESIREDIV 

