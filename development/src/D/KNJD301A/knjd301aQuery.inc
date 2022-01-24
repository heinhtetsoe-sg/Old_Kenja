<?php

require_once('for_php7.php');


class knjd301aQuery extends Query {

    //学期取得
    function getSemester($model, $flg)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER AS VALUE, ";
        $query .= "     T1.SEMESTERNAME AS LABEL ";
        if ($flg) {
            $query .= "     ,T2.SEMESTERDIV ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        if ($flg) {
            $query .= "     LEFT JOIN SCHOOL_MST T2 ON T2.YEAR = T1.YEAR ";
            if ($model->Properties["useSchool_KindField"] == '1' && SCHOOLKIND != '') {
                $query .= "     AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($flg) {
            $query .= "     AND T1.SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     N1.ABBV1 as LABEL, ";
        $query .= "     N1.NAME1 as VALUE, ";
        $query .= "     N1.NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST N1 ";
        $query .= " WHERE ";
        $query .= "     N1.NAMECD1='A023' ";
        $query .= " ORDER BY ";
        $query .= "     N1.NAMECD2 ";
        return $query;
    }

    //テスト種別
    function getTest($model, $grade) {
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW') {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SEMESTER, ";
            $query .= "     T1.TESTKINDCD || T1.TESTITEMCD AS VALUE, ";
            $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || ':' || T1.TESTITEMNAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     TESTITEM_MST_COUNTFLG_NEW T1 ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SEMESTER, ";
            $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
            $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || L1.TESTITEMNAME AS LABEL ";
            $query .= " FROM ";
            if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
                $query .= "     ADMIN_CONTROL_GCM_SDIV_DAT T1 ";
                $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV L1 ON T1.YEAR = L1.YEAR ";
            } else {
                $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
                $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ";
            }
            $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
            $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
            $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
            $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
            if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
                $query .= "                                                AND T1.GRADE = L1.GRADE ";
                $query .= "                                                AND T1.COURSECD = L1.COURSECD ";
                $query .= "                                                AND T1.MAJORCD = L1.MAJORCD ";
            }
            $query .= " WHERE ";
            $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
            $query .= "     AND T1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
            $query .= "     AND T1.CLASSCD      = '00' ";
            $query .= "     AND T1.CURRICULUM_CD  = '00' ";
            $query .= "     AND T1.SUBCLASSCD  = '000000' ";
            if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
                $query .= "     AND T1.GRADE = '00' ";
                $query .= "     AND T1.COURSECD || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }
    
    //クラブ取得
    function getClub($model)
    {
        $query .= " SELECT ";
        $query .= "     CLUBCD AS VALUE, ";
        $query .= "     CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND='".$model->field['SCHOOL_KIND']."' ";
        
        return $query;
    }

}
?>
