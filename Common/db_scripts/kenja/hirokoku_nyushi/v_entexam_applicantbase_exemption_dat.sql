-- kanji=����
-- $Id: v_entexam_applicantbase_exemption_dat.sql 57740 2017-12-27 02:38:23Z maeshiro $

DROP VIEW V_ENTEXAM_APPLICANTBASE_EXEMPTION_DAT

CREATE VIEW V_ENTEXAM_APPLICANTBASE_EXEMPTION_DAT \
   (ENTEXAMYEAR, \
    APPLICANTDIV, \
    TESTDIV, \
    EXAMNO, \
    EXEMPTION_CD \
    ) \
AS SELECT \
    T1.ENTEXAMYEAR, \
    T1.APPLICANTDIV, \
    T1.TESTDIV, \
    T1.EXAMNO, \
    CASE WHEN T1.APPLICANTDIV = '2' AND T1.TESTDIV = '6' \
         THEN \
         CASE WHEN T1.JUDGE_KIND = '1' THEN '06' \
              WHEN T1.JUDGE_KIND = '2' THEN '07' \
              WHEN T1.JUDGE_KIND = '3' THEN '08' \
              WHEN T1.JUDGE_KIND = '4' THEN '26' \
              WHEN T1.JUDGE_KIND = '5' THEN '28' \
              WHEN L1.REMARK1 IS NOT NULL THEN '09' \
              WHEN L2.REMARK2 IS NOT NULL THEN '10' \
              ELSE '11' \
         END \
         ELSE \
         CASE WHEN T1.JUDGE_KIND = '1' THEN '01' \
              WHEN T1.JUDGE_KIND = '2' THEN '02' \
              WHEN T1.JUDGE_KIND = '3' THEN '03' \
              WHEN T1.JUDGE_KIND = '4' THEN (CASE T1.APPLICANTDIV WHEN '1' THEN '01' WHEN '2' THEN '21' END) \
              WHEN T1.JUDGE_KIND = '5' THEN '23' \
              WHEN L1.REMARK1 IS NOT NULL THEN '04' \
              WHEN L2.REMARK2 IS NOT NULL THEN '05' \
              ELSE '00' \
         END \
    END AS EXEMPTION_CD \
FROM \
    ENTEXAM_APPLICANTBASE_DAT T1 \
    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 \
         ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR \
        AND L1.EXAMNO = T1.EXAMNO \
        AND L1.SEQ = '015' \
    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L2 \
         ON L2.ENTEXAMYEAR = T1.ENTEXAMYEAR \
        AND L2.EXAMNO = T1.EXAMNO \
        AND L2.SEQ = '014'
