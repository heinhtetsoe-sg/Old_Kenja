<?php

require_once('for_php7.php');

class knjxexpQuery extends Query
{
    //V_NAME_MSTから情報を取得
    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '  ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."'";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    //年度コンボフラグを取得（名称マスタ）
    public function getComboFlg()
    {
        $db     = Query::dbCheckOut();
        $query  = "SELECT NAMECD2, ";
        $query .= "       NAME1 as PRGID, ";
        $query .= "       NAMESPARE1 as COMBO_FLG ";
        $query .= "FROM   NAME_MST ";
        $query .= "WHERE  NAMECD1 = 'A022' ";
        $ret_val = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $ret_val[$row["PRGID"]] = $row["COMBO_FLG"];
        }
        $result->free();
        Query::dbCheckIn($db);
        return $ret_val;
    }
    //年度コンボ（学期マスタ）
    public function getYearSemester($exp_year)
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
        $query .= "   YEAR, SEMESTER ";

        return $query;
    }
    //学年と組
    public function getHrClass($model)
    {
        $query = "";
        $query .= "SELECT ";
        $query .= "  T1.GRADE, ";
        $query .= "  T1.HR_CLASS, ";
        $query .= "  T1.HR_NAME, ";
        $query .= "  T1.HR_NAMEABBV, ";
        $query .= "  T2.STAFFCD, ";
        $query .= "  T2.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "  SCHREG_REGD_HDAT T1 ";
        if ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT) {
            $query .= "  LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR  = T1.YEAR ";
            $query .= "                               AND L1.GRADE = T1.GRADE ";
        }
        $query .= "  ,STAFF_MST T2 ";
        $query .= "WHERE ";
        if ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT) {
            $query .= " ((T1.TR_CD1 = '". STAFFCD ."' OR";           //2005/06/29
            $query .= "  T1.TR_CD2 = '". STAFFCD ."' OR";           //2005/06/29
            $query .= "  T1.TR_CD3 = '". STAFFCD ."' OR";           //2005/06/29
            $query .= "  T1.SUBTR_CD1 = '". STAFFCD ."' OR";        //2005/06/29
            $query .= "  T1.SUBTR_CD2 = '". STAFFCD ."' OR";        //2005/06/29
            $query .= "  T1.SUBTR_CD3 = '". STAFFCD ."') ";      //2005/06/29
            $query .= " OR (L1.GRADE IN ";
            $query .= "       (SELECT ";
            $query .= "            FIELD2 ";
            $query .= "        FROM ";
            $query .= "            STAFF_DETAIL_MST ST ";
            $query .= "        WHERE ";
            $query .= "            T1.YEAR = ST.YEAR ";
            $query .= "            AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "            AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "            AND ST.FIELD1 = '0200')) ";
            $query .= "   ) AND ";
        }
        $query .= "  T1.TR_CD1 = T2.STAFFCD AND ";
        $query .= "  T1.YEAR || '-' || T1.SEMESTER = '" .$model->exp_year. "' ";
        if ($model->programid == "KNJD105B") {
            $query .= "  AND T1.GRADE NOT IN (SELECT T1.GRADE ";
            $query .= "                    FROM SCHREG_REGD_HDAT T1, ";
            $query .= "                         NAME_MST T2 ";
            $query .= "                    WHERE T1.YEAR || '-' || T1.SEMESTER = '" .$model->exp_year. "' AND ";
            $query .= "                          T2.NAMECD1 = 'Z010' AND ";
            $query .= "                          T2.NAMECD2 = '00' AND ";
            $query .= "                          T2.NAME1 = 'CHIBEN' AND ";
            $query .= "                          T1.GRADE > '10') ";
        }
        if ($model->grdGrade == "1") {
            $query .= "  AND EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 NAME_MST A023 ";
            $query .= "             WHERE ";
            $query .= "                 A023.NAMECD1 = 'A023' ";
            $query .= "                 AND T1.GRADE BETWEEN A023.NAMESPARE2 AND A023.NAMESPARE3) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "  AND EXISTS (SELECT ";
                $query .= "                 'X' ";
                $query .= "             FROM ";
                $query .= "                 SCHREG_REGD_GDAT S1 ";
                $query .= "             WHERE ";
                $query .= "                 S1.YEAR     = T1.YEAR AND ";
                $query .= "                 S1.GRADE    = T1.GRADE AND ";
                $query .= "                 S1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->school_kind != "") {
            $query .= "  AND EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 SCHREG_REGD_GDAT S1 ";
            $query .= "             WHERE ";
            $query .= "                 S1.YEAR     = T1.YEAR AND ";
            $query .= "                 S1.GRADE    = T1.GRADE AND ";
            $query .= "                 S1.SCHOOL_KIND IN ('".implode(explode(':', $model->school_kind), "','")."')) ";
        }

        $query .= " ORDER BY ";
        $query .= "  T1.GRADE,T1.HR_CLASS ";

        #echo $query;
        return $query;
    }

    //学年と組（担当保健室）
    public function getHrClass2($model)
    {
        list ($year, $semester) = explode('-', $model->exp_year);

        $query  = " WITH COURSE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.NAMESPARE1 AS SCHOOL_KIND, ";
        $query .= "         T1.NAMESPARE2 AS COURSECD, ";
        $query .= "         T1.NAMESPARE3 AS MAJORCD ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST T1, ";
        $query .= "         (SELECT ";
        $query .= "             T2.NAMESPARE1 ";
        $query .= "         FROM ";
        $query .= "             STAFF_DETAIL_MST T1, ";
        $query .= "             V_NAME_MST T2 ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR         = T2.YEAR AND ";
        $query .= "             T1.YEAR         = '".$year."' AND ";
        $query .= "             T1.STAFFCD      = '".STAFFCD."' AND ";
        $query .= "             T1.STAFF_SEQ    = '008' AND ";
        $query .= "             T2.NAMECD1      = 'Z043' AND ";
        $query .= "             T1.FIELD1       = T2.NAMECD2 ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$year."' AND ";
        $query .= "         T1.NAMECD1  = T2.NAMESPARE1 ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     S1.HR_NAME, ";
        $query .= "     S1.HR_NAMEABBV, ";
        $query .= "     S3.STAFFCD, ";
        $query .= "     S3.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT S1 ";
        $query .= "          ON T1.YEAR     = S1.YEAR ";
        $query .= "         AND T1.SEMESTER = S1.SEMESTER ";
        $query .= "         AND T1.GRADE    = S1.GRADE ";
        $query .= "         AND T1.HR_CLASS = S1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
        $query .= "          ON T1.YEAR     = S2.YEAR ";
        $query .= "         AND T1.GRADE    = S2.GRADE ";
        $query .= "     INNER JOIN STAFF_MST S3 ";
        $query .= "          ON S1.TR_CD1   = S3.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '".$model->exp_year."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     S2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') AND ";
            }
        } elseif ($model->school_kind != "") {
            $query .= "     S2.SCHOOL_KIND IN ('".implode(explode(':', $model->school_kind), "','")."') AND ";
        }
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 COURSE E1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.COURSECD     = E1.COURSECD AND ";
        $query .= "                 T1.MAJORCD      = E1.MAJORCD AND ";
        $query .= "                 S2.SCHOOL_KIND  = E1.SCHOOL_KIND ";
        $query .= "             ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";

        return $query;
    }

    //コース取得
    public function getCourseCode()
    {
        $query = " SELECT ";
        $query .= "   COURSECODE, ";
        $query .= "   COURSECODENAME ";
        $query .= " FROM ";
        $query .= "   COURSECODE_MST ";

        return $query;
    }
    //卒業年度
    public function getGrdYear()
    {
        $query = " SELECT DISTINCT ";
        $query .= "   FISCALYEAR(GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "   GRD_BASE_MST ";
        $query .= " ORDER BY ";
        $query .= "     GRD_YEAR DESC ";

        return $query;
    }

    //卒業年組
    public function getGrdHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     GREGDH.GRADE || GREGDH.HR_CLASS AS VALUE, ";
        $query .= "     MAX(GREGDH.HR_NAME) AS LABEL ";
        $query .= " FROM ";
        $query .= "     GRD_REGD_HDAT GREGDH ";
        $query .= "     INNER JOIN GRD_BASE_MST GBASE ON GREGDH.SEMESTER = GBASE.GRD_SEMESTER ";
        $query .= "           AND GREGDH.GRADE = GBASE.GRD_GRADE ";
        $query .= "           AND GREGDH.HR_CLASS = GBASE.GRD_HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         GREGDH.YEAR = CAST(FISCALYEAR(GBASE.GRD_DATE) AS VARCHAR(4)) ";
        $query .= "     AND GREGDH.YEAR = '{$model->search["GRD_YEAR"]}' ";
        $query .= " GROUP BY ";
        $query .= "     GREGDH.GRADE, ";
        $query .= "     GREGDH.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getStudents($model)
    {
        if ($model->cmd == "list" && $model->grade) {
            $grade = $model->grade;
        } elseif ($model->cmd == "list" && !$model->grade) {
            $grade = "";
        } elseif ($model->cmd == "search" && $model->search["GRADE"]) {
            $grade = $model->search["GRADE"];
        }

        list ($year, $semester) = explode('-', $model->exp_year);

        $query  = "";

        if ($model->programid == "KNJF150" && $model->Properties["useNurseoffRestrict"] == "1" && ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT)) {
            $query .= " WITH COURSE AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.NAMESPARE1 AS SCHOOL_KIND, ";
            $query .= "         T1.NAMESPARE2 AS COURSECD, ";
            $query .= "         T1.NAMESPARE3 AS MAJORCD ";
            $query .= "     FROM ";
            $query .= "         V_NAME_MST T1, ";
            $query .= "         (SELECT ";
            $query .= "             T2.NAMESPARE1 ";
            $query .= "         FROM ";
            $query .= "             STAFF_DETAIL_MST T1, ";
            $query .= "             V_NAME_MST T2 ";
            $query .= "         WHERE ";
            $query .= "             T1.YEAR         = T2.YEAR AND ";
            $query .= "             T1.YEAR         = '".$year."' AND ";
            $query .= "             T1.STAFFCD      = '".STAFFCD."' AND ";
            $query .= "             T1.STAFF_SEQ    = '008' AND ";
            $query .= "             T2.NAMECD1      = 'Z043' AND ";
            $query .= "             T1.FIELD1       = T2.NAMECD2 ";
            $query .= "         ) T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".$year."' AND ";
            $query .= "         T1.NAMECD1  = T2.NAMESPARE1 ";
            $query .= " ) ";
        }

        $query .= " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T3.HR_NAME, ";
        $query .= "   T3.HR_NAMEABBV, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T2.SEX, ";
        $query .= "   T2.NAME_SHOW, ";
        $query .= "   T2.NAME_KANA, ";
        if ($model->mode == "grd") {   //卒業
            $query .= "   T2.GRD_SEMESTER, ";
            $query .= "   T2.GRD_GRADE, ";
            $query .= "   T2.GRD_HR_CLASS, ";
            $query .= "   T2.GRD_ATTENDNO, ";
        }
        $query .= "   FISCALYEAR(T2.ENT_DATE) AS ENT_YEAR, ";
        $query .= "   FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR, ";
        $query .= "   SUBSTR(T1.SCHREGNO, 1, 4) AS DUMMY1, ";
        $query .= "   SUBSTR(T1.SCHREGNO, 5, 4) AS DUMMY2 ";
        $query .= " FROM ";
        if ($model->mode == "grd") {   //卒業
            $query .= "   GRD_REGD_DAT T1, ";
            $query .= "   GRD_BASE_MST T2, ";
            $query .= "   GRD_REGD_HDAT T3 ";
        } elseif ($model->mode == "ungrd") { //在籍
            $query .= "   SCHREG_REGD_DAT T1, ";
            $query .= "   SCHREG_BASE_MST T2,";
            $query .= "   SCHREG_REGD_HDAT T3 ";
        }
        if ($model->search["DATE"]) {
            $query .= "   ,NURSEOFF_VISITREC_DAT T4 ";
        }
        if ($model->search["TRANSFERCD"]) {
            if ($model->mode == "grd") {   //卒業
                $query .= "   LEFT JOIN GRD_TRANSFER_DAT T5 ON T5.SCHREGNO = T1.SCHREGNO ";
            } elseif ($model->mode == "ungrd") { //在籍
                $query .= "   LEFT JOIN SCHREG_TRANSFER_DAT T5 ON T5.SCHREGNO = T1.SCHREGNO ";
            }
            $query .= "   LEFT JOIN SEMESTER_MST S1 ON S1.YEAR = T1.YEAR AND S1.SEMESTER = T1.SEMESTER ";
        }
        $query .= " WHERE ";
        if (isset($grade) && $model->mode == "ungrd" ) {
            $query .= "   T1.GRADE || '-' || T1.HR_CLASS = '" .$grade."' AND ";
        }
        if ($model->mode == "grd" && $model->search["HR_CLASS"]) {
            $query .= "   T2.GRD_GRADE || T2.GRD_HR_CLASS = '" .sprintf("%03s", $model->search["HR_CLASS"]). "' AND ";
        }
        if ($model->mode == "ungrd") {  //在籍生
            $query .= "   T1.YEAR || '-' || T1.SEMESTER = '" .$model->exp_year. "' AND ";
        } elseif ($model->mode == "grd" && $model->search["GRD_YEAR"]) {  //卒業生
            $query .= "   FISCALYEAR(T2.GRD_DATE) = '" .$model->search["GRD_YEAR"]. "' AND ";
        }
        if ($model->search["COURSECODE"]) {
            $query .= "   T1.COURSECODE = '" .$model->search["COURSECODE"]. "' AND ";
        }
        if ($model->search["SRCH_SCHREGNO"]) {
            if ($model->Properties["schregno_bubunkensaku"] == "1") {
                $query .= "   T1.SCHREGNO LIKE '%" .$model->search["SRCH_SCHREGNO"]. "%' AND ";
            } else {
                $query .= "   T1.SCHREGNO = '" .$model->search["SRCH_SCHREGNO"]. "' AND ";
            }
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
        if ($model->search["DATE"]) {
            $query .= "   T4.VISIT_DATE = '" .$model->search["DATE"]. "' AND ";
            $query .= "   T1.SCHREGNO = T4.SCHREGNO AND ";
        }
        if ($model->search["ENT_DIV"]) {
            $query .= "   T2.ENT_DIV = '" .$model->search["ENT_DIV"]. "' AND ";
        }
        if ($model->search["GRD_DIV"]) {
            $query .= "   T2.GRD_DIV = '" .$model->search["GRD_DIV"]. "' AND ";
        }
        if ($model->search["TRANSFERCD"]) {
            $query .= "   T5.TRANSFERCD = '" .$model->search["TRANSFERCD"]. "' AND ";
            $query .= "   (T5.TRANSFER_SDATE BETWEEN S1.SDATE AND S1.EDATE OR T5.TRANSFER_EDATE BETWEEN S1.SDATE AND S1.EDATE) AND ";
        }
        $query .= "   T1.YEAR     = T3.YEAR AND ";
        $query .= "   T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "   T1.GRADE    = T3.GRADE AND ";
        $query .= "   T1.HR_CLASS = T3.HR_CLASS AND ";

        if ($model->mode == "grd") {  //卒業生
            $query .= "   T1.YEAR     = FISCALYEAR(T2.GRD_DATE) AND ";
            $query .= "   T1.SEMESTER = T2.GRD_SEMESTER AND ";
            $query .= "   T1.GRADE    = T2.GRD_GRADE AND ";
            $query .= "   T1.HR_CLASS = T2.GRD_HR_CLASS AND ";
        }

        if ($model->mode == "ungrd" && $model->programid == "KNJD105B") {
            $query .= "   T1.GRADE NOT IN (SELECT S1.GRADE ";
            $query .= "                    FROM SCHREG_REGD_HDAT S1, ";
            $query .= "                         NAME_MST S2 ";
            $query .= "                    WHERE S1.YEAR || '-' || S1.SEMESTER = '" .$model->exp_year. "' AND ";
            $query .= "                          S2.NAMECD1 = 'Z010' AND ";
            $query .= "                          S2.NAMECD2 = '00' AND ";
            $query .= "                          S2.NAME1 = 'CHIBEN' AND ";
            $query .= "                          S1.GRADE > '06') AND ";
        }

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     EXISTS (SELECT ";
                $query .= "                 'X' ";
                $query .= "             FROM ";
                $query .= "                 SCHREG_REGD_GDAT G1 ";
                $query .= "             WHERE ";
                $query .= "                 T1.YEAR     = G1.YEAR AND ";
                $query .= "                 T1.GRADE    = G1.GRADE AND ";
                $query .= "                 G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) AND ";
            }
        } elseif ($model->mode == "ungrd" && $model->school_kind != "") {
            $query .= "     EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 SCHREG_REGD_GDAT G1 ";
            $query .= "             WHERE ";
            $query .= "                 T1.YEAR     = G1.YEAR AND ";
            $query .= "                 T1.GRADE    = G1.GRADE AND ";
            $query .= "                 G1.SCHOOL_KIND IN ('".implode(explode(':', $model->school_kind), "','")."')) AND ";
        } elseif ($model->mode == "grd" && $model->school_kind != "") {
            $query .= "     NOT EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 SCHREG_REGD_GDAT G1 ";
            $query .= "             WHERE ";
            $query .= "                 T1.YEAR     = G1.YEAR AND ";
            $query .= "                 T1.GRADE    = G1.GRADE AND ";
            $query .= "                 G1.SCHOOL_KIND NOT IN ('".implode(explode(':', $model->school_kind), "','")."')) AND ";
        }

        if ($model->programid == "KNJF150" && $model->Properties["useNurseoffRestrict"] == "1" && ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT)) {
            $query .= "     EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 COURSE E1 ";
            $query .= "             WHERE ";
            $query .= "                 T1.COURSECD = E1.COURSECD ";
            $query .= "                 AND T1.MAJORCD  = E1.MAJORCD ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "                 AND E1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } else {
                $query .= "                 AND E1.SCHOOL_KIND IN ('".implode(explode(':', $model->school_kind), "','")."') ";
            }
            $query .= "            ) AND ";
        }

        $query .= "   T1.SCHREGNO = T2.SCHREGNO ";

        $query .= " ORDER BY ";
        $query .= "   T1.GRADE,";
        $query .= "   T1.HR_CLASS,";
        if ($model->Properties["KNJXEXP_SEARCH"] == "SCHREGNO") {
            $query .= "   SUBSTR(T1.SCHREGNO, 1, 4) DESC, ";
            $query .= "   SUBSTR(T1.SCHREGNO, 5, 4) ";
        } elseif ($model->Properties["KNJXEXP_SEARCH"] == "SCHREGNO_NORMAL") {
            $query .= "   SCHREGNO ";
        } else {
            $query .= "   T1.ATTENDNO ";
        }

        #echo $query;
        return $query;
    }
}
