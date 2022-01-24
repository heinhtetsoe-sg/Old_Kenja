<?php

require_once('for_php7.php');


class knjj100Query extends Query
{

    public function getHr_Class_alp($model, $year, $semester, $auth_user, $staffcd, $auth)
    {
        $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T1.YEAR='" .$year ."' ";
        $query .= "AND SEMESTER='".$semester ."' ";
        if ($auth == "1") {
        //参照・更新可（制限付き）
            if ($auth_user == DEF_REFER_RESTRICT || $auth_user == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (   TR_CD1    = '{$staffcd}' ";
                $query .= "          OR TR_CD2    = '{$staffcd}' ";
                $query .= "          OR TR_CD3    = '{$staffcd}' ";
                $query .= "          OR SUBTR_CD1 = '{$staffcd}' ";
                $query .= "          OR SUBTR_CD2 = '{$staffcd}' ";
                $query .= "          OR SUBTR_CD3 = '{$staffcd}' ";
                $query .= "          OR T1.GRADE IN ";
                $query .= "              (SELECT ";
                $query .= "                   FIELD2 ";
                $query .= "               FROM ";
                $query .= "                   STAFF_DETAIL_MST ST ";
                $query .= "               WHERE ";
                $query .= "                   T1.YEAR = ST.YEAR ";
                $query .= "                   AND ST.STAFFCD = '{$staffcd}' ";
                $query .= "                   AND ST.STAFF_SEQ IN ('005', '006', '007') ";
                $query .= "                   AND ST.FIELD1 = '0200') ";
                $query .= "          ) ";
            }
        }

        return $query;
    }

    //学期コンボ取得
    public function getJ004()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'J004' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ＣＳＶ作成
    public function selectCsvQuery($model)
    {

        $query .= " WITH SCHINFO AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".implode("','", explode(',', $model->selectdata))."') ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.HR_NAMEABBV, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.COMMITTEE_FLG, ";
        $query .= "     T1.COMMITTEECD, ";
        $query .= "     C1.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME, ";
        $query .= "     T1.EXECUTIVECD ";
        $query .= " FROM ";
        $query .= "     SCHINFO T2 ";
        $query .= "     LEFT JOIN SCHREG_COMMITTEE_HIST_DAT T1 ON T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "          AND T1.GRADE = T2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      AND T1.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "      AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "      AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "      AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN COMMITTEE_MST C1 ON T1.COMMITTEE_FLG = C1.COMMITTEE_FLG AND T1.COMMITTEECD = C1.COMMITTEECD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND C1.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            if ($model->selectSchoolKind) {
                $query .= " AND C1.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= " AND C1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND C1.SCHOOLCD = T1.SCHOOLCD ";
            $query .= " AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= " AND C1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND C1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->field["J004"] != "ALL") {
            $query .= "     AND T1.SEMESTER = '{$model->field["J004"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMst($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }
}
