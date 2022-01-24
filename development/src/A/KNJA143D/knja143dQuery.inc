<?php

require_once('for_php7.php');

class knja143dQuery extends Query
{

    //学期マスタ
    public function getSemeMst($year, $semester)
    {
        $query  = "SELECT * FROM SEMESTER_MST WHERE YEAR='".$year."' AND SEMESTER='".$semester."' ";

        return $query;
    }

    //Z010
    public function getZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "       NAMECD1 = 'Z010' ";
        $query .= "   AND NAMECD2 = '00' ";

        return $query;
    }

    //校種取得
    public function getSchkind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //年組取得（権限チェック）
    public function getAuth($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " AND T2.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";

        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (T1.TR_CD1, T1.TR_CD2, T1.TR_CD3)   ";
        }

        return $query;
    }

    //生徒取得
    public function getSchno($model, $year, $semester)
    {
        $query  = "SELECT T1.SCHREGNO, ";
        $query .= "       L1.HR_NAME || '　' || T1.ATTENDNO || '番' || '　' || T2.NAME AS NAME ";
        $query .= "FROM   SCHREG_REGD_DAT T1 ";
        $query .= "       LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "            AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "            AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS, ";
        $query .= "       SCHREG_BASE_MST T2 ";
        $query .= "WHERE  T1.YEAR       = '".$year."' AND ";
        $query .= "       T1.SEMESTER   = '".$semester."' AND ";
        $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "       T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= "ORDER BY T1.ATTENDNO ";

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
    public function getSchName($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE = SUBSTR('".$model->field["GRADE_HR_CLASS"]."',1,2) ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
