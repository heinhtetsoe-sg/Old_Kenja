<?php

require_once('for_php7.php');

class knjd632Query extends Query {

    //処理学年
    function GetGrade()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     CAST(CAST(GRADE AS SMALLINT) AS CHAR(1)) || '学年' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    /* 実行 */
    function ExecuteQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->grade == "06") {
            $field[] = "NAMESPARE2";
            $field[] = "NAMESPARE3";
            $courseDiv[] = "1"; //文系
            $courseDiv[] = "2"; //理系
        } else {
            $field[] = "NAMESPARE1";
            $courseDiv[] = "0"; //無
        }

        // 0:単年データ
        $deleteSch = array();
        for ($i = 0; $i < get_count($field); $i++) {
            knjd632Query::insertSubclassQuery($db, $model, $field[$i], "0", $courseDiv[$i], $deleteSch);
        }
        knjd632Query::insertGradeTannen($db, $model, "0", "3");

        // 1:累積データ
        for ($i = 0; $i < get_count($field); $i++) {
            knjd632Query::insertSubclassQuery($db, $model, $field[$i], "1", $courseDiv[$i], $deleteSch);
        }
        knjd632Query::insertGradeTannen($db, $model, "1", "3");

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    /* 科目別の処理 */
    function insertSubclassQuery($db, $model, $field, $dataDiv, $courseDiv, &$deleteSch)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclasscd = "33-H-2-333333";
        } else {
            $subclasscd = "333333";
        }
        //削除クエリー
        $query  = knjd632Query::getDeleteQuery($model, $dataDiv, $courseDiv, $subclasscd);
        $db->query($query);

        //追加クエリー
        if ($dataDiv == "0") {
            $fieldCnt = $db->getOne(knjd632Query::getSubclassCnt($field));
            $query  = knjd632Query::getTannen($model, $field, $fieldCnt, $dataDiv, $courseDiv);
        } else {
            if ($model->grade <= "03") {
                if ($model->grade == "01") {
                    $grade = "''";
                } else {
                    $grade = ($model->grade == "02") ? "'01'" : "'01', '02'";
                }
            } else {
                if ($model->grade == "04") {
                    $grade = "''";
                } else {
                    $grade = ($model->grade == "05") ? "'04'" : "'04', '05'";
                }
            }
            $query  = knjd632Query::getRuisekiData($model, $subclasscd, $grade, $courseDiv);
        }

        $result = $db->query($query); 
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            if ($dataDiv == "0" && $courseDiv == "2" && strlen($row["CNT"]) == 0) {
                //単年で理系の実力データ無しは登録しない
                continue;
            } else if ($dataDiv == "0" && $courseDiv == "1" && strlen($row["CNT"]) == 0) {
                //単年で文系の実力データ無しを保管
                $deleteSch[] = $row["SCHREGNO"];
            } else if ($dataDiv == "0" && $courseDiv == "2" && strlen($row["CNT"]) > 0 && in_array($row["SCHREGNO"], $deleteSch)) {
                /**
                 * 単年で理系の実力データありで、単年文系の実力データなしデータがあった場合
                 * 単年文系のデータを削除する。
                 */
                $delQuery = knjd632Query::tanDelete($model, $row, $subclasscd);
                $db->query($delQuery);
            }

            //項目
            $data = knjd632Query::getFieldData($model, $row, $dataDiv, $courseDiv, $subclasscd);
            //追加
            $query = Query::insertSQL($data, "RECORD_MOCK_RANK_DAT");
            $db->query($query);
        }
        return;
    }

    //対象科目数
    function getSubclassCnt($field)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'D007' ";
        $query .= "     AND ".$field." = '1' ";

        return $query;
    }

    //削除クエリー
    function getDeleteQuery($model, $dataDiv, $courseDiv, $subclasscd)
    {
        $query  = " DELETE FROM ";
        $query .= "     RECORD_MOCK_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV   = '".$dataDiv."' ";
        $query .= "     AND COURSE_DIV = '".$courseDiv."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".substr($subclasscd, 0, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = '".substr($subclasscd, 3, 1)."' ";
            $query .= "     AND CURRICULUM_CD   = '".substr($subclasscd, 5, 1)."' ";
            $query .= "     AND SUBCLASSCD      = '".substr($subclasscd, 7, 6)."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= "     AND GRADE      = '".$model->grade."' ";

        return $query;
    }

    //単年データ削除クエリー
    function tanDelete($model, $row, $subclasscd)
    {
        $query  = " DELETE FROM ";
        $query .= "     RECORD_MOCK_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO   = '".$row["SCHREGNO"]."' ";
        $query .= "     AND DATA_DIV   = '0' ";
        $query .= "     AND COURSE_DIV = '1' ";
       //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".substr($subclasscd, 0, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = '".substr($subclasscd, 3, 1)."' ";
            $query .= "     AND CURRICULUM_CD   = '".substr($subclasscd, 5, 1)."' ";
            $query .= "     AND SUBCLASSCD      = '".substr($subclasscd, 7, 6)."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= "     AND GRADE      = '".$model->grade."' ";

        return $query;
    }

    //項目
    function getFieldData($model, $row, $dataDiv, $courseDiv, $subclasscd)
    {
        $data = array();
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]     = $row["SCHREGNO"];
        $data["DATA_DIV"][TEXT]     = $dataDiv;
        $data["COURSE_DIV"][TEXT]   = $courseDiv;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]          = substr($subclasscd, 0, 2);
            $data["SCHOOL_KIND"][TEXT]      = substr($subclasscd, 3, 1);
            $data["CURRICULUM_CD"][TEXT]    = substr($subclasscd, 5, 1);
            $data["SUBCLASSCD"][TEXT]       = substr($subclasscd, 7, 6);
        } else {
            $data["SUBCLASSCD"][TEXT]   = $subclasscd;
        }
        $data["GRADE"][TEXT]        = $model->grade;
        $data["SCORE1"][NUMBER]     = $row["SCORE1"];
        $data["SCORE2"][NUMBER]     = $row["SCORE2"];
        $data["SCORE3"][NUMBER]     = $row["SCORE3"];
        $data["RANK1"][NUMBER]      = $row["RANK1"];
        $data["RANK2"][NUMBER]      = $row["RANK2"];
        $data["RANK"][NUMBER]       = $row["RANK"];
        $data["PERFECT1"][NUMBER]   = $row["PERFECT1"];
        $data["PERFECT2"][NUMBER]   = $row["PERFECT2"];
        $data["PERFECT3"][NUMBER]   = $row["PERFECT3"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        return $data;
    }

    //単年データ取得
    function getTannen($model, $field, $fieldCnt, $dataDiv, $courseDiv)
    {
        $query  = " WITH SCH_T AS ( ";
        $query .= knjd632Query::getSchreg($model);

        $query .= " ), MOCK_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     COUNT(*) AS CNT, ";
        $query .= "     SUM(VALUE(SCORE, 0)) AS MOCK_SCORE, ";
        $query .= "     RANK() OVER(ORDER BY SUM(VALUE(SCORE, 0)) DESC) AS RANK2 ";
        $query .= " FROM ";
        $query .= "     MOCK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCORE IS NOT NULL ";
        $query .= "     AND SUBSTR(MOCKCD, 1, 1) = '2' ";
        $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";
        $query .= "     AND MOCK_SUBCLASS_CD IN (SELECT ";
        $query .= "                                  NAME1 ";
        $query .= "                              FROM ";
        $query .= "                                  V_NAME_MST ";
        $query .= "                              WHERE ";
        $query .= "                                  YEAR = '".CTRL_YEAR."' ";
        $query .= "                                  AND NAMECD1 = 'D007' ";
        $query .= "                                  AND ".$field." = '1') ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " HAVING ";
        $query .= "     COUNT(*) > 0 ";

        $query .= " ), COMBINED_T AS ( ";
        $query .=" SELECT ";
        $query .="     YEAR, ";
        $query .="     COMBINED_SUBCLASSCD ";
        $query .=" FROM ";
        $query .="     SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .=" WHERE ";
        $query .="     YEAR = '".CTRL_YEAR."' ";
        $query .=" GROUP BY ";
        $query .="     YEAR, ";
        $query .="     COMBINED_SUBCLASSCD ";

        $query .= " ), RECORD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(T1.SCORE, 0) * VALUE(L1.GET_CREDIT, 0)) AS RECORD_SCORE, ";
        $query .= "     RANK() OVER(ORDER BY SUM(VALUE(T1.SCORE, 0) * VALUE(L1.GET_CREDIT, 0)) DESC) AS RANK1 ";
        $query .= " FROM ";
        $query .= "     RECORD_RANK_DAT T1 ";
        $query .= "     LEFT JOIN RECORD_SCORE_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "          AND L1.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "          AND L1.TESTITEMCD = T1.TESTITEMCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND L1.CLASSCD         = T1.CLASSCD ";
            $query .= "          AND L1.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "          AND L1.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "          AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "          AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCORE IS NOT NULL ";
        $query .= "     AND L1.GET_CREDIT IS NOT NULL ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.TESTKINDCD = '99' ";
        $query .= "     AND T1.TESTITEMCD = '00' ";
        $query .= "     AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";
        $query .= "     AND SUBSTR(T1.SUBCLASSCD, 1, 2) < '89' ";
        $query .= "     AND T1.SUBCLASSCD NOT IN ('333333', '555555', '999999') ";
        $query .= "     AND T1.SUBCLASSCD NOT IN (SELECT W1.COMBINED_SUBCLASSCD FROM COMBINED_T W1) ";
        if ($model->electdiv == "1") {
            $query .= "     AND T1.SUBCLASSCD NOT IN (SELECT W1.SUBCLASSCD FROM SUBCLASS_MST W1 WHERE W1.ELECTDIV = '1') ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        $query .= " ), PERFECT1_T AS ( ";
        if (CTRL_YEAR == "2005" || CTRL_YEAR == "2006") {

            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     SUM(VALUE(L1.COMP_CREDIT, 0)) AS CREDIT ";
            $query .= " FROM ";
            $query .= "     RECORD_RANK_DAT T1 ";
            $query .= "     LEFT JOIN RECORD_SCORE_DAT L1 ON L1.YEAR = T1.YEAR ";
            $query .= "          AND L1.SEMESTER = T1.SEMESTER ";
            $query .= "          AND L1.TESTKINDCD = T1.TESTKINDCD ";
            $query .= "          AND L1.TESTITEMCD = T1.TESTITEMCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "          AND L1.CLASSCD         = T1.CLASSCD ";
                $query .= "          AND L1.SCHOOL_KIND     = T1.SCHOOL_KIND ";
                $query .= "          AND L1.CURRICULUM_CD   = T1.CURRICULUM_CD ";
            }
            $query .= "          AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "          AND L1.SCHREGNO = T1.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '9' ";
            $query .= "     AND T1.TESTKINDCD = '99' ";
            $query .= "     AND T1.TESTITEMCD = '00' ";
            $query .= "     AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";
            $query .= "     AND SUBSTR(T1.SUBCLASSCD, 1, 2) < '89' ";
            $query .= "     AND T1.SUBCLASSCD NOT IN ('333333', '555555', '999999') ";
            $query .= "     AND T1.SUBCLASSCD NOT IN (SELECT W1.COMBINED_SUBCLASSCD FROM COMBINED_T W1) ";
            if ($model->electdiv == "1") {
                $query .= "     AND T1.SUBCLASSCD NOT IN (SELECT W1.SUBCLASSCD FROM SUBCLASS_MST W1 WHERE W1.ELECTDIV = '1') ";
            }
            $query .= " GROUP BY ";
            $query .= "     T1.SCHREGNO ";

        } else {

            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     SUM(VALUE(L3.CREDITS, 0)) AS CREDIT ";
            $query .= " FROM ";
            $query .= "     SCH_T T1 ";
            $query .= "     LEFT JOIN (SELECT DISTINCT ";
            $query .= "                    LL1.YEAR, ";
            $query .= "                    LL1.SCHREGNO, ";
            $query .= "                    LL2.SUBCLASSCD ";
            $query .= "                FROM ";
            $query .= "                    CHAIR_STD_DAT LL1 ";
            $query .= "                    LEFT JOIN (SELECT DISTINCT ";
            $query .= "                                   YEAR, ";
            $query .= "                                   CHAIRCD, ";
            $query .= "                                   SUBCLASSCD ";
            $query .= "                               FROM ";
            $query .= "                                   CHAIR_DAT ";
            $query .= "                               WHERE ";
            $query .= "                                   YEAR = '".CTRL_YEAR."') LL2 ON LL2.CHAIRCD = LL1.CHAIRCD ";
            $query .= "                WHERE ";
            $query .= "                    LL1.YEAR = '".CTRL_YEAR."') L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= "     LEFT JOIN CREDIT_MST L3 ON L3.YEAR = L1.YEAR ";
            $query .= "          AND L3.COURSECD = T1.COURSECD ";
            $query .= "          AND L3.MAJORCD = T1.MAJORCD ";
            $query .= "          AND L3.GRADE = '".$model->grade."' ";
            $query .= "          AND L3.COURSECODE = T1.COURSECODE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "          AND L3.CLASSCD         = L1.CLASSCD ";
                $query .= "          AND L3.SCHOOL_KIND     = L1.SCHOOL_KIND ";
                $query .= "          AND L3.CURRICULUM_CD   = L1.CURRICULUM_CD ";
            }
            $query .= "          AND L3.SUBCLASSCD = L1.SUBCLASSCD ";
            $query .= " WHERE ";
            $query .= "     L1.SUBCLASSCD NOT IN (SELECT W2.COMBINED_SUBCLASSCD FROM COMBINED_T W2) ";
            $query .= "     AND SUBSTR(L1.SUBCLASSCD, 1, 2) < '89' ";
            if ($model->electdiv == "1") {
                $query .= "     AND L1.SUBCLASSCD NOT IN (SELECT W1.SUBCLASSCD FROM SUBCLASS_MST W1 WHERE W1.ELECTDIV = '1') ";
            }
            $query .=" GROUP BY ";
            $query .="     T1.SCHREGNO ";

        }

        $query .= " ), RECORD_T2 AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(T1.SCORE, 0) * VALUE(L1.GET_CREDIT, 0)) AS RECORD_SCORE, ";
        $query .= "     RANK() OVER(ORDER BY SUM(VALUE(T1.SCORE, 0) * VALUE(L1.GET_CREDIT, 0)) DESC) AS RANK1 ";
        $query .= " FROM ";
        $query .= "     RECORD_RANK_DAT T1 ";
        $query .= "     LEFT JOIN RECORD_SCORE_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "          AND L1.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "          AND L1.TESTITEMCD = T1.TESTITEMCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND L1.CLASSCD         = T1.CLASSCD ";
            $query .= "          AND L1.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "          AND L1.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "          AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "          AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCORE IS NOT NULL ";
        $query .= "     AND L1.GET_CREDIT IS NOT NULL ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.TESTKINDCD = '99' ";
        $query .= "     AND T1.TESTITEMCD = '00' ";
        $query .= "     AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";
        $query .= "     AND SUBSTR(T1.SUBCLASSCD, 1, 2) < '89' ";
        $query .= "     AND T1.SUBCLASSCD NOT IN ('333333', '555555', '999999') ";
        $query .= "     AND T1.SUBCLASSCD NOT IN (SELECT W1.COMBINED_SUBCLASSCD FROM COMBINED_T W1) ";
        $query .= "     AND T1.SUBCLASSCD NOT IN (SELECT W1.SUBCLASSCD FROM SUBCLASS_MST W1 WHERE W1.ELECTDIV = '1') ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        $query .= " ), PERFECT1_T2 AS ( ";
        if (CTRL_YEAR == "2005" || CTRL_YEAR == "2006") {

            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     SUM(VALUE(L1.COMP_CREDIT, 0)) AS CREDIT ";
            $query .= " FROM ";
            $query .= "     RECORD_RANK_DAT T1 ";
            $query .= "     LEFT JOIN RECORD_SCORE_DAT L1 ON L1.YEAR = T1.YEAR ";
            $query .= "          AND L1.SEMESTER = T1.SEMESTER ";
            $query .= "          AND L1.TESTKINDCD = T1.TESTKINDCD ";
            $query .= "          AND L1.TESTITEMCD = T1.TESTITEMCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "          AND L1.CLASSCD         = T1.CLASSCD ";
                $query .= "          AND L1.SCHOOL_KIND     = T1.SCHOOL_KIND ";
                $query .= "          AND L1.CURRICULUM_CD   = T1.CURRICULUM_CD ";
            }
            $query .= "          AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "          AND L1.SCHREGNO = T1.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '9' ";
            $query .= "     AND T1.TESTKINDCD = '99' ";
            $query .= "     AND T1.TESTITEMCD = '00' ";
            $query .= "     AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";
            $query .= "     AND SUBSTR(T1.SUBCLASSCD, 1, 2) < '89' ";
            $query .= "     AND T1.SUBCLASSCD NOT IN ('333333', '555555', '999999') ";
            $query .= "     AND T1.SUBCLASSCD NOT IN (SELECT W1.COMBINED_SUBCLASSCD FROM COMBINED_T W1) ";
            $query .= "     AND T1.SUBCLASSCD NOT IN (SELECT W1.SUBCLASSCD FROM SUBCLASS_MST W1 WHERE W1.ELECTDIV = '1') ";
            $query .= " GROUP BY ";
            $query .= "     T1.SCHREGNO ";

        } else {

            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     SUM(VALUE(L3.CREDITS, 0)) AS CREDIT ";
            $query .= " FROM ";
            $query .= "     SCH_T T1 ";
            $query .= "     LEFT JOIN (SELECT DISTINCT ";
            $query .= "                    LL1.YEAR, ";
            $query .= "                    LL1.SCHREGNO, ";
            $query .= "                    LL2.SUBCLASSCD ";
            $query .= "                FROM ";
            $query .= "                    CHAIR_STD_DAT LL1 ";
            $query .= "                    LEFT JOIN (SELECT DISTINCT ";
            $query .= "                                   YEAR, ";
            $query .= "                                   CHAIRCD, ";
            $query .= "                                   SUBCLASSCD ";
            $query .= "                               FROM ";
            $query .= "                                   CHAIR_DAT ";
            $query .= "                               WHERE ";
            $query .= "                                   YEAR = '".CTRL_YEAR."') LL2 ON LL2.CHAIRCD = LL1.CHAIRCD ";
            $query .= "                WHERE ";
            $query .= "                    LL1.YEAR = '".CTRL_YEAR."') L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= "     LEFT JOIN CREDIT_MST L3 ON L3.YEAR = L1.YEAR ";
            $query .= "          AND L3.COURSECD = T1.COURSECD ";
            $query .= "          AND L3.MAJORCD = T1.MAJORCD ";
            $query .= "          AND L3.GRADE = '".$model->grade."' ";
            $query .= "          AND L3.COURSECODE = T1.COURSECODE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "          AND L3.CLASSCD         = L1.CLASSCD ";
                $query .= "          AND L3.SCHOOL_KIND     = L1.SCHOOL_KIND ";
                $query .= "          AND L3.CURRICULUM_CD   = L1.CURRICULUM_CD ";
            }
            $query .= "          AND L3.SUBCLASSCD = L1.SUBCLASSCD ";
            $query .= " WHERE ";
            $query .= "     L1.SUBCLASSCD NOT IN (SELECT W2.COMBINED_SUBCLASSCD FROM COMBINED_T W2) ";
            $query .= "     AND SUBSTR(L1.SUBCLASSCD, 1, 2) < '89' ";
            $query .= "     AND L1.SUBCLASSCD NOT IN (SELECT W1.SUBCLASSCD FROM SUBCLASS_MST W1 WHERE W1.ELECTDIV = '1') ";
            $query .=" GROUP BY ";
            $query .="     T1.SCHREGNO ";

        }

        $query .= " ), PERFECT2_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(VALUE(PERFECT, 0)) AS PERFECT2 ";
        $query .= " FROM ";
        $query .= "     MOCK_PERFECT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND COURSE_DIV = '".$courseDiv."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        $query .= "     AND MOCK_SUBCLASS_CD IN (SELECT ";
        $query .= "                                  NAME1 ";
        $query .= "                              FROM ";
        $query .= "                                  V_NAME_MST ";
        $query .= "                              WHERE ";
        $query .= "                                  YEAR = '".CTRL_YEAR."' ";
        $query .= "                                  AND NAMECD1 = 'D007' ";
        $query .= "                                  AND ".$field." = '1') ";

        $query .= " ), BASE_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     MOCK_T ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     RECORD_T ";
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L2.CNT, ";
        $query .= "     L1.RECORD_SCORE AS SCORE1, ";
        $query .= "     L2.MOCK_SCORE AS SCORE2, ";
        $query .= "     VALUE(L4.RECORD_SCORE, 0) + VALUE(L2.MOCK_SCORE, 0) AS SCORE3, ";
        $query .= "     (VALUE(L3.CREDIT, 0) * 100) AS PERFECT1, ";
        $query .= "     CASE WHEN VALUE(L2.CNT, 0) > 0 ";
        $query .= "          THEN VALUE(PER2.PERFECT2, 0) ";
        $query .= "          ELSE 0 END AS PERFECT2, ";
        $query .= "     CASE WHEN VALUE(L2.CNT, 0) > 0 ";
        $query .= "          THEN VALUE(PER2.PERFECT2, 0) + (VALUE(L5.CREDIT, 0) * 100) ";
        $query .= "          ELSE 0 + (VALUE(L5.CREDIT, 0) * 100) END AS PERFECT3, ";
        $query .= "     L1.RANK1, ";
        $query .= "     L2.RANK2, ";
        $query .= "     RANK() OVER(ORDER BY VALUE(L4.RECORD_SCORE, 0) + VALUE(L2.MOCK_SCORE, 0) DESC) AS RANK ";
        $query .= " FROM ";
        $query .= "     BASE_T T1 ";
        $query .= "     LEFT JOIN RECORD_T L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN MOCK_T L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN PERFECT1_T L3 ON L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN RECORD_T2 L4 ON L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN PERFECT1_T2 L5 ON L5.SCHREGNO = T1.SCHREGNO, ";
        $query .= "     PERFECT2_T PER2 ";

        return $query;
    }

    //累積データ取得
    function getRuisekiData($model, $subclasscd, $grade, $courseDiv)
    {
        $query .= " WITH SCH_T AS ( ";
        $query .= knjd632Query::getSchreg($model);
        $query .= " ), YEAR_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE, ";
        $query .= "     MAX(YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR < '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";
        $query .= "     AND GRADE IN (".$grade.") ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE ";

        $query .= " ), TAISYOU_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     VALUE(PERFECT1, 0)  AS PERFECT1, ";
        $query .= "     VALUE(PERFECT2, 0)  AS PERFECT2, ";
        $query .= "     VALUE(PERFECT3, 0)  AS PERFECT3 ";
        $query .= " FROM ";
        $query .= "     RECORD_MOCK_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '0' ";
        $query .= "     AND COURSE_DIV = '".$courseDiv."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".substr($subclasscd, 0, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = '".substr($subclasscd, 3, 1)."' ";
            $query .= "     AND CURRICULUM_CD   = '".substr($subclasscd, 5, 1)."' ";
            $query .= "     AND SUBCLASSCD      = '".substr($subclasscd, 7, 6)."' ";
        } else {
            $query .= "     AND SUBCLASSCD      = '$subclasscd' ";
        }
        
        $query .= " ), TAISYOU_SCORE1 AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     VALUE(SCORE1, 0) AS SCORE1 ";
        $query .= " FROM ";
        $query .= "     RECORD_MOCK_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '0' ";
        $query .= "     AND COURSE_DIV = '".$courseDiv."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".substr($subclasscd, 0, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = '".substr($subclasscd, 3, 1)."' ";
            $query .= "     AND CURRICULUM_CD   = '".substr($subclasscd, 5, 1)."' ";
            $query .= "     AND SUBCLASSCD      = '".substr($subclasscd, 7, 6)."' ";
        } else {
            $query .= "     AND SUBCLASSCD      = '$subclasscd' ";
        }
        $query .= "     AND SCORE1 IS NOT NULL ";

        $query .= " ), TAISYOU_SCORE2 AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     VALUE(SCORE2, 0) AS SCORE2 ";
        $query .= " FROM ";
        $query .= "     RECORD_MOCK_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '0' ";
        $query .= "     AND COURSE_DIV = '".$courseDiv."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".substr($subclasscd, 0, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = '".substr($subclasscd, 3, 1)."' ";
            $query .= "     AND CURRICULUM_CD   = '".substr($subclasscd, 5, 1)."' ";
            $query .= "     AND SUBCLASSCD      = '".substr($subclasscd, 7, 6)."' ";
        } else {
            $query .= "     AND SUBCLASSCD      = '$subclasscd' ";
        }
        $query .= "     AND SCORE2 IS NOT NULL ";

        $query .= " ), TAISYOU_SCORE3 AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     VALUE(SCORE3, 0) AS SCORE3 ";
        $query .= " FROM ";
        $query .= "     RECORD_MOCK_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '0' ";
        $query .= "     AND COURSE_DIV = '".$courseDiv."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".substr($subclasscd, 0, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = '".substr($subclasscd, 3, 1)."' ";
            $query .= "     AND CURRICULUM_CD   = '".substr($subclasscd, 5, 1)."' ";
            $query .= "     AND SUBCLASSCD      = '".substr($subclasscd, 7, 6)."' ";
        } else {
            $query .= "     AND SUBCLASSCD      = '$subclasscd' ";
        }
        $query .= "     AND SCORE3 IS NOT NULL ";

        $query .= " ), TOTAL_SUM AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUM(VALUE(PERFECT1, 0))  AS PERFECT1, ";
        $query .= "     SUM(VALUE(PERFECT2, 0))  AS PERFECT2, ";
        $query .= "     SUM(VALUE(PERFECT3, 0))  AS PERFECT3 ";
        $query .= " FROM ";
        $query .= "     RECORD_MOCK_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SCHREGNO || GRADE IN (SELECT W1.YEAR || W1.SCHREGNO || W1.GRADE FROM YEAR_T W1) ";
        $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM TAISYOU_T) ";
        $query .= "     AND DATA_DIV = '0' ";
        $query .= "     AND COURSE_DIV = '0' "; //固定(3年以外のデータを抽出する為)
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".substr($subclasscd, 0, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = '".substr($subclasscd, 3, 1)."' ";
            $query .= "     AND CURRICULUM_CD   = '".substr($subclasscd, 5, 1)."' ";
            $query .= "     AND SUBCLASSCD      = '".substr($subclasscd, 7, 6)."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";

        $query .= " ), TOTAL_SCORE1 AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUM(VALUE(SCORE1, 0))  AS SCORE1 ";
        $query .= " FROM ";
        $query .= "     RECORD_MOCK_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SCHREGNO || GRADE IN (SELECT W1.YEAR || W1.SCHREGNO || W1.GRADE FROM YEAR_T W1) ";
        $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM TAISYOU_T) ";
        $query .= "     AND DATA_DIV = '0' ";
        $query .= "     AND COURSE_DIV = '0' "; //固定(3年以外のデータを抽出する為)
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".substr($subclasscd, 0, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = '".substr($subclasscd, 3, 1)."' ";
            $query .= "     AND CURRICULUM_CD   = '".substr($subclasscd, 5, 1)."' ";
            $query .= "     AND SUBCLASSCD      = '".substr($subclasscd, 7, 6)."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= "     AND SCORE1 IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";

        $query .= " ), TOTAL_SCORE2 AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUM(VALUE(SCORE2, 0))  AS SCORE2 ";
        $query .= " FROM ";
        $query .= "     RECORD_MOCK_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SCHREGNO || GRADE IN (SELECT W1.YEAR || W1.SCHREGNO || W1.GRADE FROM YEAR_T W1) ";
        $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM TAISYOU_T) ";
        $query .= "     AND DATA_DIV = '0' ";
        $query .= "     AND COURSE_DIV = '0' "; //固定(3年以外のデータを抽出する為)
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".substr($subclasscd, 0, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = '".substr($subclasscd, 3, 1)."' ";
            $query .= "     AND CURRICULUM_CD   = '".substr($subclasscd, 5, 1)."' ";
            $query .= "     AND SUBCLASSCD      = '".substr($subclasscd, 7, 6)."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= "     AND SCORE2 IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";

        $query .= " ), TOTAL_SCORE3 AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SUM(VALUE(SCORE3, 0))  AS SCORE3 ";
        $query .= " FROM ";
        $query .= "     RECORD_MOCK_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SCHREGNO || GRADE IN (SELECT W1.YEAR || W1.SCHREGNO || W1.GRADE FROM YEAR_T W1) ";
        $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM TAISYOU_T) ";
        $query .= "     AND DATA_DIV = '0' ";
        $query .= "     AND COURSE_DIV = '0' "; //固定(3年以外のデータを抽出する為)
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".substr($subclasscd, 0, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = '".substr($subclasscd, 3, 1)."' ";
            $query .= "     AND CURRICULUM_CD   = '".substr($subclasscd, 5, 1)."' ";
            $query .= "     AND SUBCLASSCD      = '".substr($subclasscd, 7, 6)."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= "     AND SCORE3 IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";

        $query .= " ), SUM_PERFECT AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(T1.PERFECT1) AS PERFECT1, ";
        $query .= "     SUM(T1.PERFECT2) AS PERFECT2, ";
        $query .= "     SUM(T1.PERFECT3) AS PERFECT3 ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "          * ";
        $query .= "      FROM ";
        $query .= "          TOTAL_SUM ";
        $query .= "      UNION ALL ";
        $query .= "      SELECT ";
        $query .= "          * ";
        $query .= "      FROM ";
        $query .= "          TAISYOU_T ) T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        $query .= " ), SUM_SCORE1 AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(T1.SCORE1, 0))  AS SCORE1, ";
        $query .= "     RANK() OVER(ORDER BY SUM(T1.SCORE1) DESC) AS RANK1 ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "          * ";
        $query .= "      FROM ";
        $query .= "          TAISYOU_SCORE1 ";
        $query .= "      UNION ALL ";
        $query .= "      SELECT ";
        $query .= "          * ";
        $query .= "      FROM ";
        $query .= "          TOTAL_SCORE1 ) T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        $query .= " ), SUM_SCORE2 AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(T1.SCORE2, 0))  AS SCORE2, ";
        $query .= "     RANK() OVER(ORDER BY SUM(T1.SCORE2) DESC) AS RANK2 ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "          * ";
        $query .= "      FROM ";
        $query .= "          TAISYOU_SCORE2 ";
        $query .= "      UNION ALL ";
        $query .= "      SELECT ";
        $query .= "          * ";
        $query .= "      FROM ";
        $query .= "          TOTAL_SCORE2 ) T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        $query .= " ), SUM_SCORE3 AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(T1.SCORE3, 0))  AS SCORE3, ";
        $query .= "     RANK() OVER(ORDER BY SUM(T1.SCORE3) DESC) AS RANK3 ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "          * ";
        $query .= "      FROM ";
        $query .= "          TAISYOU_SCORE3 ";
        $query .= "      UNION ALL ";
        $query .= "      SELECT ";
        $query .= "          * ";
        $query .= "      FROM ";
        $query .= "          TOTAL_SCORE3 ) T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        $query .= " ), TOTAL_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     TOTAL_SUM ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     TAISYOU_T ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM_S1.SCORE1, ";
        $query .= "     SUM_S2.SCORE2, ";
        $query .= "     SUM_S3.SCORE3, ";
        $query .= "     SUM_PER.PERFECT1, ";
        $query .= "     SUM_PER.PERFECT2, ";
        $query .= "     SUM_PER.PERFECT3, ";
        $query .= "     SUM_S1.RANK1, ";
        $query .= "     SUM_S2.RANK2, ";
        $query .= "     SUM_S3.RANK3 AS RANK ";
        $query .= " FROM ";
        $query .= "     TOTAL_T T1 ";
        $query .= "     LEFT JOIN SUM_PERFECT SUM_PER ON T1.SCHREGNO = SUM_PER.SCHREGNO ";
        $query .= "     LEFT JOIN SUM_SCORE1 SUM_S1 ON T1.SCHREGNO = SUM_S1.SCHREGNO ";
        $query .= "     LEFT JOIN SUM_SCORE2 SUM_S2 ON T1.SCHREGNO = SUM_S2.SCHREGNO ";
        $query .= "     LEFT JOIN SUM_SCORE3 SUM_S3 ON T1.SCHREGNO = SUM_S3.SCHREGNO ";

        return $query;
    }

    function getSchreg($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";

        return $query;
    }

    function insertGradeTannen($db, $model, $dataDiv, $courseDiv)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclasscd = "33-H-2-333333";
        } else {
            $subclasscd = "333333";
        }

        $query  = knjd632Query::getDeleteQuery($model, $dataDiv, $courseDiv, $subclasscd);
        $db->query($query);

        $query  = " INSERT INTO RECORD_MOCK_RANK_DAT ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '".$dataDiv."' AS DATA_DIV, ";
        $query .= "     '".$courseDiv."' AS COURSE_DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     L1.SCORE1, ";
        $query .= "     L2.SCORE2, ";
        $query .= "     L3.SCORE3, ";
        $query .= "     L1.RANK1, ";
        $query .= "     L2.RANK2, ";
        $query .= "     L3.RANK, ";
        $query .= "     MAX(T1.PERFECT1) AS PERFECT1, ";
        $query .= "     MAX(T1.PERFECT2) AS PERFECT2, ";
        $query .= "     MAX(T1.PERFECT3) AS PERFECT3, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     RECORD_MOCK_RANK_DAT T1 ";
        $query .= knjd632Query::getGradeTannenJoin($model, $dataDiv, $courseDiv, $subclasscd, "1", "1");
        $query .= knjd632Query::getGradeTannenJoin($model, $dataDiv, $courseDiv, $subclasscd, "2", "2");
        $query .= knjd632Query::getGradeTannenJoin($model, $dataDiv, $courseDiv, $subclasscd, "3", "");

        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.DATA_DIV = '".$dataDiv."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD         = '".substr($subclasscd, 0, 2)."' ";
            $query .= "     AND T1.SCHOOL_KIND     = '".substr($subclasscd, 3, 1)."' ";
            $query .= "     AND T1.CURRICULUM_CD   = '".substr($subclasscd, 5, 1)."' ";
            $query .= "     AND T1.SUBCLASSCD      = '".substr($subclasscd, 7, 6)."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= "     AND T1.GRADE = '".$model->grade."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     L1.SCORE1, ";
        $query .= "     L2.SCORE2, ";
        $query .= "     L3.SCORE3, ";
        $query .= "     L1.RANK1, ";
        $query .= "     L2.RANK2, ";
        $query .= "     L3.RANK ";
        $query .= " ) ";

        $db->query($query);
    }

    //ランク付け
    function getGradeTannenJoin($model, $dataDiv, $courseDiv, $subclasscd, $score, $rank)
    {
        $query  = "     LEFT JOIN ( ";
        $query .= "                 SELECT ";
        $query .= "                     YEAR, ";
        $query .= "                     SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 CLASSCD, ";
            $query .= "                 SCHOOL_KIND, ";
            $query .= "                 CURRICULUM_CD, ";
        }
        $query .= "                     SUBCLASSCD, ";
        $query .= "                     GRADE, ";
        $query .= "                     MAX(SCORE".$score.") AS SCORE".$score.", ";
        $query .= "                     RANK() OVER(ORDER BY VALUE(MAX(SCORE".$score."), 0) DESC) AS RANK".$rank." ";
        $query .= "                 FROM ";
        $query .= "                     RECORD_MOCK_RANK_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' ";
        $query .= "                     AND DATA_DIV = '".$dataDiv."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     AND CLASSCD         = '".substr($subclasscd, 0, 2)."' ";
            $query .= "                     AND SCHOOL_KIND     = '".substr($subclasscd, 3, 1)."' ";
            $query .= "                     AND CURRICULUM_CD   = '".substr($subclasscd, 5, 1)."' ";
            $query .= "                     AND SUBCLASSCD      = '".substr($subclasscd, 7, 6)."' ";
        } else {
            $query .= "                     AND SUBCLASSCD      = '".$subclasscd."' ";
        }
        $query .= "                     AND GRADE = '".$model->grade."' ";
        $query .= "                     AND SCORE".$score." IS NOT NULL ";
        $query .= "                 GROUP BY ";
        $query .= "                     YEAR, ";
        $query .= "                     SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 CLASSCD, ";
            $query .= "                 SCHOOL_KIND, ";
            $query .= "                 CURRICULUM_CD, ";
        }
        $query .= "                     SUBCLASSCD, ";
        $query .= "                     GRADE ";
        $query .= "               ) L".$score." ON L".$score.".YEAR = T1.YEAR ";
        $query .= "          AND L".$score.".SCHREGNO = T1.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND L".$score.".CLASSCD        = T1.CLASSCD ";
            $query .= "          AND L".$score.".SCHOOL_KIND    = T1.SCHOOL_KIND ";
            $query .= "          AND L".$score.".CURRICULUM_CD  = T1.CURRICULUM_CD ";
        }
        $query .= "          AND L".$score.".SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "          AND L".$score.".GRADE = T1.GRADE ";

        return $query;
    }
}
?>