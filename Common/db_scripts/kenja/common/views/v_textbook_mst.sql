-- $Id: d9b0a9b1ce6b3adce79cdd90a8a4431a534a00e4 $

DROP VIEW V_TEXTBOOK_MST

CREATE VIEW V_TEXTBOOK_MST \
    (YEAR, \
     TEXTBOOKCD, \
     TEXTBOOKDIV, \
     TEXTBOOKNAME, \
     TEXTBOOKABBV, \
     TEXTBOOKMK, \
     TEXTBOOKMS, \
     TEXTBOOKWRITINGNAME, \
     TEXTBOOKPRICE, \
     TEXTBOOKUNITPRICE, \
     ISSUECOMPANYCD, \
     ISSUECOMPANY, \
     CONTRACTORNAME, \
     REMARK, \
     UPDATED) \
AS SELECT \
    T1.YEAR, \
    T2.textbookcd, \
    T2.textbookdiv, \
    T2.textbookname, \
    T2.textbookabbv, \
    T2.textbookmk, \
    T2.textbookms, \
    T2.textbookwritingname, \
    T2.textbookprice, \
    T2.textbookunitprice, \
    T2.issuecompanycd, \
    T2.ISSUECOMPANY, \
    T2.CONTRACTORNAME, \
    T2.REMARK, \
    T2.updated \
FROM     TEXTBOOK_YDAT T1, \
    TEXTBOOK_MST T2 \
WHERE    T1.TEXTBOOKCD = T2.TEXTBOOKCD

