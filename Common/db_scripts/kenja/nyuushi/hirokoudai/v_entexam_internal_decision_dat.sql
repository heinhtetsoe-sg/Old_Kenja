-- $Id: b051a3646cdbbb5cc883a1768ed24799edbcdb64 $

DROP VIEW V_ENTEXAM_INTERNAL_DECISION_DAT

CREATE VIEW V_ENTEXAM_INTERNAL_DECISION_DAT ( \
    ENTEXAMYEAR, \
    DECISION_CD, \
    DECISION_NAME \
) AS \
SELECT \
    YDAT.ENTEXAMYEAR, \
    YDAT.DECISION_CD, \
    MST.DECISION_NAME \
FROM \
    ENTEXAM_INTERNAL_DECISION_YDAT YDAT \
    LEFT JOIN ENTEXAM_INTERNAL_DECISION_MST MST ON YDAT.DECISION_CD = MST.DECISION_CD