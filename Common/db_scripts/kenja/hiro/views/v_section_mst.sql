
DROP VIEW V_SECTION_MST

CREATE VIEW V_SECTION_MST \
    (YEAR, \
     SECTIONCD,    \
     SECTIONNAME, \
     SECTIONABBV, \
     UPDATED) \
AS SELECT \
    T1.YEAR, \
    T2.SECTIONCD, \
    T2.SECTIONNAME, \
    T2.SECTIONABBV, \
    T2.UPDATED \
FROM     SECTION_YDAT T1, \
    SECTION_MST T2 \
WHERE    T1.SECTIONCD = T2.SECTIONCD

