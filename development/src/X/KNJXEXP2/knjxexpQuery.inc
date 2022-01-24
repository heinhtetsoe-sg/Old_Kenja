<?php

require_once('for_php7.php');

class knjxexpQuery extends Query
{
    //学年と組
    public function GetHr_Class($model)
    {
        $query = "";
        $query .= "SELECT ";
        $query .= "  T1.GRADE, ";
        $query .= "  T1.HR_CLASS, ";
        $query .= "  T1.HR_NAME, ";
        $query .= "  T1.HR_NAMEABBV ";
//      $query .= "  T2.STAFFCD, ";
//      $query .= "  T2.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "  GRD_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
                $query .= "           AND T1.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
            $query .= "           AND T1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
//      $query .= "  GRD_REGD_HDAT T1,";
//      $query .= "  STAFF_MST T2 ";
        $query .= "WHERE ";
        if ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT) {
            $query .= " (T1.TR_CD1 = '". STAFFCD ."' OR";
            $query .= "  T1.TR_CD2 = '". STAFFCD ."' OR";
            $query .= "  T1.TR_CD3 = '". STAFFCD ."' OR";
            $query .= "  T1.SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "  T1.SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "  T1.SUBTR_CD3 = '". STAFFCD ."') AND";
        }
//      $query .= "  T1.TR_CD1 = T2.STAFFCD AND ";
        $query .= "  T1.YEAR = '" .$model->year ."' AND ";
        $query .= "  T1.SEMESTER = '" .$model->seme ."' ";
        $query .= " ORDER BY ";
        $query .= "  T1.GRADE,T1.HR_CLASS ";

//        echo $query;
        return $query;
    }
    //コース取得
    public function GetCourseCode()
    {
        $query = " SELECT ";
        $query .= "   COURSECODE, ";
        $query .= "   COURSECODENAME ";
        $query .= " FROM ";
        $query .= "   COURSECODE_MST ";

        return $query;
    }

    //年度コンボ（学期マスタ）
    public static function getYearSemester($exp_year)
    {
        $pre_year = CTRL_YEAR - 1;
        $query  = " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SEMESTER, ";
        $query .= "   SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "       '$pre_year' <= YEAR AND SEMESTER <> '9' ";
        if ($exp_year != "off") {
            $query .= "   AND YEAR || '-' || SEMESTER = '" .$exp_year. "' ";
        }
        $query .= " ORDER BY ";
        $query .= "   YEAR DESC, SEMESTER DESC";

        return $query;
    }
    //卒業年度
    public function GetGrdYear()
    {
        $query = " SELECT DISTINCT ";
        $query .= "   FISCALYEAR(GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "   GRD_BASE_MST ";

        return $query;
    }
    public function GetStudents($model)
    {
        if ($model->cmd == "list" && $model->grade) {
            $grade = $model->grade;
        } elseif ($model->cmd == "list" && !$model->grade) {
            $grade = "";
        } elseif ($model->cmd == "search" && $model->search["GRADE"]) {
            $grade = $model->search["GRADE"];
        }

        $query = " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T3.HR_NAME, ";
        $query .= "   T3.HR_NAMEABBV, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T2.SEX, ";
        $query .= "   T2.NAME_SHOW, ";
        $query .= "   T2.NAME_KANA, ";
        $query .= "   T2.GRD_SEMESTER, ";
        $query .= "   T2.GRD_GRADE, ";
        $query .= "   T2.GRD_HR_CLASS, ";
        $query .= "   T2.GRD_ATTENDNO, ";
        $query .= "   FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "   GRD_REGD_DAT T1, ";
        $query .= "   GRD_BASE_MST T2, ";
        $query .= "   GRD_REGD_HDAT T3 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T3.YEAR = REG_G.YEAR ";
                $query .= "           AND T3.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T3.YEAR = REG_G.YEAR ";
            $query .= "           AND T3.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        if ($model->mode == "grd" && $model->search["HR_CLASS"]) {
            $query .= "   T2.GRD_HR_CLASS = '" .sprintf("%03s", $model->search["HR_CLASS"]). "' AND ";
        }
        if ($model->mode == "grd" && $model->search["GRD_YEAR"]) {  //卒業生
            $query .= "   FISCALYEAR(T2.GRD_DATE) = '" .$model->search["GRD_YEAR"]. "' AND ";
        }
        if ($model->search["COURSECODE"]) {
            $query .= "   T1.COURSECODE = '" .$model->search["COURSECODE"]. "' AND ";
        }
        if ($model->search["NAME"]) {
            $query .= "   T2.NAME LIKE '%" .str_replace("'", "''", $model->search["NAME"]). "%' AND ";
        }
        if ($model->search["NAME_SHOW"]) {
            $query .= "   T2.NAME_SHOW LIKE '%" .str_replace("'", "''", $model->search["NAME_SHOW"]). "%' AND ";
        }
        if ($model->search["NAME_KANA"]) {
            $query .= "   T2.NAME_KANA LIKE '%" .str_replace("'", "''", $model->search["NAME_KANA"]). "%' AND ";
        }
        if ($model->search["NAME_ENG"]) {
            $query .= "   T2.NAME_ENG LIKE '%" .str_replace("'", "''", $model->search["NAME_ENG"]). "%' AND ";
        }
        if ($model->search["SEX"]) {
            $query .= "   T2.SEX = '" .$model->search["SEX"]. "' AND ";
        }
        $query .= "   T1.YEAR     = T3.YEAR AND ";
        $query .= "   T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "   T1.GRADE    = T3.GRADE AND ";
        $query .= "   T1.HR_CLASS = T3.HR_CLASS AND ";

//      $query .= "   T1.YEAR     = FISCALYEAR(T2.GRD_DATE) AND ";
//      $query .= "   T1.SEMESTER = T2.GRD_SEMESTER AND ";
//      $query .= "   T1.GRADE    = T2.GRD_GRADE AND ";
//      $query .= "   T1.HR_CLASS = T2.GRD_HR_CLASS AND ";

        $query .= "   T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "   T1.GRADE,";
        $query .= "   T1.HR_CLASS,";
        $query .= "   T1.ATTENDNO ";

//        echo $query;
        return $query;
    }

    public function GetMStudents($model)
    {
        $grade = $model->grade;

        $query = " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T3.HR_NAME, ";
        $query .= "   T3.HR_NAMEABBV, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T2.SEX, ";
        $query .= "   T2.NAME_SHOW, ";
        $query .= "   T2.NAME_KANA, ";
        $query .= "   T2.GRD_SEMESTER, ";
        $query .= "   T2.GRD_GRADE, ";
        $query .= "   T2.GRD_HR_CLASS, ";
        $query .= "   T2.GRD_ATTENDNO, ";
        $query .= "   FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "   GRD_REGD_DAT T1 ";
        $query .= "   LEFT JOIN GRD_REGD_HDAT T3 ON T1.YEAR = T3.YEAR ";
        $query .= "   AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "   AND T1.GRADE || T1.HR_CLASS = T3.GRADE || T3.HR_CLASS ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T3.YEAR = REG_G.YEAR ";
                $query .= "           AND T3.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T3.YEAR = REG_G.YEAR ";
            $query .= "           AND T3.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "   LEFT JOIN GRD_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";

        $query .= "   T1.YEAR     = '".$model->year."' AND ";
        $query .= "   T1.SEMESTER = '".$model->seme."' AND ";
        $query .= "   T1.GRADE    = '".SUBSTR($model->grade, 0, 2)."' AND ";
        $query .= "   T1.HR_CLASS = '".SUBSTR($model->grade, 3)."' ";

//      $query .= "   T1.YEAR     = FISCALYEAR(T2.GRD_DATE) AND ";
//      $query .= "   T1.SEMESTER = T2.GRD_SEMESTER AND ";
//      $query .= "   T1.GRADE    = T2.GRD_GRADE AND ";
//      $query .= "   T1.HR_CLASS = T2.GRD_HR_CLASS AND ";

//        $query .= "   T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "   T1.GRADE,";
        $query .= "   T1.HR_CLASS,";
        $query .= "   T1.ATTENDNO ";

//        echo $query;
        return $query;
    }
}
