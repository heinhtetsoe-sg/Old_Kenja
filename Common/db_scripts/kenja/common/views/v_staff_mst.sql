-- $Id: aa4f2604819b9e47754d02c4b44fe2a516824be4 $

DROP VIEW V_STAFF_MST

CREATE VIEW V_STAFF_MST AS \
    SELECT \
        T1.YEAR, \ 
        T2.STAFFCD, \ 
        T2.STAFFNAME, \ 
        T2.STAFFNAME_SHOW, \ 
        T2.STAFFNAME_KANA, \ 
        T2.STAFFNAME_ENG, \ 
        T2.STAFFNAME_REAL, \ 
        T2.STAFFNAME_KANA_REAL, \ 
        CASE WHEN L1.FIELD1 IS NOT NULL THEN L1.FIELD1 ELSE T2.JOBCD END AS JOBCD, \ 
        CASE WHEN L2.FIELD1 IS NOT NULL THEN L2.FIELD1 ELSE T2.SECTIONCD END AS SECTIONCD, \ 
        CASE WHEN L3.FIELD1 IS NOT NULL THEN L3.FIELD1 ELSE T2.DUTYSHARECD END AS DUTYSHARECD, \
        CASE WHEN L4.FIELD1 IS NOT NULL THEN L4.FIELD1 ELSE T2.CHARGECLASSCD END AS CHARGECLASSCD, \
        L5.FIELD1 AS KATAGAKI1_F1, \
        L5.FIELD2 AS KATAGAKI1_F2, \
        L5.FIELD3 AS KATAGAKI1_F3, \
        L6.FIELD1 AS KATAGAKI2_F1, \
        L6.FIELD2 AS KATAGAKI2_F2, \
        L6.FIELD3 AS KATAGAKI2_F3, \
        L7.FIELD1 AS KATAGAKI3_F1, \
        L7.FIELD2 AS KATAGAKI3_F2, \
        L7.FIELD3 AS KATAGAKI3_F3, \
        L8.FIELD1 AS USECSS, \
        L8.FIELD2 AS USEFONTSIZE, \
        T2.STAFFSEX, \ 
        T2.STAFFBIRTHDAY, \ 
        T2.STAFFZIPCD, \ 
        T2.STAFFADDR1, \ 
        T2.STAFFADDR2, \ 
        T2.STAFFTELNO, \ 
        T2.STAFFFAXNO, \ 
        T2.STAFFE_MAIL, \ 
        T2.EDBOARD_STAFFCD, \ 
        T2.EDBOARD_TORIKOMI_FLG, \ 
        T2.UPDATED \ 
    FROM  STAFF_YDAT T1 \ 
          LEFT JOIN STAFF_DETAIL_MST L1 ON L1.YEAR = T1.YEAR \ 
                                       AND L1.STAFFCD = T1.STAFFCD \ 
                                       AND L1.STAFF_SEQ = '001' \ 
          LEFT JOIN STAFF_DETAIL_MST L2 ON L2.YEAR = T1.YEAR \ 
                                       AND L2.STAFFCD = T1.STAFFCD \ 
                                       AND L2.STAFF_SEQ = '002' \ 
          LEFT JOIN STAFF_DETAIL_MST L3 ON L3.YEAR = T1.YEAR \ 
                                       AND L3.STAFFCD = T1.STAFFCD \ 
                                       AND L3.STAFF_SEQ = '003' \ 
          LEFT JOIN STAFF_DETAIL_MST L4 ON L4.YEAR = T1.YEAR \ 
                                       AND L4.STAFFCD = T1.STAFFCD \ 
                                       AND L4.STAFF_SEQ = '004' \ 
          LEFT JOIN STAFF_DETAIL_MST L5 ON L5.YEAR = T1.YEAR \ 
                                       AND L5.STAFFCD = T1.STAFFCD \ 
                                       AND L5.STAFF_SEQ = '005' \ 
          LEFT JOIN STAFF_DETAIL_MST L6 ON L6.YEAR = T1.YEAR \ 
                                       AND L6.STAFFCD = T1.STAFFCD \ 
                                       AND L6.STAFF_SEQ = '006' \ 
          LEFT JOIN STAFF_DETAIL_MST L7 ON L7.YEAR = T1.YEAR \ 
                                       AND L7.STAFFCD = T1.STAFFCD \ 
                                       AND L7.STAFF_SEQ = '007' \ 
          LEFT JOIN STAFF_DETAIL_SEQ_MST L8 ON L8.STAFFCD = T1.STAFFCD \ 
                                       AND L8.STAFF_SEQ = '001' \ 
          ,STAFF_MST T2  \ 
    WHERE T1.STAFFCD = T2.STAFFCD
