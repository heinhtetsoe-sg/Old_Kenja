<?php

require_once('for_php7.php');

class knjd658bQuery extends Query {
    //学校名取得の取得(
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model) {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          TR_CD3 = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期
    function getSemesterMst() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER <> '9' ";

        return $query;
    }

    //テスト種別取得
    function getTestKind($model) {
        $query  = " WITH MAIN_T(LABEL, VALUE, KINDCD, ITEMCD) AS ( ";
        $query .= " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || '　' || TESTITEMNAME, ";
        $query .= "     TESTKINDCD || TESTITEMCD, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     VALUE <> '9900' ";
        $query .= " ORDER BY ";
        $query .= "     KINDCD, ";
        $query .= "     ITEMCD ";

        return $query;
    }

    //対象生徒
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO AS VALUE, ";
        $query .= "     T2.SCHREGNO || '　' || T2.ATTENDNO || '番　' || T1.NAME_SHOW AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T2.GRADE || T2.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //模試名コンボ
    function getMock2Dat() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.MOCKCD AS VALUE, ";
        $query .= "     T1.MOCKCD || '　' || L1.MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK2_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     MOCK_MST L1 ON L1.MOCKCD = T1.MOCKCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }
}
?>
