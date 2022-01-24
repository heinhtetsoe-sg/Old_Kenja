-- $Id: fdd34221f6d5a579fe773f8870c49e816311a1ad $

DROP VIEW V_EDBOARD_COURSECODE_MST
CREATE VIEW V_EDBOARD_COURSECODE_MST \
    (EDBOARD_SCHOOLCD, YEAR, COURSECODE, COURSECODENAME, COURSECODEABBV1, COURSECODEABBV2, COURSECODEABBV3, UPDATED) AS \
SELECT \
    T1.EDBOARD_SCHOOLCD, \
    T1.YEAR, \
    T2.COURSECODE, \
    T2.COURSECODENAME, \
    T2.COURSECODEABBV1, \
    T2.COURSECODEABBV2, \
    T2.COURSECODEABBV3, \
    T2.UPDATED \
FROM \
    EDBOARD_COURSECODE_YDAT T1, \
    EDBOARD_COURSECODE_MST T2 \
WHERE \
    T1.EDBOARD_SCHOOLCD = T2.EDBOARD_SCHOOLCD AND \
    T1.COURSECODE       = T2.COURSECODE