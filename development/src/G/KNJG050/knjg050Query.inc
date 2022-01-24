<?php

require_once('for_php7.php');

class knjg050Query extends Query
{

    //学校区分取得
    public function getSchoolDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOLDIV ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";

        return $query;
    }

    //名称マスタ"A023"NAMESPARE2、NAMESPARE3登録確認
    public function getNameSpareCnt()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(NAMESPARE2) AS SPARE2, ";
        $query .= "     COUNT(NAMESPARE3) AS SPARE3 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";

        return $query;
    }

    //学年コンボ（A023登録無し）
    public function getGrade1($model, $schoolDiv)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        if ($schoolDiv == 1) {
            $query .= "     AND INT(GRADE) >= 3 ";
        } else {
            $query .= "     AND INT(GRADE) = 3 ";
        }
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

    //学年コンボ（A023登録有）
    public function getGrade2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     NAME_MST N1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$model->field["YEAR"]}' AND ";
        $query .= "     N1.NAMECD1  = 'A023' AND ";
        $query .= "     T1.GRADE BETWEEN N1.NAMESPARE2 AND N1.NAMESPARE3 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組コンボ用（A023登録無し）
    public function getHR1($model, $schoolDiv)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$model->field["YEAR"]}' ";
        $query .= "     AND SEMESTER = '{$model->field["GAKKI"]}' ";
        if ($schoolDiv == 1) {
            $query .= "     AND INT(T1.GRADE) >= 3 ";
        } else {
            $query .= "     AND INT(T1.GRADE) = 3 ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組コンボ用（A023登録有）
    public function getHR2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.HR_NAME AS LABEL, ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     ,NAME_MST N1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$model->field["YEAR"]}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["GAKKI"]}' AND ";
        $query .= "     N1.NAMECD1  = 'A023' AND ";
        $query .= "     T1.GRADE BETWEEN N1.NAMESPARE2 AND N1.NAMESPARE3 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（リストtoリスト）
    public function getAuth($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->field["YEAR"]}' ";
        $query .= "     AND SEMESTER = '{$model->field["GAKKI"]}' ";
        $query .= "     AND GRADE    = '{$model->field["CMBCLASS"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得（リストtoリスト）
    public function getstudent($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ATTENDNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '番　' || T2.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$model->field["YEAR"]}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["GAKKI"]}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '{$model->field["CMBCLASS"]}' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO ";
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
    public function getSchName($model)
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
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE   = '".substr($model->field["CMBCLASS"], 0, 2)."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";
    }
        return $query;
    }
}
