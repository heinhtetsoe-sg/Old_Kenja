<?php

require_once('for_php7.php');

class knja200aQuery extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //部クラブ一覧取得
    function getClub($model)
    {
        $query  = " SELECT ";
        $query .= "     CM.CLUBCD AS VALUE, ";
        $query .= "     CM.CLUBCD || ':' || CM.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_MST CM ";
        $query .= " WHERE ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 CLUB_YDAT CY ";
        $query .= "             WHERE ";
        $query .= "                 CY.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND CY.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND CY.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND CY.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND CY.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND CY.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND CY.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "                 AND CY.CLUBCD = CM.CLUBCD) ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND CM.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND CM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND CM.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND CM.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND CM.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND CM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CM.CLUBCD ";
        return $query;
    }

    //部クラブ一覧取得
    function getCommittee($model, $committeeFlg)
    {
        $query  = " SELECT ";
        $query .= "     CM.COMMITTEE_FLG || '-' || CM.COMMITTEECD AS VALUE, ";
        $query .= "     CM.COMMITTEE_FLG || '-' || CM.COMMITTEECD || ':' || CM.COMMITTEENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COMMITTEE_MST CM ";
        $query .= " WHERE ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 COMMITTEE_YDAT CY ";
        $query .= "             WHERE ";
        $query .= "                 CY.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND CY.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND CY.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND CY.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND CY.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND CY.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND CY.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "                 AND CY.COMMITTEE_FLG = CM.COMMITTEE_FLG ";
        $query .= "                 AND CY.COMMITTEECD = CM.COMMITTEECD) ";
        $query .= "     AND CM.COMMITTEE_FLG = '{$committeeFlg}' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND CM.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND CM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND CM.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND CM.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND CM.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND CM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //年組一覧取得
    function getHR($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //学期取得
    function getSemester($year)
    {
        $query = "SELECT semester AS VALUE, semestername AS LABEL FROM semester_mst where year='" .$year. "' and not semester='9'";

        return $query;
    }

}
?>
