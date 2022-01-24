<?php

require_once('for_php7.php');


class knjd102bQuery extends Query {

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER < '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //テスト項目取得
    function get_testkind($model)
    {
        $query  = "SELECT ";
        $query .= "    TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "    TESTITEMNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "    AND TESTKINDCD <> '99' ";
        $query .= "ORDER BY ";
        $query .= "    TESTKINDCD, ";
        $query .= "    TESTITEMCD ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT ";
            $query .= "    GRADE || HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR='".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER='".$model->field["SEMESTER"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT ";
            $query .= "    GRADE || HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR='".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER='".$model->field["SEMESTER"]."' ";
            $query .= "    AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒取得
    function getStudent($model)
    {
        $query  = "WITH SCHNO AS ( ";
        $query .= "    SELECT ";
        $query .= "        * ";
        $query .= "    FROM ";
        $query .= "        SCHREG_REGD_DAT  ";
        $query .= "    WHERE ";
        $query .= "        YEAR='".CTRL_YEAR."' ";
        $query .= "        AND SEMESTER='".$model->field["SEMESTER"]."' ";
        $query .= "        AND GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";

        $query .= "), BASE AS ( ";
        $query .= "    SELECT ";
        $query .= "        SCHREGNO ";
        $query .= "    FROM ";
        $query .= "        SCHREG_BASE_MST ";
        $query .= "    WHERE ";
        $query .= "        SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ";
        $query .= "        AND GRD_DIV IN ('2','3') AND GRD_DATE <= '".str_replace("/","-",$model->field["DATE"])."' ";
        $query .= "    GROUP BY SCHREGNO ";

        $query .= "), TRANS AS ( ";
        $query .= "    SELECT ";
        $query .= "        SCHREGNO ";
        $query .= "    FROM ";
        $query .= "        SCHREG_TRANSFER_DAT ";
        $query .= "    WHERE ";
        $query .= "        SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ";
        $query .= "        AND ((TRANSFERCD IN ('1','2','3') ";
        $query .= "        AND '".str_replace("/","-",$model->field["DATE"])."' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) OR  ";
        $query .= "            (TRANSFERCD IN ('4') AND '".str_replace("/","-",$model->field["DATE"])."' < TRANSFER_SDATE) )  ";
        $query .= "    GROUP BY SCHREGNO ";

        $query .= "), UNION_T AS ( ";
        $query .= "    SELECT ";
        $query .= "        SCHREGNO ";
        $query .= "    FROM ";
        $query .= "        BASE ";
        $query .= "    UNION ";
        $query .= "    SELECT ";
        $query .= "        SCHREGNO ";
        $query .= "    FROM ";
        $query .= "        TRANS) ";

        $query .= "SELECT ";
        $query .= "    CASE WHEN L2.SCHREGNO IS NULL OR L2.SCHREGNO = '' ";
        $query .= "         THEN T1.SCHREGNO || '　' || T1.ATTENDNO || '　' || L1.NAME_SHOW ";
        $query .= "         ELSE T1.SCHREGNO || '●' || T1.ATTENDNO || '●' || L1.NAME_SHOW END AS LABEL, ";
        $query .= "    T1.SCHREGNO AS VALUE ";
        $query .= "FROM ";
        $query .= "    SCHNO T1 ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN UNION_T L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "ORDER BY ";
        $query .= "    T1.ATTENDNO ";

        return $query;
    }

}
?>
