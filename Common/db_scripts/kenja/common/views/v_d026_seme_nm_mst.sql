-- $Id: bee5530890381eb063e016f5fb5ba6774a0cc4e1 $

drop view v_d026_seme_nm_mst

create view v_d026_seme_nm_mst \
    (YEAR, \
     SEMESTER, \
     SUBCLASSCD) \
as \
SELECT \
    YEAR, \
    '1' AS SEMESTER, \
    NAME1 AS SUBCLASSCD \
FROM \
    V_NAME_MST \
WHERE \
    NAMECD1 = 'D026' \
    AND ABBV1 = '1' \
UNION \
SELECT \
    YEAR, \
    '2' AS SEMESTER, \
    NAME1 AS SUBCLASSCD \
FROM \
    V_NAME_MST \
WHERE \
    NAMECD1 = 'D026' \
    AND ABBV2 = '1' \
UNION \
SELECT \
    YEAR, \
    '3' AS SEMESTER, \
    NAME1 AS SUBCLASSCD \
FROM \
    V_NAME_MST \
WHERE \
    NAMECD1 = 'D026' \
    AND ABBV3 = '1' \
UNION \
SELECT \
    YEAR, \
    '9' AS SEMESTER, \
    NAME1 AS SUBCLASSCD \
FROM \
    V_NAME_MST \
WHERE \
    NAMECD1 = 'D026' \
    AND NAMESPARE1 = '1'
