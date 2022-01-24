<?php

require_once('for_php7.php');

class knji130Query extends Query
{

    //年組取得
    public function searchGradeHrClass($model, $year, $grade_hr_class = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.HR_NAME AS LABEL, ";
        $query .= "     T2.GRADE || '-' || T2.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ON ";
        $query .= "         T2.YEAR     = T3.YEAR AND ";
        $query .= "         T2.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T2.GRADE    = T3.GRADE AND ";
        $query .= "         T2.HR_CLASS = T3.HR_CLASS ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.GRD_DIV IN ('2','3','6') AND ";
        $query .= "     FISCALYEAR(T1.GRD_DATE) = T2.YEAR AND ";
        $query .= "     T2.YEAR     = '".$year."' ";
        if ($grade_hr_class) {
            $query .= "     AND T2.GRADE || '-' || T2.HR_CLASS = '".$grade_hr_class."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //V_NAME_MST取得
    public function getNameMst($namecd1, $namecd2 = "", $flg = "")
    {
        $query  = " SELECT ";
        if ($flg == "1") {
            $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        } elseif ($flg == "2") {
            $query .= "     NAME1 AS LABEL ";
        } elseif ($flg == "3") {
            $query .= "     NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE ";
        } else {
            $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //生徒一覧取得
    public function getStudentList($model)
    {
        $query  = " WITH SCH_LIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         MAX(T1.YEAR || '-' || T1.SEMESTER) AS YEAR_SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "         ,SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T2.GRD_DIV IN ('2','3','6') ";
        if ($model->search["GRD_YEAR"]) {
            $query .= "     AND FISCALYEAR(T2.GRD_DATE) = '" .$model->search["GRD_YEAR"]. "' ";
            $query .= "     AND FISCALYEAR(T2.GRD_DATE) = T1.YEAR ";
        }
        if ($model->search["GRADE_HR_CLASS"]) {
            $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '" .$model->search["GRADE_HR_CLASS"]. "' ";
        }
        if ($model->search["COURSECODE"]) {
            $query .= "     AND T1.COURSECODE = '" .$model->search["COURSECODE"]. "' ";
        }
        if ($model->search["S_SCHREGNO"]) {
            $query .= "     AND T1.SCHREGNO LIKE '%" .str_replace("'", "''", $model->search["S_SCHREGNO"]). "%' ";
        }
        if ($model->search["NAME"]) {
            $query .= "     AND T2.NAME LIKE '%" .str_replace("'", "''", $model->search["NAME"]). "%' ";
        }
        if ($model->search["NAME_SHOW"]) {
            $query .= "     AND T2.NAME_SHOW LIKE '%" .str_replace("'", "''", $model->search["NAME_SHOW"]). "%' ";
        }
        if ($model->search["NAME_KANA"]) {
            $query .= "     AND T2.NAME_KANA LIKE '%" .str_replace("'", "''", $model->search["NAME_KANA"]). "%' ";
        }
        if ($model->search["SEX"]) {
            $query .= "     AND T2.SEX = '" .$model->search["SEX"]. "' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L3.HR_NAME, ";
        $query .= "     L3.HR_NAMEABBV, ";
        $query .= "     L1.ATTENDNO, ";
        $query .= "     L1.COURSECD, ";
        $query .= "     L1.MAJORCD, ";
        $query .= "     L1.COURSECODE, ";
        $query .= "     L2.SEX, ";
        $query .= "     L2.NAME, ";
        $query .= "     L2.NAME_SHOW, ";
        $query .= "     L2.NAME_KANA, ";
        $query .= "     L1.YEAR, ";
        $query .= "     L1.SEMESTER ";
        $query .= " FROM ";
        $query .= "     SCH_LIST T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT L1  ON T1.SCHREGNO    = L1.SCHREGNO ";
        $query .= "                                  AND T1.YEAR_SEMESTER = L1.YEAR || '-' || L1.SEMESTER ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L2  ON T1.SCHREGNO    = L2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L3 ON L1.YEAR        = L3.YEAR ";
        $query .= "                                  AND L1.SEMESTER    = L3.SEMESTER ";
        $query .= "                                  AND L1.GRADE       = L3.GRADE ";
        $query .= "                                  AND L1.HR_CLASS    = L3.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     L1.YEAR, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.ATTENDNO ";

        return $query;
    }

    //年度
    public function getGrdDateYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "   FISCALYEAR(GRD_DATE) AS VALUE, ";
        $query .= "   FISCALYEAR(GRD_DATE) || '年' AS LABEL ";
        $query .= " FROM ";
        $query .= "   SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     GRD_DIV IN ('2','3','6') AND ";
        $query .= "     GRD_DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "   VALUE DESC ";

        return $query;
    }

    //コースコード取得
    public function searchCourseCodeMst($coursecode = "")
    {
        $query  = " SELECT ";
        $query .= "     COURSECODE || '　' || COURSECODENAME AS LABEL, ";
        $query .= "     COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSECODE_MST ";
        if ($coursecode) {
            $query .= " WHERE ";
            $query .= "     COURSECODE = '".$coursecode."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //各生徒情報
    public function getStudentData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.ANNUAL, ";
        $query .= "     T1.INOUTCD, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.REAL_NAME, ";
        $query .= "     T1.REAL_NAME_KANA, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BLOODTYPE, ";
        $query .= "     T1.BLOOD_RH, ";
        $query .= "     T1.HANDICAP, ";
        $query .= "     T1.NATIONALITY, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.FINSCHOOLCD ";
        $query .= "          ELSE T1.FINSCHOOLCD ";
        $query .= "          END AS FINSCHOOLCD, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.FINISH_DATE ";
        $query .= "          ELSE T1.FINISH_DATE ";
        $query .= "          END AS FINISH_DATE, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.ENT_DATE ";
        $query .= "          ELSE T1.ENT_DATE ";
        $query .= "          END AS ENT_DATE, ";
        $query .= "     T3.CURRICULUM_YEAR, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.ENT_DIV ";
        $query .= "          ELSE T1.ENT_DIV ";
        $query .= "          END AS ENT_DIV, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.ENT_REASON ";
        $query .= "          ELSE T1.ENT_REASON ";
        $query .= "          END AS ENT_REASON, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.ENT_SCHOOL ";
        $query .= "          ELSE T1.ENT_SCHOOL ";
        $query .= "          END AS ENT_SCHOOL, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.ENT_ADDR ";
        $query .= "          ELSE T1.ENT_ADDR ";
        $query .= "          END AS ENT_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
            $query .= "          THEN T3.ENT_ADDR2 ";
            $query .= "          ELSE T1.ENT_ADDR2 ";
            $query .= "          END AS ENT_ADDR2, ";
        }
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.GRD_DATE ";
        $query .= "          ELSE T1.GRD_DATE ";
        $query .= "          END AS GRD_DATE, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.GRD_DIV ";
        $query .= "          ELSE T1.GRD_DIV ";
        $query .= "          END AS GRD_DIV, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.GRD_REASON ";
        $query .= "          ELSE T1.GRD_REASON ";
        $query .= "          END AS GRD_REASON, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.GRD_SCHOOL ";
        $query .= "          ELSE T1.GRD_SCHOOL ";
        $query .= "          END AS GRD_SCHOOL, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.GRD_ADDR ";
        $query .= "          ELSE T1.GRD_ADDR ";
        $query .= "          END AS GRD_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
            $query .= "          THEN T3.GRD_ADDR2 ";
            $query .= "          ELSE T1.GRD_ADDR2 ";
            $query .= "          END AS GRD_ADDR2, ";
        }
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.GRD_NO ";
        $query .= "          ELSE T1.GRD_NO ";
        $query .= "          END AS GRD_NO, ";
        $query .= "     T1.TENGAKU_SAKI_ZENJITU, ";
        $query .= "     T1.TENGAKU_SAKI_GRADE, ";
        $query .= "     T1.NYUGAKUMAE_SYUSSIN_JOUHOU, ";
        $query .= "     T4.BASE_REMARK1 AS EXAMNO, ";
        $query .= "     T2.COURSECODE, ";
        $query .= "     T1.UPDATED AS UPDATED1, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.COURSECD ||','|| T2.MAJORCD AS COURSEMAJORCD, ";
        $query .= "     T2.UPDATED AS UPDATED2, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.GRADE || ',' || T2.HR_CLASS AS GRCL ";
        $query .= " FROM ";
        $query .= "     V_SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "                                         AND T3.SCHOOL_KIND = '". $model->schoolKind. "' ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST T4 ON T4.SCHREGNO = T1.SCHREGNO AND T4.BASE_SEQ = '003' ";
        $query .= " WHERE";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      T2.YEAR     = '".$model->exp_year."' AND ";
        $query .= "      T2.SEMESTER = '".$model->exp_semester."' ";

        return $query;
    }

    //復学対象生徒チェック
    public function checkSchData($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     VALUE(GRD_DIV,'0') NOT IN ('2','3','6') ";

        return $query;
    }

    //起動チェック
    public function checktoStart($db)
    {

        //テーブル事前チェック
        foreach (array("SCHREG_REGD_HDAT", "V_COURSE_MST", "V_MAJOR_MST", "V_FINSCHOOL_MST") as $val) {
            $query = " SELECT COUNT(*) FROM ".$val." WHERE YEAR = '".CTRL_YEAR."'";
            if ($val == "SCHREG_REGD_HDAT") {
                $query .= " AND SEMESTER = '".CTRL_SEMESTER."' ";
            }

            if ($db->getOne($query) == "0") {
                return false;
            }
        }

        //名称マスタ事前チェック
        foreach (array("A001", "A002", "A003", "Z002") as $val) {
            $query = " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 = '".$val."' ";
            if ($db->getOne($query) == "0") {
                return false;
            }
        }

        return true;
    }

    //年組取得
    public function getGradeHrClass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE || ',' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.TR_CD1      = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2      = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3      = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1   = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD2   = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD3   = '".STAFFCD."' ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科コンボ
    public function getCourseMajor()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "      COURSECD || ',' || MAJORCD AS VALUE, ";
        $query .= "      COURSECD || MAJORCD || ' ' || COURSENAME || MAJORABBV AS LABEL ";
        $query .= "  FROM ";
        $query .= "      V_COURSE_MAJOR_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //コースコード取得
    public function getCourseCode()
    {
        $query  = " SELECT ";
        $query .= "     COURSECODE AS VALUE, ";
        $query .= "     COURSECODE || ' ' || COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSECODE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."'";
        $query .= " ORDER BY ";
        $query .= "     COURSECODE ";

        return $query;
    }

    //出身中学校コンボ
    public function getFinschoolName($finschoolcd)
    {
        $query .= " SELECT ";
        $query .= "     FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '".$finschoolcd."' ";

        return $query;
    }

    //学年種別取得
    public function getSchoolKind($model, $grade)
    {
        $db = Query::dbCheckOut();

        $query  = "  SELECT";
        $query .= "      SCHOOL_KIND ";
        $query .= "  FROM";
        $query .= "      SCHREG_REGD_GDAT ";
        $query .= "  WHERE";
        $query .= "      YEAR   = '".$model->exp_year."' AND";
        $query .= "      GRADE  = '".$grade."' ";

        $school_kind = $db->getOne($query);
        Query::dbCheckIn($db);

        return $school_kind;
    }

    //復学日重複チェック
    public function isExistComebackDate($model)
    {
        $db = Query::dbCheckOut();

        $fukugaku_date = str_replace("/", "-", $model->field["FUKUGAKU_DATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_COMEBACK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO      = '".$model->schregno."' AND ";
        $query .= "     COMEBACK_DATE = '".$fukugaku_date."' ";

        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return (($row == 0) ? false : true);
    }

    //出席番号重複チェック
    public function isExistAttendno($model)
    {
        $db = Query::dbCheckOut();
        list($grade, $hr_class) = explode(",", $model->field["GRCL"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE  ";
        $query .= "     GRADE    = '".$grade."' AND ";
        $query .= "     HR_CLASS = '".$hr_class."' AND ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     ATTENDNO = '".sprintf("%03d", $model->field["ATTENDNO"])."' AND ";
        $query .= "     SCHREGNO <> '".$model->schregno."' ";

        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return (($row == 0) ? false : true);
    }

    //SCHREG_REGD_DATのデータ有無チェック
    public function isExistRegdDat($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."'  ";

        return $query;
    }

    //SCHREG_BASE_DETAIL_MSTのデータ有無チェック
    public function isExistBaseDetailMst($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     BASE_SEQ = '".$seq."'  ";

        return $query;
    }

    //アップデート
    public function &getUpdateQuery($model, $table)
    {
        //DB接続
        $db = Query::dbCheckOut();

        if ($table == "SCHREG_BASE_MST") {
            //SCHREG_ENT_GRD_HIST_DATデータ取得
            $entGrdData = $db->getRow(knji130Query::getEntGrdData($model, $model->schregno, "MAX"), DB_FETCHMODE_ASSOC);

            $data = array();
            $data["INOUTCD"][TEXT]      = $model->field["INOUTCD"];
            $data["ENT_DATE"][TEXT]     = str_replace("/", "-", $entGrdData["ENT_DATE"]);
            $data["ENT_DIV"][TEXT]      = $entGrdData["ENT_DIV"];
            $data["ENT_REASON"][TEXT]   = $entGrdData["ENT_REASON"];
            $data["ENT_SCHOOL"][TEXT]   = $entGrdData["ENT_SCHOOL"];
            $data["ENT_ADDR"][TEXT]     = $entGrdData["ENT_ADDR"];
            if ($model->Properties["useAddrField2"] == "1") {
                $data["ENT_ADDR2"][TEXT]   = $entGrdData["ENT_ADDR2"];
            }
            $data["GRD_DATE"][TEXT]     = str_replace("/", "-", $entGrdData["GRD_DATE"]);
            $data["GRD_DIV"][TEXT]      = $entGrdData["GRD_DIV"];
            $data["GRD_REASON"][TEXT]   = $entGrdData["GRD_REASON"];
            $data["GRD_SCHOOL"][TEXT]   = $entGrdData["GRD_SCHOOL"];
            $data["GRD_ADDR"][TEXT]     = $entGrdData["GRD_ADDR"];
            if ($model->Properties["useAddrField2"] == "1") {
                $data["GRD_ADDR2"][TEXT]    = $entGrdData["GRD_ADDR2"];
            }
            $data["GRD_TERM"][TEXT]     = $entGrdData["GRD_TERM"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where = " WHERE SCHREGNO = '".$model->schregno."'";

            $query = Query::updateSQL($data, $table, $where);
            $db->query($query);
        }

        if ($table == "SCHREG_REGD_DAT") {
            list($coursecd, $majorcd)   = explode(",", $model->field["COURSEMAJORCD"]);
            list($grade, $hr_class)     = explode(",", $model->field["GRCL"]);

            //SCHREG_REGD_DATのデータ有無チェック
            $chkRegd = $db->getOne(knji130Query::isExistRegdDat($model));

            $data = array();
            $data["GRADE"][TEXT]        = $grade;
            $data["HR_CLASS"][TEXT]     = $hr_class;
            $data["ATTENDNO"][TEXT]     = sprintf("%03d", $model->field["ATTENDNO"]);
            $data["ANNUAL"][TEXT]       = sprintf("%02d", $model->field["ANNUAL"]);
            $data["SEAT_COL"][FUNC]     = "conv_col_row('".sprintf("%03d", $model->field["ATTENDNO"])."',1)";
            $data["SEAT_ROW"][FUNC]     = "conv_col_row('".sprintf("%03d", $model->field["ATTENDNO"])."',2)";
            $data["COURSECD"][TEXT]     = $coursecd;
            $data["MAJORCD"][TEXT]      = $majorcd;
            $data["COURSECODE"][TEXT]   = $model->field["COURSECODE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if ($chkRegd > 0) {
                $where  = " WHERE ";
                $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
                $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $where .= "     SEMESTER    = '".CTRL_SEMESTER."' ";

                $query = Query::updateSQL($data, $table, $where);
                $db->query($query);
            } else {
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = CTRL_SEMESTER;

                $query = Query::insertSQL($data, $table);
                $db->query($query);
            }
        }

        if ($table == "SCHREG_BASE_DETAIL_MST") {
            //SCHREG_ENT_GRD_HIST_DATデータ取得
            $entGrdData = $db->getRow(knji130Query::getEntGrdData($model, $model->schregno, "MAX"), DB_FETCHMODE_ASSOC);

            $seq = array("001"  => "TENGAKU_SAKI_ZENJITU",
                         "002"  => "NYUGAKUMAE_SYUSSIN_JOUHOU");

            foreach ($seq as $key => $val) {
                //SCHREG_BASE_DETAIL_MSTのデータ有無チェック
                $chkDetail = $db->getOne(knji130Query::isExistBaseDetailMst($model, $key));

                $data = array();
                $data["BASE_REMARK1"][TEXT] = $entGrdData[$val];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                if ($chkDetail > 0) {
                    $where  = " WHERE ";
                    $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
                    $where .= "     BASE_SEQ    = '".$key."' ";

                    $query = Query::updateSQL($data, $table, $where);
                    $db->query($query);
                } else {
                    $data["SCHREGNO"][TEXT]     = $model->schregno;
                    $data["BASE_SEQ"][TEXT]     = $key;

                    $query = Query::insertSQL($data, $table);
                    $db->query($query);
                }
            }
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //SCHREG_ENT_GRD_HIST_DAT更新
    public function &getInsUpdEntGrd($model)
    {
        //DB接続
        $db = Query::dbCheckOut();
        //SCHREG_ENT_GRD_HIST_DATデータ有無チェック
        $entGrdData = $db->getRow(knji130Query::getEntGrdData($model, $model->schregno, ""), DB_FETCHMODE_ASSOC);
        //データ取得
        $insData = $db->getRow(knji130Query::getEntGrdIstData($model->schregno), DB_FETCHMODE_ASSOC);
        //追加
        $data = array();
        if (!is_array($entGrdData)) {
            $data["SCHREGNO"][TEXT]                     = $model->schregno;
            $data["SCHOOL_KIND"][TEXT]                  = $model->schoolKind;
            $data["FINSCHOOLCD"][TEXT]                  = $insData["FINSCHOOLCD"];
            $data["FINISH_DATE"][DATE]                  = $insData["FINISH_DATE"];
            $data["ENT_DATE"][DATE]                     = str_replace("/", "-", $model->field["ENT_DATE"]);
            $data["CURRICULUM_YEAR"][TEXT]              = $model->field["CURRICULUM_YEAR"];
            $data["ENT_DIV"][TEXT]                      = $model->field["ENT_DIV"];
            $data["ENT_REASON"][TEXT]                   = $model->field["ENT_REASON"];
            $data["ENT_SCHOOL"][TEXT]                   = $model->field["ENT_SCHOOL"];
            $data["ENT_ADDR"][TEXT]                     = $model->field["ENT_ADDR"];
            if ($model->Properties["useAddrField2"] == "1") {
                $data["ENT_ADDR2"][TEXT]                    = $model->field["ENT_ADDR2"];
            }
            $data["REGISTERCD"][TEXT]                   = STAFFCD;
            $data["UPDATED"][FUNC]                      = "sysdate()";

        //更新
        } else {
            $data["ENT_DATE"][DATE]         = str_replace("/", "-", $model->field["ENT_DATE"]);
            $data["CURRICULUM_YEAR"][TEXT]  = $model->field["CURRICULUM_YEAR"];
            $data["ENT_DIV"][TEXT]          = $model->field["ENT_DIV"];
            $data["ENT_REASON"][TEXT]       = $model->field["ENT_REASON"];
            $data["ENT_SCHOOL"][TEXT]       = $model->field["ENT_SCHOOL"];
            $data["ENT_ADDR"][TEXT]         = $model->field["ENT_ADDR"];
            if ($model->Properties["useAddrField2"] == "1") {
                $data["ENT_ADDR2"][TEXT]        = $model->field["ENT_ADDR2"];
            }
            $data["GRD_DATE"][DATE]         = "";
            $data["GRD_DIV"][TEXT]          = "";
            $data["GRD_REASON"][TEXT]       = "";
            $data["GRD_SCHOOL"][TEXT]       = "";
            $data["GRD_ADDR"][TEXT]         = "";
            if ($model->Properties["useAddrField2"] == "1") {
                $data["GRD_ADDR2"][TEXT]        = "";
            }
            $data["GRD_NO"][TEXT]           = "";
            $data["GRD_TERM"][TEXT]         = "";
            $data["TENGAKU_SAKI_ZENJITU"][DATE] = "";
            $data["NYUGAKUMAE_SYUSSIN_JOUHOU"][TEXT] = $entGrdData["NYUGAKUMAE_SYUSSIN_JOUHOU"];
        }

        if (!is_array($entGrdData)) {
            $query = Query::insertSQL($data, "SCHREG_ENT_GRD_HIST_DAT");
        } else {
            $where .= " WHERE ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     SCHOOL_KIND = '".$model->schoolKind."' ";

            $query = Query::updateSQL($data, "SCHREG_ENT_GRD_HIST_DAT", $where);
        }
        $db->query($query);

        //DB切断
        Query::dbCheckIn($db);
    }

    //SCHREG_ENT_GRD_HIST_DATデータ取得
    public function getEntGrdData($model, $schregno, $div)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_DAT T1 ";
        $query .= "     INNER JOIN NAME_MST L1 ON L1.NAMECD1     = 'A023' ";
        $query .= "                           AND T1.SCHOOL_KIND = L1.NAME1 ";
        $query .= " WHERE ";
        if ($div == "MAX") {
            $query .= "     T1.SCHREGNO = '".$schregno."' ";
            $query .= " ORDER BY ";
            $query .= "     L1.NAMECD2 DESC ";
        } else {
            $query .= "     T1.SCHREGNO = '".$schregno."' ";
            $query .= "     AND T1.SCHOOL_KIND = '". $model->schoolKind. "' ";
        }

        return $query;
    }

    //入学卒業履歴データ追加用データ取得
    public function getEntGrdIstData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     FINSCHOOLCD, ";
        $query .= "     FINISH_DATE, ";
        $query .= "     ENT_DATE, ";
        $query .= "     ENT_DIV, ";
        $query .= "     ENT_REASON, ";
        $query .= "     ENT_SCHOOL, ";
        $query .= "     ENT_ADDR ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "     , ENT_ADDR2 ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //削除
    public function &deleteTableQuery($model, $table, $fukugaku_date = "")
    {
        $query  = " DELETE FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        if ($fukugaku_date) {
            $query .= "     AND COMEBACK_DATE = '".$fukugaku_date."' ";
        }

        return $query;
    }

    //対象フィールド一覧取得
    public function getFieldQuery($model, $table)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //バックアップテーブル追加
    public function &insertComeBackTable($db, $model, $ins_table, $from_table, $field, $setField)
    {
        if ($from_table == "SCHREG_ENT_GRD_HIST_DAT") {
            //MAXデータ取得
            $entGrdData = $db->getRow(knji130Query::getEntGrdData($model, $model->schregno, "MAX"), DB_FETCHMODE_ASSOC);
        }

        $query  = " INSERT INTO $ins_table ({$setField}) ";
        $query .= " SELECT ";
        $query .= "     $field ";
        $query .= " FROM ";
        $query .= "     $from_table ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        if ($from_table == "SCHREG_ENT_GRD_HIST_DAT" && $entGrdData["SCHOOL_KIND"]) {
            $query .= " AND SCHOOL_KIND = '".$entGrdData["SCHOOL_KIND"]."' ";
        }

        return $query;
    }

    //V_NAME_MSTから情報を取得
    public function getNameMstData($cd)
    {
        $query  = " SELECT * FROM v_name_mst ";
        $query .= " WHERE namecd1 ='" .$cd. "' AND year='".CTRL_YEAR."'";
        $query .= " ORDER BY namecd1, namecd2";
        return $query;
    }
}
