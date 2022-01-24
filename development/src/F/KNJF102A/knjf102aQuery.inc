<?php

require_once('for_php7.php');

class knjf102aQuery extends Query
{

    //学年取得
    public function getGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス取得
    public function getHrClass($model, $div)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        if ($div == 1) {
            $query .= " GRADE = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        }
        $query .= "     GRADE IN (  SELECT ";
        $query .= "                     GRADE ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_GDAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                 AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                 AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "             ) ";

        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "      TR_CD2 = '".STAFFCD."' OR ";
            $query .= "      TR_CD3 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    public function getStudentList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || ' ' || T1.ATTENDNO || '番 ' || T2.NAME_SHOW AS LABEL, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //テーブル存在チェック
    public function checkTableExist()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    public function getSchName($model, $div)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    , SCHOOLCD ";
        }
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR    = '".CTRL_YEAR."' ";
            if ($div == 1) {
                $query .= "                     AND GRADE   = '".$model->field["GRADE_HR_CLASS"]."' ";
            } else {
                $query .= "                     AND GRADE   = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
            }
            $query .= "                     ) ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR    = '".CTRL_YEAR."' ";
            if ($div == 1) {
                $query .= "                     AND GRADE   = '".$model->field["GRADE_HR_CLASS"]."' ";
            } else {
                $query .= "                     AND GRADE   = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
            }
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";
    }
        return $query;
    }
}
