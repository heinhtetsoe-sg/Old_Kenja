<?php
class knje371bQuery extends Query
{

    //学校名
    public function getSchoolName($schoolCd)
    {
        $query  = "    SELECT ";
        $query .= "      SCHOOL_NAME ";
        $query .= "    FROM ";
        $query .= "      COLLEGE_MST ";
        $query .= "    WHERE ";
        $query .= "      SCHOOL_CD = '{$schoolCd}' ";

        return $query;
    }
    //学部名
    public function getFacultyName($schoolCd, $facultyCd)
    {
        $query  = "    SELECT ";
        $query .= "      FACULTYNAME ";
        $query .= "    FROM ";
        $query .= "      COLLEGE_FACULTY_MST ";
        $query .= "    WHERE ";
        $query .= "      SCHOOL_CD = '{$schoolCd}' ";
        $query .= "      AND FACULTYCD = '{$facultyCd}' ";

        return $query;
    }
    //学校名
    public function getDepartmentName($schoolCd, $facultyCd, $departmentCd)
    {
        $query  = "    SELECT ";
        $query .= "      DEPARTMENTNAME ";
        $query .= "    FROM ";
        $query .= "      COLLEGE_DEPARTMENT_MST ";
        $query .= "    WHERE ";
        $query .= "      SCHOOL_CD = '{$schoolCd}' ";
        $query .= "      AND FACULTYCD = '{$facultyCd}' ";
        $query .= "      AND DEPARTMENTCD = '{$departmentCd}' ";

        return $query;
    }

    //学校・学部・学科名称取得
    public function getSchoolAllName($db, $model)
    {
        list($gamen, $cmdKind) = explode("_", $model->cmd);

        $query  = "    SELECT ";
        $query .= "      T2.SCHOOL_NAME, ";
        $query .= "      T3.FACULTYNAME, ";
        $query .= "      T1.DEPARTMENTCD, ";
        $query .= "      T4.DEPARTMENTNAME ";
        $query .= "    FROM ";
        $query .= "      COLLEGE_RECOMMEND_CONDITION_MAIN_DAT T1 ";
        $query .= "      LEFT JOIN COLLEGE_MST T2 ";
        $query .= "        ON T2.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "      LEFT JOIN COLLEGE_FACULTY_MST T3 ";
        $query .= "        ON T3.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "        AND T3.FACULTYCD = T1.FACULTYCD ";
        $query .= "      LEFT JOIN COLLEGE_DEPARTMENT_MST T4 ";
        $query .= "        ON T4.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "        AND T4.FACULTYCD = T1.FACULTYCD ";
        $query .= "        AND T4.DEPARTMENTCD = T1.DEPARTMENTCD ";
        $query .= "    WHERE ";
        $query .= "      T1.YEAR             = '{$model->year}' ";
        if (!isset($model->warning) && ($cmdKind == "" || $cmdKind == "edit" || $cmdKind == "reset")) {
            $query .= "      AND T1.SCHOOL_CD    =  '{$model->schoolCd}' ";
            $query .= "      AND T1.FACULTYCD    =  '{$model->facultyCd}' ";
            $query .= "      AND T1.DEPARTMENTCD =  '{$model->departmentCd}' ";
        } else {
            $query .= "      AND T1.SCHOOL_CD    =  '{$model->field["SCHOOL_CD"]}' ";
            $query .= "      AND T1.FACULTYCD    =  '{$model->field["FACULTYCD"]}' ";
            $query .= "      AND T1.DEPARTMENTCD =  '{$model->field["DEPARTMENTCD"]}' ";
        }
        $row = $db->getRow($query, DB_FECTHMODE_ASSOC);

        $row["DEPARTMENTNAME"] = ($row["DEPARTMENTCD"] == sprintf("%03d", "")) ? "(全学科)" : $row["DEPARTMENTNAME"];
        return $row["SCHOOL_NAME"]." ".$row["FACULTYNAME"]." ".$row["DEPARTMENTNAME"];
    }

    //学年コンボ
    public function getGrade($model, $execute_date)
    {
        $subclassCd = (!isset($model->warning) && $model->cmd == "subSubclass_edit") ? $model->subclassAllCd : $model->field["SUBCLASSCD"]; //リンクを押した場合と科目コンボを変えた場合で参照すべき変数が変わる

        $query  = "    WITH SUBCLASS_GRADE AS ( ";
        $query .= "      SELECT ";
        $query .= "        T1.YEAR, ";
        $query .= "        T1.GRADE ";
        $query .= "      FROM ";
        $query .= "        SCHREG_REGD_DAT T1 ";
        $query .= "      WHERE ";
        $query .= "        T1.YEAR = '{$model->year}' ";
        $query .= "        AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "        AND T1.SCHREGNO IN ( ";
        $query .= "          SELECT ";
        $query .= "            C1.SCHREGNO ";
        $query .= "          FROM ";
        $query .= "            CHAIR_STD_DAT C1 ";
        $query .= "            INNER JOIN CHAIR_DAT C2 ";
        $query .= "              ON C2.YEAR = C1.YEAR ";
        $query .= "              AND C2.SEMESTER = C1.SEMESTER ";
        $query .= "              AND C2.CHAIRCD = C1.CHAIRCD ";
        $query .= "          WHERE ";
        $query .= "            C1.YEAR = '{$model->year}' ";
        $query .= "            AND C1.SEMESTER = '{$model->semester}' ";
        $query .= "            AND '{$execute_date}' BETWEEN C1.APPDATE AND C1.APPENDDATE ";
        $query .= "            AND C2.CLASSCD || '-' || C2.SCHOOL_KIND || '-' || C2.CURRICULUM_CD || '-' || C2.SUBCLASSCD = '{$subclassCd}' ";
        $query .= "        ) ";
        $query .= "      GROUP BY ";
        $query .= "        T1.YEAR, ";
        $query .= "        T1.GRADE ";
        $query .= "    ) ";
        $query .= "    SELECT ";
        $query .= "      GDAT.GRADE AS VALUE, ";
        $query .= "      GDAT.GRADE_NAME1 AS LABEL ";
        $query .= "    FROM ";
        $query .= "      SUBCLASS_GRADE SUBG ";
        $query .= "      LEFT JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "        ON GDAT.YEAR = SUBG.YEAR ";
        $query .= "        AND GDAT.GRADE = SUBG.GRADE ";
        $query .= "    ORDER BY ";
        $query .= "      VALUE ";
        
        return $query;
    }

    //コースコード取得
    public function getCourseCode($model)
    {
        $query  = "    WITH MAIN_DATA AS ( ";
        $query .= "      SELECT ";
        $query .= "        T1.COURSECD, ";
        $query .= "        T1.MAJORCD, ";
        $query .= "        T1.COURSECODE ";
        $query .= "      FROM ";
        $query .= "        SCHREG_REGD_DAT T1 ";
        $query .= "      WHERE ";
        $query .= "        T1.YEAR = '{$model->year}' ";
        $query .= "    	   AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "      GROUP BY ";
        $query .= "        T1.COURSECD, ";
        $query .= "        T1.MAJORCD, ";
        $query .= "        T1.COURSECODE ";
        $query .= "    ) ";
        $query .= "    SELECT ";
        $query .= "      T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS VALUE, ";
        $query .= "      T1.COURSECD || T1.MAJORCD || T1.COURSECODE || ':' || T2.MAJORNAME || T3.COURSECODENAME AS LABEL ";
        $query .= "    FROM ";
        $query .= "      MAIN_DATA T1 ";
        $query .= "      LEFT JOIN MAJOR_MST T2 ";
        $query .= "        ON T2.COURSECD = T1.COURSECD ";
        $query .= "        AND T2.MAJORCD = T1.MAJORCD ";
        $query .= "      LEFT JOIN COURSECODE_MST T3 ";
        $query .= "        ON T3.COURSECODE = T1.COURSECODE ";
        $query .= "    ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目コンボ
    public function getSubclass($model)
    {
        $query  = "    SELECT ";
        $query .= "      CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        $query .= "      CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL ";
        $query .= "    FROM ";
        $query .= "      V_SUBCLASS_MST ";
        $query .= "    WHERE ";
        $query .= "      YEAR = '{$model->year}' ";
        $query .= "    ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //資格マスタ
    public function getQualifiedMst($Row)
    {
        $query  = " SELECT ";
        $query .= "     QUALIFIED_CD || '：' || QUALIFIED_NAME || '（' || VALUE(QUALIFIED_ABBV,'　') || '）' AS LABEL, ";
        $query .= "     QUALIFIED_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     CONDITION_DIV = '{$Row["CONDITION_DIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     INT(VALUE) ";

        return $query;
    }

    //資格マスタ(MANAGEMENT_FLG)
    public function getQualifiedMst_MFlg($Row)
    {
        $query  = " SELECT ";
        $query .= "     MANAGEMENT_FLG ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     QUALIFIED_CD = '{$Row["QUALIFIED_CD"]}' ";
        $query .= "     AND VALUE(SAKI_FLG, '0') <> '1' ";

        return $query;
    }

    //主催
    public function getPromoter($Row)
    {
        $query  = " SELECT ";
        $query .= "     PROMOTER ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     QUALIFIED_CD = '{$Row["QUALIFIED_CD"]}' ";

        return $query;
    }

    //級・段位
    public function getRank()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H312' ";

        return $query;
    }

    //級・段位(QUALIFIED_RESULT_MST)
    public function getRankResultMst($qualifiedCd, $model)
    {
        $query  = " SELECT ";
        $query .= "     RESULT_CD || ' ' || RESULT_NAME AS LABEL, ";
        $query .= "     RESULT_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_RESULT_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND QUALIFIED_CD = '{$qualifiedCd}' ";
        $query .= "     AND CERT_FLG     = 'T' ";//正式フラグが立っているもの

        return $query;
    }
    
    //選択した級・段位
    public function getSelectedRank($qualifiedCd, $model)
    {
        $query  = " SELECT ";
        $query .= "     NAME_MST.NAMECD2 AS VALUE, ";
        $query .= "     NAME_MST.NAMECD2 || ' ' || NAME_MST.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_RANK_DAT ";
        $query .= " INNER JOIN ";
        $query .= "     NAME_MST ";
        $query .= "     ON QUALIFIED_RANK_DAT.RANK = NAME_MST.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     QUALIFIED_CD='" . $qualifiedCd . "' ";
        $query .= "     AND NAME_MST.NAMECD1='H312' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象テーブルにNOT_PRINTがあるかどうか取得
    public function getSyscatColumns($tableName)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(COLNAME) ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = '".$tableName."' ";
        $query .= "     AND COLNAME = 'NOT_PRINT' ";

        return $query;
    }

    //
    public function getSubclassNum($model)
    {
        $query .= "  SELECT ";
        $query .= "      SUBCLASS_NUM ";
        $query .= "  FROM ";
        $query .= "      COLLEGE_RECOMMEND_CONDITION_MAIN_DAT ";
        $query .= "  WHERE ";
        $query .= "      YEAR             = '".CTRL_YEAR."' ";
        $query .= "      AND SCHOOL_CD    =  '{$model->schoolCd}' ";
        $query .= "      AND FACULTYCD    =  '{$model->facultyCd}' ";
        $query .= "      AND DEPARTMENTCD =  '{$model->departmentCd}' ";

        return $query;
    }

    /*********データ操作(メイン)**********/

    //データ一覧取得(メイン)
    public function getListMain($model, $year, $div)
    {
        $query  = "    SELECT ";
        $query .= "      T1.SCHOOL_CD, ";
        $query .= "      T2.SCHOOL_NAME, ";
        $query .= "      T1.FACULTYCD, ";
        $query .= "      T3.FACULTYNAME, ";
        $query .= "      T1.DEPARTMENTCD, ";
        $query .= "      T4.DEPARTMENTNAME, ";
        $query .= "      T1.COURSE_CONDITION_FLG, ";
        $query .= "      T1.SUBCLASS_CONDITION_FLG, ";
        $query .= "      T1.QUALIFIED_CONDITION_FLG, ";
        $query .= "      T1.SUBCLASS_NUM ";
        $query .= "    FROM ";
        $query .= "      COLLEGE_RECOMMEND_CONDITION_MAIN_DAT T1 ";
        $query .= "      LEFT JOIN COLLEGE_MST T2 ";
        $query .= "        ON T2.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "      LEFT JOIN COLLEGE_FACULTY_MST T3 ";
        $query .= "        ON T3.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "        AND T3.FACULTYCD = T1.FACULTYCD ";
        $query .= "      LEFT JOIN COLLEGE_DEPARTMENT_MST T4 ";
        $query .= "        ON T4.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "        AND T4.FACULTYCD = T1.FACULTYCD ";
        $query .= "        AND T4.DEPARTMENTCD = T1.DEPARTMENTCD ";
        $query .= "    WHERE ";
        $query .= "      T1.YEAR             = '{$year}' ";
        if ($div == "link") {
            $query .= "      AND T1.SCHOOL_CD    =  '{$model->schoolCd}' ";
            $query .= "      AND T1.FACULTYCD    =  '{$model->facultyCd}' ";
            $query .= "      AND T1.DEPARTMENTCD =  '{$model->departmentCd}' ";
        } elseif ($div == "validate") {
            $query .= "      AND T1.SCHOOL_CD    =  '{$model->field["SCHOOL_CD"]}' ";
            $query .= "      AND T1.FACULTYCD    =  '{$model->field["FACULTYCD"]}' ";
            $query .= "      AND T1.DEPARTMENTCD =  '{$model->field["DEPARTMENTCD"]}' ";
        }
        $query .= "    ORDER BY ";
        $query .= "      T1.SCHOOL_CD, ";
        $query .= "      T1.FACULTYCD, ";
        $query .= "      T1.DEPARTMENTCD ";

        return $query;
    }

    //削除処理(メイン)
    public function getDeleteQueryMain($fields)
    {
        $query  = "    DELETE ";
        $query .= "    FROM ";
        $query .= "      COLLEGE_RECOMMEND_CONDITION_MAIN_DAT ";
        $query .= "    WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND SCHOOL_CD      = '{$fields["SCHOOL_CD"]}' ";
        $query .= "      AND FACULTYCD      = '{$fields["FACULTYCD"]}' ";
        $query .= "      AND DEPARTMENTCD   = '{$fields["DEPARTMENTCD"]}' ";

        return $query;
    }

    //追加処理(メイン)
    public function &getInsertQueryMain($fields)
    {
        $data["YEAR"][TEXT]                     = CTRL_YEAR;
        $data["SCHOOL_CD"][TEXT]                = $fields["SCHOOL_CD"];
        $data["FACULTYCD"][TEXT]                = $fields["FACULTYCD"];
        $data["DEPARTMENTCD"][TEXT]             = sprintf("%03d", $fields["DEPARTMENTCD"]); //全学科対象の要件にはALLゼロを設定
        $data["COURSE_CONDITION_FLG"][TEXT]     = $fields["COURSE_CONDITION_FLG"];
        $data["SUBCLASS_CONDITION_FLG"][TEXT]   = $fields["SUBCLASS_CONDITION_FLG"];
        $data["QUALIFIED_CONDITION_FLG"][TEXT]  = $fields["QUALIFIED_CONDITION_FLG"];
        $data["SUBCLASS_NUM"][TEXT]             = $fields["SUBCLASS_NUM"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";
        $query = Query::insertSQL($data, "COLLEGE_RECOMMEND_CONDITION_MAIN_DAT");

        return $query;
    }

    //更新処理(メイン)
    public function &getUpdateQueryMain($fields)
    {
        $data["SUBCLASS_NUM"][TEXT]             = $fields["SUBCLASS_NUM"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";

        $where .= " WHERE ";
        $where .= "   YEAR             =  '". CTRL_YEAR ."' ";
        $where .= "   AND SCHOOL_CD    =  '{$fields["SCHOOL_CD"]}' ";
        $where .= "   AND FACULTYCD    =  '{$fields["FACULTYCD"]}' ";
        $where .= "   AND DEPARTMENTCD =  '{$fields["DEPARTMENTCD"]}' ";

        $query = Query::updateSQL($data, "COLLEGE_RECOMMEND_CONDITION_MAIN_DAT", $where);

        return $query;
    }

    /*********データ操作(2.コース)**********/

    //データ一覧(コース)
    public function getList2($model, $div)
    {
        $query  = "    SELECT ";
        $query .= "      SCHOOL_CD, ";
        $query .= "      FACULTYCD, ";
        $query .= "      DEPARTMENTCD, ";
        $query .= "      COURSECD || MAJORCD || COURSECODE AS COURSECODE ";
        $query .= "    FROM ";
        $query .= "      COLLEGE_RECOMMEND_CONDITION_COURSE_DAT ";
        $query .= "    WHERE ";
        $query .= "      YEAR             = '".CTRL_YEAR."' ";
        $query .= "      AND SCHOOL_CD    =  '{$model->schoolCd}' ";
        $query .= "      AND FACULTYCD    =  '{$model->facultyCd}' ";
        $query .= "      AND DEPARTMENTCD =  '{$model->departmentCd}' ";
        $query .= "    ORDER BY ";
        $query .= "      SCHOOL_CD, ";
        $query .= "      FACULTYCD, ";
        $query .= "      DEPARTMENTCD, ";
        $query .= "      COURSECD, ";
        $query .= "      MAJORCD, ";
        $query .= "      COURSECODE ";

        return $query;
    }

    //削除処理(コース)
    public function getDeleteQuery2($fields)
    {
        $query  = "    DELETE ";
        $query .= "    FROM ";
        $query .= "      COLLEGE_RECOMMEND_CONDITION_COURSE_DAT ";
        $query .= "    WHERE ";
        $query .= "      YEAR               = '".CTRL_YEAR."' ";
        $query .= "      AND SCHOOL_CD      = '{$fields["SCHOOL_CD"]}' ";
        $query .= "      AND FACULTYCD      = '{$fields["FACULTYCD"]}' ";
        $query .= "      AND DEPARTMENTCD   = '{$fields["DEPARTMENTCD"]}' ";
        //$query .= "      AND COURSECD || MAJORCD || COURSECODE = '{$fields["COURSECODE"]}' "; //現時点では登録するコースは1つのみなので、更新時にコースをすべて削除しておく

        return $query;
    }

    //追加処理(コース)
    public function &getInsertQuery2($fields)
    {
        $coursecd   = substr($fields["COURSECODE"], 0, 1);
        $majorcd    = substr($fields["COURSECODE"], 1, 3);
        $coursecode = substr($fields["COURSECODE"], 4, 4);

        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SCHOOL_CD"][TEXT]            = $fields["SCHOOL_CD"];
        $data["FACULTYCD"][TEXT]            = $fields["FACULTYCD"];
        $data["DEPARTMENTCD"][TEXT]         = $fields["DEPARTMENTCD"];
        $data["COURSECD"][TEXT]             = $coursecd;
        $data["MAJORCD"][TEXT]              = $majorcd;
        $data["COURSECODE"][TEXT]           = $coursecode;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "COLLEGE_RECOMMEND_CONDITION_COURSE_DAT");
        return $query;
    }

    /*********データ操作(3.科目)**********/

    //データ一覧(科目)
    public function getList3($model, $div, $subclassCd = "")
    {
        $query  = "    SELECT ";
        $query .= "      T1.SCHOOL_CD, ";
        $query .= "      T1.FACULTYCD, ";
        $query .= "      T1.DEPARTMENTCD, ";
        $query .= "      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "      T1.GRADE1_FLG, ";
        $query .= "      T1.GRADE2_FLG, ";
        $query .= "      T1.GRADE3_FLG, ";
        $query .= "      T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSECODE2, ";
        $query .= "      T1.REQUIRED_FLG, ";
        $query .= "      T1.TRANSFER_FLG, ";
        $query .= "      T1.COMEBACK_FLG, ";
        $query .= "      T2.SUBCLASSNAME, ";
        $query .= "      T3.MAJORNAME || T4.COURSECODENAME AS COURSECODENAME ";
        $query .= "    FROM COLLEGE_RECOMMEND_CONDITION_SUBCLASS_DAT T1 ";
        $query .= "    LEFT JOIN V_SUBCLASS_MST T2 ";
        $query .= "      ON  T2.YEAR           = T1.YEAR ";
        $query .= "      AND T2.CLASSCD        = T1.CLASSCD ";
        $query .= "      AND T2.SCHOOL_KIND    = T1.SCHOOL_KIND ";
        $query .= "      AND T2.CURRICULUM_CD  = T1.CURRICULUM_CD ";
        $query .= "      AND T2.SUBCLASSCD     = T1.SUBCLASSCD ";
        $query .= "    LEFT JOIN MAJOR_MST T3 ";
        $query .= "      ON  T3.COURSECD        = T1.COURSECD ";
        $query .= "      AND T3.MAJORCD         = T1.MAJORCD ";
        $query .= "    LEFT JOIN COURSECODE_MST T4 ";
        $query .= "      ON T4.COURSECODE       = T1.COURSECODE ";
        $query .= "    WHERE ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T1.SCHOOL_CD    =  '{$model->schoolCd}' ";
        $query .= "      AND T1.FACULTYCD    =  '{$model->facultyCd}' ";
        $query .= "      AND T1.DEPARTMENTCD =  '{$model->departmentCd}' ";
        if ($div != "list") {
            $query .= "      AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD =  '{$subclassCd}' ";
        }
        $query .= "    ORDER BY ";
        $query .= "      T1.SCHOOL_CD, ";
        $query .= "      T1.FACULTYCD, ";
        $query .= "      T1.DEPARTMENTCD, ";
        $query .= "      T1.CLASSCD, ";
        $query .= "      T1.SCHOOL_KIND, ";
        $query .= "      T1.CURRICULUM_CD, ";
        $query .= "      T1.SUBCLASSCD ";

        return $query;
    }

    //削除処理(科目)
    public function getDeleteQuery3($fields)
    {
        $query  = "    DELETE ";
        $query .= "    FROM ";
        $query .= "      COLLEGE_RECOMMEND_CONDITION_SUBCLASS_DAT ";
        $query .= "    WHERE ";
        $query .= "      YEAR               = '".CTRL_YEAR."' ";
        $query .= "      AND SCHOOL_CD      = '{$fields["SCHOOL_CD"]}' ";
        $query .= "      AND FACULTYCD      = '{$fields["FACULTYCD"]}' ";
        $query .= "      AND DEPARTMENTCD   = '{$fields["DEPARTMENTCD"]}' ";
        $query .= "      AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD  = '{$fields["SUBCLASSCD"]}' ";

        return $query;
    }

    //追加処理(科目)
    public function &getInsertQuery3($fields)
    {
        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $fields["SUBCLASSCD"]);
        $coursecd   = substr($fields["COURSECODE2"], 0, 1);
        $majorcd    = substr($fields["COURSECODE2"], 1, 3);
        $coursecode = substr($fields["COURSECODE2"], 4, 4);

        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SCHOOL_CD"][TEXT]            = $fields["SCHOOL_CD"];
        $data["FACULTYCD"][TEXT]            = $fields["FACULTYCD"];
        $data["DEPARTMENTCD"][TEXT]         = $fields["DEPARTMENTCD"];
        $data["CLASSCD"][TEXT]              = $classCd;
        $data["SCHOOL_KIND"][TEXT]          = $schoolKind;
        $data["CURRICULUM_CD"][TEXT]        = $curriculumCd;
        $data["SUBCLASSCD"][TEXT]           = $subclassCd;
        $data["GRADE1_FLG"][TEXT]           = $fields["GRADE1_FLG"];
        $data["GRADE2_FLG"][TEXT]           = $fields["GRADE2_FLG"];
        $data["GRADE3_FLG"][TEXT]           = $fields["GRADE3_FLG"];
        $data["COURSECD"][TEXT]             = $coursecd;
        $data["MAJORCD"][TEXT]              = $majorcd;
        $data["COURSECODE"][TEXT]           = $coursecode;
        $data["REQUIRED_FLG"][TEXT]         = $fields["REQUIRED_FLG"];
        $data["TRANSFER_FLG"][TEXT]         = $fields["TRANSFER_FLG"];
        $data["COMEBACK_FLG"][TEXT]         = $fields["COMEBACK_FLG"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "COLLEGE_RECOMMEND_CONDITION_SUBCLASS_DAT");

        return $query;
    }

    /*********データ操作(資格)**********/

    //データ一覧(資格)
    public function getList4($model, $div)
    {
        $query  = "  SELECT ";
        $query .= "    T1.SCHOOL_CD, ";
        $query .= "    T1.FACULTYCD, ";
        $query .= "    T1.DEPARTMENTCD, ";
        $query .= "    T1.QUALIFIED_CD, ";
        $query .= "    T1.VALID_S_DATE, ";
        $query .= "    T1.CONDITION_RANK, ";
        $query .= "    T1.CONDITION_SCORE, ";
        $query .= "    T2.CONDITION_DIV, ";
        $query .= "    T2.QUALIFIED_NAME, ";
        $query .= "    T2.PROMOTER, ";
        $query .= "    T3.NAME1 AS RANK_NAME ";
        $query .= "  FROM COLLEGE_RECOMMEND_CONDITION_QUALIFIED_DAT T1 ";
        $query .= "  LEFT JOIN QUALIFIED_MST T2 ";
        $query .= "    ON T2.QUALIFIED_CD = T1.QUALIFIED_CD ";
        $query .= "  LEFT JOIN NAME_MST T3 ";
        $query .= "    ON  T3.NAMECD1 = 'H312' ";
        $query .= "    AND T3.NAMECD2 = T1.CONDITION_RANK ";
        $query .= "  WHERE ";
        $query .= "    T1.YEAR  = '".CTRL_YEAR."' ";
        $query .= "      AND T1.SCHOOL_CD    =  '{$model->schoolCd}' ";
        $query .= "      AND T1.FACULTYCD    =  '{$model->facultyCd}' ";
        $query .= "      AND T1.DEPARTMENTCD =  '{$model->departmentCd}' ";
        if ($div == "link") {
            $query .= "      AND T1.QUALIFIED_CD =  '{$model->qualifiedCd}' ";
        } elseif ($div == "validate") {
            $query .= "      AND T1.QUALIFIED_CD =  '{$model->field["QUALIFIED_CD"]}' ";
        }
        $query .= "  ORDER BY ";
        $query .= "    T1.SCHOOL_CD, ";
        $query .= "    T1.FACULTYCD, ";
        $query .= "    T1.DEPARTMENTCD, ";
        $query .= "    T1.QUALIFIED_CD ";

        return $query;
    }

    //削除処理(資格)
    public function getDeleteQuery4($fields)
    {
        $query  = "    DELETE ";
        $query .= "    FROM ";
        $query .= "      COLLEGE_RECOMMEND_CONDITION_QUALIFIED_DAT ";
        $query .= "    WHERE ";
        $query .= "      YEAR               = '".CTRL_YEAR."' ";
        $query .= "      AND SCHOOL_CD      = '{$fields["SCHOOL_CD"]}' ";
        $query .= "      AND FACULTYCD      = '{$fields["FACULTYCD"]}' ";
        $query .= "      AND DEPARTMENTCD   = '{$fields["DEPARTMENTCD"]}' ";
        $query .= "      AND QUALIFIED_CD   = '{$fields["QUALIFIED_CD"]}' ";

        return $query;
    }

    //追加処理(資格)
    public function &getInsertQuery4($fields)
    {
        $data["YEAR"][TEXT]                     = CTRL_YEAR;
        $data["SCHOOL_CD"][TEXT]                = $fields["SCHOOL_CD"];
        $data["FACULTYCD"][TEXT]                = $fields["FACULTYCD"];
        $data["DEPARTMENTCD"][TEXT]             = $fields["DEPARTMENTCD"];
        $data["QUALIFIED_CD"][TEXT]             = $fields["QUALIFIED_CD"];
        $data["VALID_S_DATE"][DATE]             = $fields["VALID_S_DATE"];
        $data["CONDITION_RANK"][TEXT]           = $fields["CONDITION_RANK"];
        $data["CONDITION_SCORE"][TEXT]          = $fields["CONDITION_SCORE"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";
        $query = Query::insertSQL($data, "COLLEGE_RECOMMEND_CONDITION_QUALIFIED_DAT");

        return $query;
    }

    /***コピー処理***/
    public function getColmun($db, $table)
    {
        $query  = "    SELECT ";
        $query .= "      COLUMN_NAME ";
        $query .= "    FROM ";
        $query .= "      SYSIBM.COLUMNS ";
        $query .= "    WHERE ";
        $query .= "      TABLE_NAME = '{$table}' ";
        $query .= "    ORDER BY ";
        $query .= "      ORDINAL_POSITION ";
        $result = $db->query($query);
        $columns = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $columns[] = $row["COLUMN_NAME"];
        }

        return $columns;
    }
}
