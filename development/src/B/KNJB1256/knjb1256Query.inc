<?php

require_once('for_php7.php');

class knjb1256Query extends Query {

    //年度を取得
    function getYear($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //履修履歴が含まれる履歴コードを取得
    function getNotNullRirekiCode($model)
    {
        $query  = "WITH STD_RIREKI_STD AS ( ";
        $query .= "SELECT ";
        $query .= "    RIREKI_CODE, ";
        $query .= "    COUNT(*) AS CNT ";
        $query .= "FROM ";
        $query .= "    SUBCLASS_STD_SELECT_RIREKI_DAT ";
        $query .= "WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "GROUP BY ";
        $query .= "    RIREKI_CODE ";
        $query .= "), PATTERN_STD AS ( ";
        $query .= "SELECT ";
        $query .= "    RIREKI_CODE, ";
        $query .= "    COUNT(*) AS CNT ";
        $query .= "FROM ";
        $query .= "    COMP_CREDITS_PATTERN_STD_COURSE_DAT ";
        $query .= "WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "GROUP BY ";
        $query .= "    RIREKI_CODE ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    T1.RIREKI_CODE ";
        $query .= "FROM ";
        $query .= "    STD_RIREKI_STD T1, ";
        $query .= "    PATTERN_STD T2 ";
        $query .= "WHERE ";
        $query .= "    T1.RIREKI_CODE = T2.RIREKI_CODE ";

        return $query;
    }

    //履修履歴
    function getRirekiCode($model, $flg="", $rirekicd="")
    {
        $rirekiArray = array();
        $rirekiArray = explode(",", $rirekicd);

        $query  = " SELECT ";
        $query .= "     RIREKI_CODE AS VALUE, ";
        $query .= "     SELECT_NAME || ' ' || CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/')) AS LABEL ";
        $query .= " FROM ";
        $query .= "     STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        if ($flg) {
            if ($model->field["RIREKI_CODE"] != "") {
                if (get_count($rirekiArray) > 0) {
                    $query .= " AND RIREKI_CODE IN ( ";
                    $conma = "";
                    for ($i =0; $i < get_count($rirekiArray); $i++) {
                        $query .= "  $conma'{$rirekiArray[$i]}' ";
                        $conma = ',';
                    }
                    $query .= " ) ";
                }
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース
    function getCourseCode($model) {

        $query  = " SELECT DISTINCT ";
        $query .= "     T5.GRADE_NAME1 || '(' || T4.COURSECD || T4.MAJORCD || ') ' || T2.COURSENAME || T3.MAJORNAME || ' (' || T4.COURSECODE || ') ' || T1.COURSECODENAME AS LABEL, ";
        $query .= "     T4.GRADE || '-' || T4.COURSECD || '-' || T4.MAJORCD || '-' || T4.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSECODE_MST T1, ";
        $query .= "     COURSE_MST T2, ";
        $query .= "     MAJOR_MST T3, ";
        $query .= "     SCHREG_REGD_DAT T4, ";
        $query .= "     SCHREG_REGD_GDAT T5 ";
        $query .= " WHERE ";
        $query .= "     T4.YEAR       ='{$model->field["YEAR"]}' AND ";
        $query .= "     T1.COURSECODE = T4.COURSECODE AND ";
        $query .= "     T2.COURSECD   = T4.COURSECD AND ";
        $query .= "     T3.COURSECD   = T4.COURSECD AND ";
        $query .= "     T4.MAJORCD    = T3.MAJORCD AND ";
        $query .= "     T4.YEAR       = T5.YEAR AND ";
        $query .= "     T4.GRADE      = T5.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T5.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T5.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //履修パターン
    function getPattern($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.PATTERN_CD AS VALUE, ";
        $query .= "     T1.PATTERN_CD || ':' || T1.PATTERN_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '{$model->field["COURSECODE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス
    function getHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.HR_CLASS AS VALUE, ";
        $query .= "     L1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "          AND T1.GRADE = L1.GRADE ";
        $query .= "          AND T1.HR_CLASS = L1.HR_CLASS ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON L1.YEAR = REG_G.YEAR ";
                $query .= "           AND L1.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON L1.YEAR = REG_G.YEAR ";
            $query .= "           AND L1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '{$model->field["COURSECODE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒情報
    function getSchInfo($model, $schregNo) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     MAX(L1.HR_NAME) || '　' || MAX(T1.ATTENDNO) || '番　' || BASE.NAME || '(' || T1.SCHREGNO || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "          AND T1.GRADE = L1.GRADE ";
        $query .= "          AND T1.HR_CLASS = L1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '{$model->field["COURSECODE"]}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     BASE.NAME ";
        $query .= " ORDER BY ";
        $query .= "     LABEL ";

        return $query;
    }

    //左側生徒
    function getCompCreditsPatternStdCourseDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     MAX(L1.HR_NAME) || '　' || MAX(T1.ATTENDNO) || '番　' || BASE.NAME || '(' || T1.SCHREGNO || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "          AND T1.GRADE = L1.GRADE ";
        $query .= "          AND T1.HR_CLASS = L1.HR_CLASS ";
        $query .= "     INNER JOIN COMP_CREDITS_PATTERN_STD_COURSE_DAT I1 ON T1.YEAR = I1.YEAR ";
        $query .= "          AND I1.RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        $query .= "          AND I1.PATTERN_CD = '{$model->field["PATTERN_CD"]}' ";
        $query .= "          AND T1.GRADE = I1.GRADE ";
        $query .= "          AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = I1.COURSECD || I1.MAJORCD || I1.COURSECODE ";
        $query .= "          AND T1.SCHREGNO = I1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '{$model->field["COURSECODE"]}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     BASE.NAME ";
        $query .= " ORDER BY ";
        $query .= "     LABEL ";

        return $query;
    }

    //右側生徒
    function getStd($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     MAX(L1.HR_NAME) || '　' || MAX(T1.ATTENDNO) || '番　' || BASE.NAME || '(' || T1.SCHREGNO || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "          AND T1.GRADE = L1.GRADE ";
        $query .= "          AND T1.HR_CLASS = L1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        $query .= "     AND T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '{$model->field["COURSECODE"]}' ";
        $query .= "     AND NOT EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             COMP_CREDITS_PATTERN_STD_COURSE_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = E1.YEAR ";
        $query .= "             AND E1.RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "         ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     BASE.NAME ";
        $query .= " ORDER BY ";
        $query .= "     LABEL ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($db, $model) {

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_STD_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        $query .= "     AND PATTERN_CD = '{$model->field["PATTERN_CD"]}' ";
        $query .= "     AND GRADE || '-' || COURSECD || '-' || MAJORCD || '-' || COURSECODE = '{$model->field["COURSECODE"]}' ";
        $db->query($query);

        //生徒データをInsert
        if (get_count($model->selectStd) > 0) {
            list($grade, $coursecd, $majorcd, $coursecode) = preg_split("/-/", $model->field["COURSECODE"]);
            foreach ($model->selectStd as $schregNo) {
                $data = array();
                $data["YEAR"][TEXT]         = $model->field["YEAR"];
                $data["RIREKI_CODE"][TEXT]  = $model->field["RIREKI_CODE"];
                $data["PATTERN_CD"][TEXT]   = $model->field["PATTERN_CD"];
                $data["GRADE"][TEXT]        = $grade;
                $data["COURSECD"][TEXT]     = $coursecd;
                $data["MAJORCD"][TEXT]      = $majorcd;
                $data["COURSECODE"][TEXT]   = $coursecode;
                $data["SCHREGNO"][TEXT]     = $schregNo;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";
                $query = Query::insertSQL($data, "COMP_CREDITS_PATTERN_STD_COURSE_DAT");
                $db->query($query);
            }
        }

    }

    //学期マスタ
    function getSeme($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    /**
     * 削除 SUBCLASS_STD_SELECT_RIREKI_DAT
     */
    function &deleteSubclassStd($model, $schregNo)
    {

        $query  = "DELETE FROM ";
        $query .= "    SUBCLASS_STD_SELECT_RIREKI_DAT ";
        $query .= "WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    //科目
    function getSubclassCd($model, $schregNo)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.PATTERN_CD = '{$model->field["PATTERN_CD"]}' ";
        $query .= "     AND T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '{$model->field["COURSECODE"]}' ";
        $query .= "     AND T1.COMP_FLG = '1' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             COMP_CREDITS_PATTERN_STD_COURSE_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = E1.YEAR ";
        $query .= "             AND E1.RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        $query .= "             AND T1.PATTERN_CD = E1.PATTERN_CD ";
        $query .= "             AND T1.GRADE = E1.GRADE ";
        $query .= "             AND T1.COURSECD = E1.COURSECD ";
        $query .= "             AND T1.MAJORCD = E1.MAJORCD ";
        $query .= "             AND T1.COURSECODE = E1.COURSECODE ";
        $query .= "             AND E1.SCHREGNO = '{$schregNo}' ";
        $query .= "         ) ";

        return $query;
    }

    /**
     * 新規 SUBCLASS_STD_SELECT_RIREKI_DAT
     */
    function &insertSubclassStd($db, $model, $semeData, $schregNo, $subclassInfo)
    {
        $cmpCntSql = knjb1256Query::getStdSelectCnt($model, $semeData, $subclassInfo, $schregNo);
        $cmpCnt = $db->getOne($cmpCntSql);
        $sentakuSql = knjb1256Query::getSentakuKamoku($model, $subclassInfo);
        $sentakuCnt = $db->getOne($sentakuSql);

        if ($cmpCnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->field["YEAR"]}' ";
            $where .= "     AND SEMESTER = '{$semeData["SEMESTER"]}' ";
            $where .= "     AND RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
            $where .= "     AND CLASSCD = '{$subclassInfo["CLASSCD"]}' ";
            $where .= "     AND SCHOOL_KIND = '{$subclassInfo["SCHOOL_KIND"]}' ";
            $where .= "     AND CURRICULUM_CD = '{$subclassInfo["CURRICULUM_CD"]}' ";
            $where .= "     AND SUBCLASSCD = '{$subclassInfo["SUBCLASSCD"]}' ";
            $where .= "     AND SCHREGNO = '{$schregNo}' ";

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::updateSQL($data, "SUBCLASS_STD_SELECT_RIREKI_DAT", $where);
        } else {

            $groupSql = knjb1256Query::getGroupCd($model, $semeData, $subclassInfo, $sentakuCnt, $schregNo);
            $setGroup = $db->getOne($groupSql);

            //最大値
            if (!$setGroup) {
                $groupSql = knjb1256Query::getGroupCdMax($model, $semeData, $subclassInfo, $sentakuCnt, $schregNo);
                $setGroup = $db->getOne($groupSql);

                if ($setGroup) {
                    $setGroup = (int)$setGroup + 1;
                } else if ($sentakuCnt == "1") {
                    $setGroup = "001";
                } else {
                    $setGroup = "600";
                }

                $setGroup = sprintf("%03d", $setGroup);
                //SUBCLASS_COMP_SELECT_DAT追加
                $query = knjb1256Query::insertAutoSubclassCompSelectDat($model, $semeData, $subclassInfo, $setGroup);
                $db->query($query);
                //SUBCLASS_COMP_SELECT_MST追加
                $query = knjb1256Query::insertAutoSubclassCompSelectMst($model, $semeData, $subclassInfo, $setGroup);
                $db->query($query);
            }

            if ($setGroup) {
                $data["YEAR"][TEXT]             = $model->field["YEAR"];
                $data["SEMESTER"][TEXT]         = $semeData["SEMESTER"];
                $data["RIREKI_CODE"][TEXT]      = $model->field["RIREKI_CODE"];
                $data["GROUPCD"][TEXT]          = $setGroup;
                $data["CLASSCD"][TEXT]          = $subclassInfo["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]      = $subclassInfo["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]    = $subclassInfo["CURRICULUM_CD"];
                $data["SUBCLASSCD"][TEXT]       = $subclassInfo["SUBCLASSCD"];
                $data["SCHREGNO"][TEXT]         = $schregNo;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "SUBCLASS_STD_SELECT_RIREKI_DAT");
            }
        }

        return $query;
    }

    /**
     * SCHREG_REGD_DATのカウント
     */
    function getRegdCnt($model, $semeData, $schregNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.SEMESTER = '{$semeData["SEMESTER"]}' ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    /**
     * SUBCLASS_STD_SELECT_RIREKI_DATのカウント
     */
    function getStdSelectCnt($model, $semeData, $subclassInfo, $schregNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.SEMESTER = '{$semeData["SEMESTER"]}' ";
        $query .= "     AND T1.RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        $query .= "     AND T1.CLASSCD = '{$subclassInfo["CLASSCD"]}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$subclassInfo["SCHOOL_KIND"]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclassInfo["CURRICULUM_CD"]}' ";
        $query .= "     AND T1.SUBCLASSCD = '{$subclassInfo["SUBCLASSCD"]}' ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    /**
     * 選択科目CREDIT_MST
     */
    function getSentakuKamoku($model, $subclassInfo)
    {
        list($grade, $coursecd, $majorcd, $coursecode) = preg_split("/-/", $model->field["COURSECODE"]);
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND COURSECD = '{$coursecd}' ";
        $query .= "     AND MAJORCD = '{$majorcd}' ";
        $query .= "     AND GRADE = '{$grade}' ";
        $query .= "     AND COURSECODE = '{$coursecode}' ";
        $query .= "     AND CLASSCD = '{$subclassInfo["CLASSCD"]}' ";
        $query .= "     AND SCHOOL_KIND = '{$subclassInfo["SCHOOL_KIND"]}' ";
        $query .= "     AND CURRICULUM_CD = '{$subclassInfo["CURRICULUM_CD"]}' ";
        $query .= "     AND SUBCLASSCD = '{$subclassInfo["SUBCLASSCD"]}' ";
        $query .= "     AND REQUIRE_FLG = '3' ";
        return $query;
    }

    /**
     * SUBCLASS_COMP_SELECT_DAT.MAX(GROUPCD)取得
     */
    function getGroupCdMax($model, $semeData, $subclassInfo, $sentakuCnt, $schregNo)
    {
        $query  = " SELECT ";
        $query .= "     MAX(T1.GROUPCD) AS GROUPCD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SUBCLASS_COMP_SELECT_MST T1 ON REGD.YEAR = T1.YEAR ";
        $query .= "           AND REGD.GRADE = T1.GRADE ";
        $query .= "           AND REGD.COURSECD = T1.COURSECD ";
        $query .= "           AND REGD.MAJORCD = T1.MAJORCD ";
        $query .= "           AND REGD.COURSECODE = T1.COURSECODE ";
        if ($sentakuCnt == "1") {
            $query .= "           AND T1.GROUPCD < '600' ";
        } else {
            $query .= "           AND T1.GROUPCD >= '600'  ";
        }
        $query .= " WHERE ";
        $query .= "     REGD.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND REGD.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND REGD.SEMESTER = '{$semeData["SEMESTER"]}' ";

        return $query;
    }

    /**
     * SUBCLASS_COMP_SELECT_DAT.GROUPCD取得
     */
    function getGroupCd($model, $semeData, $subclassInfo, $sentakuCnt, $schregNo)
    {
        list($grade, $coursecd, $majorcd, $coursecode) = preg_split("/-/", $model->field["COURSECODE"]);
        $query  = " SELECT ";
        $query .= "     MIN(T1.GROUPCD) AS GROUPCD ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.GRADE = '{$grade}' ";
        $query .= "     AND T1.COURSECD = '{$coursecd}' ";
        $query .= "     AND T1.MAJORCD = '{$majorcd}' ";
        $query .= "     AND T1.COURSECODE = '{$coursecode}' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$subclassInfo["CLASSCD"]."-".$subclassInfo["SCHOOL_KIND"]."-".$subclassInfo["CURRICULUM_CD"]."-".$subclassInfo["SUBCLASSCD"]."' ";
        if ($sentakuCnt == "1") {
            $query .= "       AND T1.GROUPCD < '600' ";
        } else {
            $query .= "       AND T1.GROUPCD >= '600'  ";
        }

        return $query;
    }

    //SUBCLASS_COMP_SELECT_DAT追加（600以上のGROUPCDを新規追加）
    function insertAutoSubclassCompSelectDat($model, $semeData, $subclassInfo, $setGroup) {

        list($grade, $coursecd, $majorcd, $coursecode) = preg_split("/-/", $model->field["COURSECODE"]);
        $auto_data = array();
        $auto_data["YEAR"][TEXT]            = $model->field["YEAR"];
        $auto_data["GRADE"][TEXT]           = $grade;
        $auto_data["COURSECD"][TEXT]        = $coursecd;
        $auto_data["MAJORCD"][TEXT]         = $majorcd;
        $auto_data["COURSECODE"][TEXT]      = $coursecode;
        $auto_data["GROUPCD"][TEXT]         = $setGroup;
        $auto_data["CLASSCD"][TEXT]         = $subclassInfo["CLASSCD"];
        $auto_data["SCHOOL_KIND"][TEXT]     = $subclassInfo["SCHOOL_KIND"];
        $auto_data["CURRICULUM_CD"][TEXT]   = $subclassInfo["CURRICULUM_CD"];
        $auto_data["SUBCLASSCD"][TEXT]      = $subclassInfo["SUBCLASSCD"];
        $auto_data["REGISTERCD"][TEXT]      = STAFFCD;
        $auto_data["UPDATED"][NUMBER]       = "sysdate()";

        $query = Query::insertSQL($auto_data, "SUBCLASS_COMP_SELECT_DAT");
        return $query;
    }

    //SUBCLASS_COMP_SELECT_DAT追加（600以上のGROUPCDを新規追加）
    function insertAutoSubclassCompSelectMst($model, $semeData, $addvalue, $setGroup) {

        list($grade, $coursecd, $majorcd, $coursecode) = preg_split("/-/", $model->field["COURSECODE"]);
        $auto_data = array();
        $auto_data["YEAR"][TEXT]            = $model->field["YEAR"];
        $auto_data["GRADE"][TEXT]           = $grade;
        $auto_data["COURSECD"][TEXT]        = $coursecd;
        $auto_data["MAJORCD"][TEXT]         = $majorcd;
        $auto_data["COURSECODE"][TEXT]      = $coursecode;
        $auto_data["GROUPCD"][TEXT]         = $setGroup;
        $auto_data["NAME"][TEXT]            = $setGroup;
        $auto_data["ABBV"][TEXT]            = $setGroup;
        $auto_data["JOUGEN"][NUMBER]        = 1;
        $auto_data["KAGEN"][NUMBER]         = 1;
        $auto_data["REGISTERCD"][TEXT]      = STAFFCD;
        $auto_data["UPDATED"][NUMBER]       = "sysdate()";

        $query = Query::insertSQL($auto_data, "SUBCLASS_COMP_SELECT_MST");
        return $query;
    }
    
    //**********コピー処理**********//

    //COMP_CREDITS_PATTERN_STD_COURSE_DATのコピー
    function getCopyCompQuery($model)
    {
        $staffcd = STAFFCD;
        $query  = "INSERT INTO COMP_CREDITS_PATTERN_STD_COURSE_DAT (   ";
        $query .= "    YEAR, ";
        $query .= "    RIREKI_CODE, ";
        $query .= "    PATTERN_CD, ";
        $query .= "    GRADE, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    COURSECODE, ";
        $query .= "    SCHREGNO, ";
        $query .= "    REGISTERCD, ";
        $query .= "    UPDATED )  ";
        $query .= "SELECT   ";
        $query .= "    YEAR, ";
        $query .= "    '{$model->field["RIREKI_CODE"]}', ";
        $query .= "    PATTERN_CD, ";
        $query .= "    GRADE, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    COURSECODE, ";
        $query .= "    SCHREGNO, ";
        $query .= "    '{$staffcd}' as REGISTERCD, ";
        $query .= "    sysdate() ";
        $query .= "FROM   ";
        $query .= "    COMP_CREDITS_PATTERN_STD_COURSE_DAT   ";
        $query .= "WHERE   ";
        $query .= "    YEAR = '{$model->field["YEAR"]}'  ";
        $query .= "AND RIREKI_CODE = '{$model->field["RIREKI_CODE_COPY"]}' ";

        return $query;
    }

    //SUBCLASS_STD_SELECT_RIREKI_DATのコピー
    function getCopyRirekiQuery($model)
    {
        $staffcd = STAFFCD;
        $query  = "INSERT INTO SUBCLASS_STD_SELECT_RIREKI_DAT (   ";
        $query .= "    YEAR, ";
        $query .= "    SEMESTER, ";
        $query .= "    GROUPCD, ";
        $query .= "    RIREKI_CODE, ";
        $query .= "    CLASSCD, ";
        $query .= "    SCHOOL_KIND, ";
        $query .= "    CURRICULUM_CD, ";
        $query .= "    SUBCLASSCD, ";
        $query .= "    SCHREGNO,  ";
        $query .= "    REGISTERCD, ";
        $query .= "    UPDATED )  ";
        $query .= "SELECT   ";
        $query .= "    YEAR, ";
        $query .= "    SEMESTER, ";
        $query .= "    GROUPCD, ";
        $query .= "    '{$model->field["RIREKI_CODE"]}', ";
        $query .= "    CLASSCD, ";
        $query .= "    SCHOOL_KIND, ";
        $query .= "    CURRICULUM_CD, ";
        $query .= "    SUBCLASSCD, ";
        $query .= "    SCHREGNO, ";
        $query .= "    '{$staffcd}' as REGISTERCD, ";
        $query .= "    sysdate() ";
        $query .= "FROM   ";
        $query .= "    SUBCLASS_STD_SELECT_RIREKI_DAT   ";
        $query .= "WHERE   ";
        $query .= "    YEAR = '{$model->field["YEAR"]}'  ";
        $query .= "AND RIREKI_CODE = '{$model->field["RIREKI_CODE_COPY"]}'   ";

        return $query;
    }

    //対象データチェック
    function getCountStdData($model)
    {
        $query  = "WITH STD_SELECT_RIREKI AS ( ";
        $query .= "SELECT ";
        $query .= "     COUNT(*) AS CNT1 ";
        $query .= "FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT ";
        $query .= "WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        $query .= "), PATTERN_STD AS ( ";
        $query .= "SELECT ";
        $query .= "     COUNT(*) AS CNT2 ";
        $query .= "FROM ";
        $query .= "     COMP_CREDITS_PATTERN_STD_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "     CNT1 + CNT2 AS CNT ";
        $query .= "FROM ";
        $query .= "     STD_SELECT_RIREKI T1, ";
        $query .= "     PATTERN_STD T2 ";
        
        return $query;
    }
}
?>
