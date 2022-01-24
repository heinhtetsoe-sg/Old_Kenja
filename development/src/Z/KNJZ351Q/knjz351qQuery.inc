<?php

require_once('for_php7.php');

class knjz351qQuery extends Query {

    function getCourseMajor($model) {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE, ";
        $query .= "     A043.NAME1 ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST VCM ";
        $query .= "     LEFT JOIN NAME_MST A043 ON NAMECD1 = 'A043' ";
        $query .= "          AND VCM.COURSECD || VCM.MAJORCD = A043.NAME2 || A043.NAME3 ";
        $query .= " WHERE ";
        $query .= "     VCM.YEAR = '{$model->year}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     VALUE(NAME1, 'H') = '".SCHOOLKIND."' ";

        return $query;
    }

    //対象データ取得
    function getListdata($model, $subclasscd="", $sakiValue="") {
        $query  = "";
        $query .= " WITH SUBCLASS_LIST AS ( ";
        //学校種別(H:高校 J:中学 P:小学)
        foreach ($model->schoolKindArray as $school_kind => $subclassAll) {
            if ($school_kind == "H") {
                $school_kind_name = "（高校）";
            }
            if ($school_kind == "J") {
                $school_kind_name = "（中学）";
            }
            if ($school_kind == "P") {
                $school_kind_name = "（小学）";
            }
            $query .= " SELECT ";
            $query .= "     '{$subclassAll}' AS KAMOKU_VALUE, ";
            $query .= "     '{$subclassAll}：基本設定{$school_kind_name}' AS KAMOKU_LABEL ";
            $query .= " FROM ";
            $query .= "     SYSIBM.SYSDUMMY1  ";
            $query .= " UNION ALL ";
        }
        $query .= " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS KAMOKU_VALUE, ";
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS KAMOKU_LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T4, ";
        $query .= "     SUBCLASS_MST T3 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
        $query .= "     AND T2.YEAR       = T1.YEAR ";
        $query .= "     AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     AND T4.YEAR       = T1.YEAR ";
        $query .= "     AND T4.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T4.CHAIRCD    = T1.CHAIRCD ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND T4.STAFFCD = '".STAFFCD."' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T3.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "     AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     AND (T3.SUBCLASSCD NOT LIKE '9%' OR T3.SUBCLASSCD LIKE '90%' ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        if ($subclasscd != "" && $sakiValue != "") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     L1.KAMOKU_LABEL, ";
            $query .= "     L1.KAMOKU_VALUE, ";
            $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || S1.SEMESTERNAME || '-' || L2.TESTITEMNAME AS SAKI_LABEL, ";
            $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS SAKI_VALUE, ";
            $query .= "     T1.MOTO_SEMESTER || T1.MOTO_TESTKINDCD || T1.MOTO_TESTITEMCD || T1.MOTO_SCORE_DIV || ':' || S2.SEMESTERNAME || '-' || L3.TESTITEMNAME AS MOTO_LABEL, ";
            $query .= "     T1.MOTO_SEMESTER || T1.MOTO_TESTKINDCD || T1.MOTO_TESTITEMCD || T1.MOTO_SCORE_DIV AS MOTO_VALUE ";
        }
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_CALC_GCM_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST S1 ON S1.YEAR = T1.YEAR ";
        $query .= "                              AND S1.SEMESTER = T1.SEMESTER ";
        $query .= "     LEFT JOIN SEMESTER_MST S2 ON S2.YEAR = T1.YEAR ";
        $query .= "                              AND S2.SEMESTER = T1.MOTO_SEMESTER ";
        $query .= "     LEFT JOIN SUBCLASS_LIST L1 ON L1.KAMOKU_VALUE = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                                AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "                                                AND L2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "                                                AND L2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "                                                AND L2.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "                                                AND L2.SCHOOLCD = T1.SCHOOLCD ";
        $query .= "                                                AND L2.GRADE = T1.GRADE ";
        $query .= "                                                AND L2.COURSECD || L2.MAJORCD = T1.COURSECD || T1.MAJORCD ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV L3 ON L3.YEAR = T1.YEAR ";
        $query .= "                                                AND L3.SEMESTER = T1.MOTO_SEMESTER ";
        $query .= "                                                AND L3.TESTKINDCD = T1.MOTO_TESTKINDCD ";
        $query .= "                                                AND L3.TESTITEMCD = T1.MOTO_TESTITEMCD ";
        $query .= "                                                AND L3.SCORE_DIV = T1.MOTO_SCORE_DIV ";
        $query .= "                                                AND L3.SCHOOLCD = T1.SCHOOLCD ";
        $query .= "                                                AND L3.GRADE = T1.GRADE ";
        $query .= "                                                AND L3.COURSECD || L3.MAJORCD = T1.COURSECD || T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["COURSE_MAJOR"]) {
            $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($subclasscd != "" && $sakiValue != "") {
            $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$subclasscd."' ";
            $query .= " AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$sakiValue."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     KAMOKU_VALUE ";
        }
        return $query;
    }

    //学校種別の取得
    function getNameMstA023() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";
        return $query;
    }

    //科目データの取得
    function getSubclassMst($model) {
        $query  = "";
        $query .= " WITH SUB_ALL (VALUE, LABEL) AS ( ";
        //学校種別(H:高校 J:中学 P:小学)
        $cnt = 0;
        foreach ($model->schoolKindArray as $school_kind => $subclassAll) {
            if ($school_kind == "H") {
                $school_kind_name = "（高校）";
            }
            if ($school_kind == "J") {
                $school_kind_name = "（中学）";
            }
            if ($school_kind == "P") {
                $school_kind_name = "（小学）";
            }
            if (0 < $cnt) {
                $query .= " UNION ALL ";
            }
            $query .= "     VALUES('{$subclassAll}', '{$subclassAll}：基本設定{$school_kind_name}') ";
            $cnt++;
        }
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     T3.VALUE, ";
        $query .= "     T3.LABEL ";
        $query .= " FROM ";
        $query .= "     SUB_ALL T3 ";
        $query .= " UNION ALL ";
        $query .= " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T4, ";
        $query .= "     SUBCLASS_MST T3 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
        $query .= "     AND T2.YEAR       = T1.YEAR ";
        $query .= "     AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     AND T4.YEAR       = T1.YEAR ";
        $query .= "     AND T4.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T4.CHAIRCD    = T1.CHAIRCD ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND T4.STAFFCD = '".STAFFCD."' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T3.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "     AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     AND (T3.SUBCLASSCD NOT LIKE '9%' OR T3.SUBCLASSCD LIKE '90%' ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //親画面で、科目別設定されたパーツがあるか
    function getSubPartsCnt($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
        $query .= "     INNER JOIN ADMIN_CONTROL_GCM_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV ";
        $query .= "           = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ";
        $query .= "         AND T2.SCHOOLCD = T1.SCHOOLCD ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        $query .= "         AND T2.COURSECD || T2.MAJORCD = T1.COURSECD || T1.MAJORCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
            $query .= "          AND T2.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND T2.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND T2.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND T2.SUBCLASSCD     = '".$subclass_array[3]."' ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= "          AND T2.SUBCLASSCD     = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        return $query;
    }

    //(左)入力可テキストボックス表示データ取得
    function selectListQuery($db, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || SEM_M.SEMESTERNAME || '-' || T1.TESTITEMNAME AS LABEL, ";
        $query .= "     CASE WHEN T3.SCORE_DIV IS NOT NULL THEN '1' END AS PARTS_FLG, ";
        $query .= "     CASE WHEN C2.SCORE_DIV IS NOT NULL THEN '1' END AS CALC_FLG ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST SEM_M ";
        $query .= "         ON  SEM_M.YEAR = T1.YEAR ";
        $query .= "         AND SEM_M.SEMESTER = T1.SEMESTER ";
        //算出元(対象)
        $query .= "     LEFT JOIN ADMIN_CONTROL_CALC_GCM_SDIV_DAT C2 ";
        $query .= "         ON  C2.YEAR = T1.YEAR ";
        $query .= "         AND C2.SEMESTER || C2.TESTKINDCD || C2.TESTITEMCD || C2.SCORE_DIV ";
        $query .= "           = '".$model->field["SAKI_TESTCD"]."' ";
        $query .= "         AND C2.SCHOOLCD = T1.SCHOOLCD ";
        $query .= "         AND C2.GRADE = T1.GRADE ";
        $query .= "         AND C2.COURSECD || C2.MAJORCD = T1.COURSECD || T1.MAJORCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND C2.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND C2.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND C2.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND C2.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND C2.SUBCLASSCD     = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "         AND C2.MOTO_SEMESTER || C2.MOTO_TESTKINDCD || C2.MOTO_TESTITEMCD || C2.MOTO_SCORE_DIV ";
        $query .= "           = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ";
        //算出元(候補)
        $query .= "     INNER JOIN ADMIN_CONTROL_GCM_SDIV_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "         AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV ";
        $query .= "           = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ";
        $query .= "         AND T3.SCHOOLCD = T1.SCHOOLCD ";
        $query .= "         AND T3.GRADE = T1.GRADE ";
        $query .= "         AND T3.COURSECD || T3.MAJORCD = T1.COURSECD || T1.MAJORCD ";
        //親画面で、
        //基本設定されたパーツのみ表示する
        //科目別設定されたパーツがある場合は、科目別設定されたパーツのみ表示する
        //学校種別(H:高校 J:中学 P:小学)
        foreach ($model->schoolKindArray as $school_kind => $subclassAll) {
            if ($school_kind == $subclass_array[1]) {
                $cnt = $db->getOne(knjz351qQuery::getSubPartsCnt($model));
                $subclasscd = (0 < $cnt && $subclassAll != $model->field["SUBCLASSCD"]) ? $model->field["SUBCLASSCD"] : $subclassAll;
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $subclass_array = array();
                    $subclass_array = explode("-", $subclasscd);
                    $query .= "          AND T3.CLASSCD        = '".$subclass_array[0]."' ";
                    $query .= "          AND T3.SCHOOL_KIND    = '".$subclass_array[1]."' ";
                    $query .= "          AND T3.CURRICULUM_CD  = '".$subclass_array[2]."' ";
                    $query .= "          AND T3.SUBCLASSCD     = '".$subclass_array[3]."' ";
                    if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
                    }
                } else {
                    $query .= "          AND T3.SUBCLASSCD     = '".$subclasscd."' ";
                }
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //UPDATE(パーツ)
    function &getUpdateQuery(&$model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        //配列
        $codeArray = array();
        $codeArray = explode (",", $model->selectdata);

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = "DELETE FROM ADMIN_CONTROL_CALC_GCM_SDIV_DAT WHERE YEAR = '".$model->year."'";
        $query .= "         AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->field["SAKI_TESTCD"]."' ";
        $query .= "         AND COURSECD || '-' || MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND SUBCLASSCD     = '".$model->field["SUBCLASSCD"]."' ";
        }
        $db->query($query);

        list($coursecd, $majorcd) = preg_split("/-/", $model->field["COURSE_MAJOR"]);
        if ($codeArray[0] != "") {
            for ($i = 0; $i < get_count($codeArray); $i++) {
                $data = array();
                $data["YEAR"][TEXT]             = $model->year;
                $data["SCHOOLCD"][TEXT]         = "000000000000";
                $data["SEMESTER"][TEXT]         = substr($model->field["SAKI_TESTCD"], 0, 1);
                $data["TESTKINDCD"][TEXT]       = substr($model->field["SAKI_TESTCD"], 1, 2);
                $data["TESTITEMCD"][TEXT]       = substr($model->field["SAKI_TESTCD"], 3, 2);
                $data["SCORE_DIV"][TEXT]        = substr($model->field["SAKI_TESTCD"], 5, 2);
                $data["GRADE"][TEXT]            = "00";
                $data["COURSECD"][TEXT]         = $coursecd;
                $data["MAJORCD"][TEXT]          = $majorcd;
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]          = $subclass_array[0];
                    $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
                    $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
                    $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
                } else {
                    $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
                }
                $data["MOTO_SEMESTER"][TEXT]    = substr($codeArray[$i], 0, 1);
                $data["MOTO_TESTKINDCD"][TEXT]  = substr($codeArray[$i], 1, 2);
                $data["MOTO_TESTITEMCD"][TEXT]  = substr($codeArray[$i], 3, 2);
                $data["MOTO_SCORE_DIV"][TEXT]   = substr($codeArray[$i], 5, 2);
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                //追加
                $query = Query::insertSQL($data, "ADMIN_CONTROL_CALC_GCM_SDIV_DAT");
                $db->query($query);
            }
        }


        $db->commit(); // トランザクションをコミットする。
        //DB切断
        Query::dbCheckIn($db);
        return;
    }

    //データの存在チェック（コピー用）
    function chkCopyData($year, $flg) {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_CALC_GCM_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND COURSECD || '-' || MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        return $query;
    }

    //コピー
    function &getCopyQuery($model, $Row) {
        list($coursecd, $majorcd) = preg_split("/-/", $model->field["COURSE_MAJOR"]);
        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        $data["SCHOOLCD"][TEXT]         = $Row["SCHOOLCD"];
        $data["SEMESTER"][TEXT]         = $Row["SEMESTER"];
        $data["TESTKINDCD"][TEXT]       = $Row["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]       = $Row["TESTITEMCD"];
        $data["SCORE_DIV"][TEXT]        = $Row["SCORE_DIV"];
        $data["CLASSCD"][TEXT]          = $Row["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]      = $Row["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]    = $Row["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]       = $Row["SUBCLASSCD"];
        $data["MOTO_SEMESTER"][TEXT]    = $Row["MOTO_SEMESTER"];
        $data["MOTO_TESTKINDCD"][TEXT]  = $Row["MOTO_TESTKINDCD"];
        $data["MOTO_TESTITEMCD"][TEXT]  = $Row["MOTO_TESTITEMCD"];
        $data["MOTO_SCORE_DIV"][TEXT]   = $Row["MOTO_SCORE_DIV"];
        $data["GRADE"][TEXT]            = $Row["GRADE"];
        $data["COURSECD"][TEXT]         = $coursecd;
        $data["MAJORCD"][TEXT]          = $majorcd;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ADMIN_CONTROL_CALC_GCM_SDIV_DAT");
        return $query;
    }
}
?>
