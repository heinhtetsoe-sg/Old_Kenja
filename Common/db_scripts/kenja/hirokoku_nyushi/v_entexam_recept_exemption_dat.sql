-- kanji=����
-- $Id: v_entexam_recept_exemption_dat.sql 72177 2020-02-04 09:08:37Z maeshiro $

DROP VIEW V_ENTEXAM_RECEPT_EXEMPTION_DAT

CREATE VIEW V_ENTEXAM_RECEPT_EXEMPTION_DAT \
   (ENTEXAMYEAR, \
    APPLICANTDIV, \
    TESTDIV, \
    RECEPTNO, \
    EXAMNO, \
    EXEMPTION_CD \
    ) \
AS SELECT \
    T1.ENTEXAMYEAR, \
    T1.APPLICANTDIV, \
    T1.TESTDIV, \
    T1.RECEPTNO, \
    T1.EXAMNO, \
    CASE WHEN T1.APPLICANTDIV = '2' AND T1.TESTDIV = '6' \
         THEN \
         CASE WHEN T1.HONORDIV = '1' THEN '06' \
              WHEN T1.HONORDIV = '2' THEN '07' \
              WHEN T1.HONORDIV = '3' THEN '08' \
              WHEN T1.HONORDIV = '4' THEN '26' \
              WHEN T1.HONORDIV = '5' THEN '28' \
              ELSE '11' \
         END \
         ELSE \
         CASE WHEN T1.HONORDIV = '1' THEN '01' \
              WHEN T1.HONORDIV = '2' THEN '02' \
              WHEN T1.HONORDIV = '3' THEN '03' \
              WHEN T1.HONORDIV = '4' THEN (CASE T1.APPLICANTDIV WHEN '1' THEN '01' WHEN '2' THEN '21' END) \
              WHEN T1.HONORDIV = '5' THEN '23' \
              ELSE '00' \
         END \
    END AS EXEMPTION_CD, \
    CASE WHEN T1.APPLICANTDIV = '2' AND T1.TESTDIV = '6' \
         THEN \
         CASE \
              WHEN L1.REMARK1 IS NOT NULL THEN '09' \
              WHEN L2.REMARK2 IS NOT NULL THEN '10' \
              ELSE '11' \
         END \
         ELSE \
         CASE \
              WHEN L1.REMARK1 IS NOT NULL THEN '04' \
              WHEN L2.REMARK2 IS NOT NULL THEN '05' \
              ELSE '00' \
         END \
    END AS EXEMPTION_CD2 \
FROM \
    ENTEXAM_RECEPT_DAT T1 \
    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 \
         ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR \
        AND L1.EXAMNO = T1.EXAMNO \
        AND L1.SEQ = '015' \
    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L2 \
         ON L2.ENTEXAMYEAR = T1.ENTEXAMYEAR \
        AND L2.EXAMNO = T1.EXAMNO \
        AND L2.SEQ = '014'
