<?php

require_once('for_php7.php');


class knjd102hQuery extends Query {

    //対象外の生徒取得
    function getSchnoIdou($model, $semester)
    {
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER = '".$semester."' AND ";
        $query .= "            GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ), ";
        $query .= " BASE AS (  ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST  ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ";
        $query .= "         GRD_DIV IN ('2','3') AND ";
        $query .= "         GRD_DATE <= '".str_replace("/","-",$model->field["DATE"])."' ";
        $query .= "     GROUP BY SCHREGNO ), ";
        $query .= " TRANS AS (  ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_TRANSFER_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND  ";
        $query .= "        ((TRANSFERCD IN ('1','2','3') AND  ";
        $query .= "          '".str_replace("/","-",$model->field["DATE"])."' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) OR  ";
        $query .= "         (TRANSFERCD IN ('4') AND '".str_replace("/","-",$model->field["DATE"])."' < TRANSFER_SDATE) )  ";
        $query .= "     GROUP BY SCHREGNO ) ";

        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     BASE ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     TRANS  ";

        return $query;
    }


    //年組取得（権限チェック）
    function getAuth($model, $year, $semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '" .$year ."' AND ";
            $query .= "     T1.SEMESTER = '".$semester ."' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    }
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
            }
       }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    }
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '" .$year ."' AND ";
            $query .= "     T1.SEMESTER = '".$semester ."' AND ";
            $query .= "     (T1.TR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "         T1.TR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "         T1.TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //テスト項目取得
    function get_testkind($year, $semester, $model)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .=       $model->testTable;
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$year. "' ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     AND SEMESTER = '".$semester ."' ";
            $query .= "     AND TESTITEMCD <> '00' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
