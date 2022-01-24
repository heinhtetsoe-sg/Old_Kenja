<?php

require_once('for_php7.php');

class knjx_toolQuery extends Query {

    //職員情報取得
    function getUserData($model)
    {
        /*
        $query .= " SELECT ";
        $query .= "     DISTINCT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T1.USERID, ";
        $query .= "     LTRIM(T3.TRGTGRADE,'0') AS TRGTGRADE, ";
        $query .= "     T4.SCHOOL_KIND, ";
        $query .= "     T4.CLASSCD ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T0 ";
        $query .= "     LEFT JOIN USER_MST T1 ON T0.STAFFCD = T1.STAFFCD ";
        $query .= "     LEFT JOIN CHAIR_STF_DAT T2 on T0.STAFFCD = T2.STAFFCD ";
        $query .= "     LEFT JOIN CHAIR_CLS_DAT T3 on T2.YEAR = T3.YEAR and T2.SEMESTER = T3.SEMESTER and T2.CHAIRCD = T3.CHAIRCD ";
        $query .= "     LEFT JOIN CHAIR_DAT     T4 on T2.YEAR = T4.YEAR and T2.SEMESTER = T4.SEMESTER and T2.CHAIRCD = T4.CHAIRCD and T1.SCHOOL_KIND = T4.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T2.STAFFCD IS NOT NULL AND T1.STAFFCD IS NOT NULL AND T3.TRGTGRADE IS NOT NULL AND T4.SCHOOL_KIND IS NOT NULL AND (T1.INVALID_FLG = '0' OR INVALID_FLG IS NULL)";
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     USERID, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     TRGTGRADE, ";
        $query .= "     CLASSCD ";
        */
        
        $query .= " SELECT DISTINCT ";
        $query .= "     T5.YEAR, ";
        $query .= "     T1.USERID, ";
        $query .= "     LTRIM(T3.TRGTGRADE,'0') AS TRGTGRADE, ";
        $query .= "     T4.SCHOOL_KIND, ";
        $query .= "     T4.CLASSCD, ";
        $query .= "     T6.GROUPCD ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T0 ";
        $query .= "     LEFT JOIN STAFF_YDAT T5 on T0.STAFFCD = T5.STAFFCD ";
        $query .= "     LEFT JOIN USER_MST T1 ON T0.STAFFCD = T1.STAFFCD  ";
        $query .= "     LEFT JOIN CHAIR_STF_DAT T2 on T0.STAFFCD = T2.STAFFCD and T5.YEAR = T2.YEAR ";
        $query .= "     LEFT JOIN CHAIR_CLS_DAT T3 on T2.YEAR = T3.YEAR and T2.SEMESTER = T3.SEMESTER and T2.CHAIRCD = T3.CHAIRCD  ";
        $query .= "     LEFT JOIN CHAIR_DAT T4 on T2.YEAR = T4.YEAR and T2.SEMESTER = T4.SEMESTER and T2.CHAIRCD = T4.CHAIRCD and T1.SCHOOL_KIND = T4.SCHOOL_KIND ";
        $query .= "     LEFT JOIN USERGROUP_DAT T6 on T1.STAFFCD = T6.STAFFCD and T1.SCHOOLCD = T6.SCHOOLCD and T1.SCHOOL_KIND = T6.SCHOOL_KIND and T5.YEAR = T6.YEAR and T6.GROUPCD in('0003','0004','0012','0021') ";
        $query .= " WHERE ";
        $query .= "     T5.YEAR IS NOT NULL AND ";
        $query .= "     T5.STAFFCD IS NOT NULL AND ";
        $query .= "     T1.USERID IS NOT NULL AND ";
        $query .= "     (T3.TRGTGRADE IS NOT NULL OR T4.SCHOOL_KIND IS NOT NULL OR T4.CLASSCD IS NOT NULL OR T6.GROUPCD IS NOT NULL) AND ";
        $query .= "     (T1.INVALID_FLG = '0' OR INVALID_FLG IS NULL) ";
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     USERID, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     TRGTGRADE, ";
        $query .= "     CLASSCD ";
        
        
        return $query;
    }


}
?>
