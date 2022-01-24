<?php

require_once('for_php7.php');

class knjd678Query extends Query {
    //学期
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "                                   AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND (T1.SCHOOL_KIND = 'H' OR T1.SCHOOL_KIND = 'J') ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND ((T2.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD3 = '".STAFFCD."') ";
            $query .= "       OR (T1.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //学校校種を取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND GRADE = '".$model->field["GRADE"]."' ";
        
        return $query;
    }


    //テスト種別
    function getTestitem($model) {
        $query  = " SELECT ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || L1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ";
        $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
        $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
        $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " AND (T1.SEMESTER = '9' AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' AND T1.SCORE_DIV = '09' ";
        $query .= "   OR T1.SEMESTER <> '9' AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' AND T1.SCORE_DIV = '08') ";
        $query .= " AND T1.CLASSCD = '00' ";
        $query .= " AND T1.SCHOOL_KIND = '".$model->schoolkind."' ";
        $query .= " AND T1.CURRICULUM_CD = '00' ";
        $query .= " AND T1.SUBCLASSCD = '000000' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コースグループを取得する
    function getCourseGroup($model) {
        $query  = " WITH SCHREG_DATA AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= " AND T1.GRADE = '".$model->field["GRADE"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND ((T2.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD3 = '".STAFFCD."') ";
            $query .= "       OR (T1.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         ) ";
        }
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GROUP_CD AS VALUE, ";
        $query .= "     T1.GROUP_CD || ':' || L2.GROUP_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_DATA L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                              AND L1.COURSECD = T1.COURSECD ";
        $query .= "                              AND L1.MAJORCD = T1.MAJORCD ";
        $query .= "                              AND L1.COURSECODE = T1.COURSECODE ";
        $query .= "                              AND L1.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_HDAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                      AND L2.GRADE = T1.GRADE ";
        $query .= "                                      AND L2.GROUP_CD = T1.GROUP_CD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }
    
    //年組取得
    function getHrclass($model) {
        $query  = " WITH COURSE_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " AND GROUP_CD = '".$model->field["GROUP_CD"]."' ";
        $query .= " ), ALL_HR (VALUE, LABEL) AS ( ";
        $query .= "     VALUES('00000', '-全て-') ";
        $query .= " ) ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
        } else {
            $query .= " SELECT ";
            $query .= "     VALUE, ";
            $query .= "     LABEL ";
            $query .= " FROM ";
            $query .= "     ALL_HR T1 ";
            $query .= " UNION ALL ";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     L2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN COURSE_DATA L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                             AND L1.COURSECD = T1.COURSECD ";
        $query .= "                             AND L1.MAJORCD = T1.MAJORCD ";
        $query .= "                             AND L1.COURSECODE = T1.COURSECODE ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                  AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "                                  AND L2.GRADE = T1.GRADE ";
        $query .= "                                  AND L2.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= " AND T1.GRADE = '".$model->field["GRADE"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND ((L2.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          L2.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          L2.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "          L2.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          L2.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          L2.SUBTR_CD3 = '".STAFFCD."') ";
            $query .= "       OR (T1.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }
}
?>
