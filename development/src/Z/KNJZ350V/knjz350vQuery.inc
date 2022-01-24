<?php

require_once('for_php7.php');

class knjz350vQuery extends Query {

    //学校種別の取得
    function getNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
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
            if ($school_kind == "K") {
                $school_kind_name = "（幼稚園）";
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
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND T1.SCHOOL_KIND = '".$model->setSchoolKind."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "     AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     AND (T3.SUBCLASSCD NOT LIKE '9%' OR T3.SUBCLASSCD LIKE '90%' ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //(左)入力可テキストボックス表示データ取得
    function selectListQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || SEM_M.SEMESTERNAME || '-' || T1.TESTITEMNAME AS LABEL, ";
        $query .= "     CASE WHEN T2.SCORE_DIV IS NOT NULL THEN '1' END AS PARTS_FLG, ";
        $query .= "     T2.CONTROL_FLG ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST SEM_M ";
        $query .= "         ON  SEM_M.YEAR = T1.YEAR ";
        $query .= "         AND SEM_M.SEMESTER = T1.SEMESTER ";
        $query .= "     LEFT JOIN ADMIN_CONTROL_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV ";
        $query .= "           = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND T2.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND T2.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND T2.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND T2.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND T2.SUBCLASSCD     = '".$model->field["SUBCLASSCD"]."' ";
        }
        //科目別設定の場合は、基本設定されたパーツのみ表示する
        //学校種別(H:高校 J:中学 P:小学)
        foreach ($model->schoolKindArray as $school_kind => $subclassAll) {
            if ($subclassAll != $model->field["SUBCLASSCD"] && $school_kind == $subclass_array[1]) {
                $query .= "     INNER JOIN ADMIN_CONTROL_SDIV_DAT T3 ON T3.YEAR = T1.YEAR ";
                $query .= "         AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV ";
                $query .= "           = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $subclassAll_array = array();
                    $subclassAll_array = explode("-", $subclassAll);
                    $query .= "          AND T3.CLASSCD        = '".$subclassAll_array[0]."' ";
                    $query .= "          AND T3.SCHOOL_KIND    = '".$subclassAll_array[1]."' ";
                    $query .= "          AND T3.CURRICULUM_CD  = '".$subclassAll_array[2]."' ";
                    $query .= "          AND T3.SUBCLASSCD     = '".$subclassAll_array[3]."' ";
                } else {
                    $query .= "          AND T3.SUBCLASSCD     = '".$subclassAll."' ";
                }
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
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
        $query = "DELETE FROM ADMIN_CONTROL_SDIV_DAT WHERE YEAR = '".$model->year."'";
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

        if ($codeArray[0] != "") {
            for ($i = 0; $i < get_count($codeArray); $i++) {
                $data = array();
                $data["YEAR"][TEXT]         = $model->year;
                $data["SEMESTER"][TEXT]     = substr($codeArray[$i], 0, 1);
                $data["TESTKINDCD"][TEXT]   = substr($codeArray[$i], 1, 2);
                $data["TESTITEMCD"][TEXT]   = substr($codeArray[$i], 3, 2);
                $data["SCORE_DIV"][TEXT]    = substr($codeArray[$i], 5, 2);
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]          = $subclass_array[0];
                    $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
                    $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
                    $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
                } else {
                    $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
                }
                $data["CONTROL_FLG"][TEXT]  = "";
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                //追加
                $query = Query::insertSQL($data, "ADMIN_CONTROL_SDIV_DAT");
                $db->query($query);
            }
        }

        //科目別設定について、基本設定されたパーツ以外を削除する
        //学校種別(H:高校 J:中学 P:小学)
        foreach ($model->schoolKindArray as $school_kind => $subclassAll) {
            if ($subclassAll == $model->field["SUBCLASSCD"]) {
                $query  = " DELETE ";
                $query .= " FROM ";
                $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
                $query .= " WHERE ";
                $query .= "     T1.YEAR = '".$model->year."' ";
                //基本設定されたパーツ以外
                $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ( ";
                $query .= "         SELECT ";
                $query .= "             T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV ";
                $query .= "         FROM ";
                $query .= "             ADMIN_CONTROL_SDIV_DAT T3 ";
                $query .= "         WHERE ";
                $query .= "             T3.YEAR = '".$model->year."' ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = '".$subclassAll."' ";
                } else {
                    $query .= "         AND T3.SUBCLASSCD = '".$subclassAll."' ";
                }
                $query .= "         ) ";
                //科目別設定について、
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ('".$subclassAll."') ";
                } else {
                    $query .= " AND T1.SUBCLASSCD NOT IN ('".$subclassAll."') ";
                }
                //選択された科目の学校種別
                $query .= "     AND T1.SCHOOL_KIND = '".$school_kind."' ";
                $db->query($query);
            }
        }

        $db->commit(); // トランザクションをコミットする。
        //DB切断
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE(管理者コントロール)
    function &getUpdateControlQuery(&$model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        //配列
        $codeLeft = explode (",", $model->selectdata2Left);
        $codeRight = explode (",", $model->selectdata2Right);

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //左
        if ($codeLeft[0] != "") {
            for ($i = 0; $i < get_count($codeLeft); $i++) {
                $data = array();
                $data["CONTROL_FLG"][TEXT]  = "1";
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                //条件
                $where  = " WHERE ";
                $where .= "     YEAR = '".$model->year."' ";
                $where .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$codeLeft[$i]}' ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= " AND CLASSCD         = '".$subclass_array[0]."' ";
                    $where .= " AND SCHOOL_KIND     = '".$subclass_array[1]."' ";
                    $where .= " AND CURRICULUM_CD   = '".$subclass_array[2]."' ";
                    $where .= " AND SUBCLASSCD      = '".$subclass_array[3]."' ";
                } else {
                    $where .= " AND SUBCLASSCD      = '".$model->field["SUBCLASSCD"]."' ";
                }
                //更新
                $query = Query::updateSQL($data, "ADMIN_CONTROL_SDIV_DAT", $where);
                $db->query($query);
            }
        }
        //右
        if ($codeRight[0] != "") {
            for ($i = 0; $i < get_count($codeRight); $i++) {
                $data = array();
                $data["CONTROL_FLG"][TEXT]  = "";
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                //条件
                $where  = " WHERE ";
                $where .= "     YEAR = '".$model->year."' ";
                $where .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$codeRight[$i]}' ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= " AND CLASSCD         = '".$subclass_array[0]."' ";
                    $where .= " AND SCHOOL_KIND     = '".$subclass_array[1]."' ";
                    $where .= " AND CURRICULUM_CD   = '".$subclass_array[2]."' ";
                    $where .= " AND SUBCLASSCD      = '".$subclass_array[3]."' ";
                } else {
                    $where .= " AND SUBCLASSCD      = '".$model->field["SUBCLASSCD"]."' ";
                }
                //更新
                $query = Query::updateSQL($data, "ADMIN_CONTROL_SDIV_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit(); // トランザクションをコミットする。
        //DB切断
        Query::dbCheckIn($db);
        return;
    }
    
//-------以下、出席コントロール、実力テストコントロールの処理-------------------------------------//

    //(左)入力可テキストボックス表示データ取得
    function selectLeftQuery($year, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->setSchoolKind."05";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $setNameCd2 = "Z009";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd2 = "Z".$model->setSchoolKind."09";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd2 = "Z".SCHOOLKIND."09";
        }
        $query  = "SELECT ";
        $query .= "    dat.control_flg AS control_flg, ";
        $query .= "    dat.control_code AS control_code, ";
        $query .= "    name.name1 AS name1, ";
        $query .= "    name.namecd2 AS namecd2, ";
        $query .= "    name.namespare1 ";
        $query .= "FROM ";
        $query .= "    admin_control_dat dat LEFT OUTER JOIN V_NAME_MST name ON dat.YEAR=name.YEAR AND name.namecd1='{$setNameCd}' AND dat.control_code=name.namecd2 ";
        $query .= "WHERE ";
        $query .= "    dat.year = '$year'  AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     dat.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "    dat.control_flg='2' ";
        $query .= "UNION ";
        $query .= "SELECT ";
        $query .= "    dat.control_flg AS control_flg, ";
        $query .= "    dat.control_code AS control_code, ";
        $query .= "    name.namecd2 || ' ' || name.name1 AS name1, ";
        $query .= "    name.namecd2 AS namecd2, ";
        $query .= "    name.namespare1 ";
        $query .= "FROM ";
        $query .= "    admin_control_dat dat LEFT OUTER JOIN V_NAME_MST name ON dat.YEAR=name.YEAR AND name.namecd1='{$setNameCd2}' AND dat.control_code=name.namecd2 ";
        $query .= "WHERE ";
        $query .= "    dat.year = '$year'  AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     dat.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "    dat.control_flg='3' ";
        $query .= "UNION ";
        $query .= "SELECT ";
        $query .= "    dat.control_flg AS control_flg, ";
        $query .= "    dat.control_code AS control_code, ";
        $query .= "    S1.SEMESTER || ' ' || S1.SEMESTERNAME AS name1, ";
        $query .= "    S1.SEMESTER AS namecd2, ";
        $query .= "    '' AS namespare1 ";
        $query .= "FROM ";
        $query .= "    admin_control_dat dat LEFT OUTER JOIN SEMESTER_MST S1 ON dat.YEAR=S1.YEAR AND dat.control_code=S1.SEMESTER ";
        $query .= "WHERE ";
        $query .= "    dat.year = '$year'  AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     dat.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "    dat.control_flg='4' AND ";
        $query .= "    S1.SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    control_flg, namespare1, control_code ";

        return $query;
    }

    //(右)入力不可テキストボックス表示データ取得
    function selectRightQuery($year, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->setSchoolKind."05";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $setNameCd2 = "Z009";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd2 = "Z".$model->setSchoolKind."09";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd2 = "Z".SCHOOLKIND."09";
        }
        $query  = " SELECT DISTINCT name.namecd1, name.namecd2, name.name1 ";
        $query .= " FROM V_NAME_MST name ";
        $query .= " WHERE YEAR = '$year' ";
        $query .= "   AND namecd1 = '{$setNameCd}' ";
        $query .= "   AND NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM ";
        $query .= "                         admin_control_dat dat ";
        $query .= "                    WHERE ";
        $query .= "                         dat.year = '$year'  AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     dat.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                         dat.control_flg = '2' AND ";
        $query .= "                         dat.control_code = name.namecd2 ) ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     name.namecd1, ";
        $query .= "     name.namecd2, ";
        $query .= "     name.namecd2 || ' ' || name.name1 AS name1 ";
        $query .= " FROM V_NAME_MST name ";
        $query .= " WHERE YEAR = '$year' ";
        $query .= "   AND namecd1 = '{$setNameCd2}' ";
        $query .= "   AND NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM ";
        $query .= "                         admin_control_dat dat ";
        $query .= "                    WHERE ";
        $query .= "                         dat.year = '$year'  AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     dat.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                         dat.control_flg = '3' AND ";
        $query .= "                         dat.control_code = name.namecd2 ) ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     'MYP_DP' AS namecd1, ";
        $query .= "     S1.SEMESTER AS namecd2, ";
        $query .= "     S1.SEMESTER || ' ' || S1.SEMESTERNAME AS name1 ";
        $query .= " FROM SEMESTER_MST S1 ";
        $query .= " WHERE S1.YEAR = '$year' ";
        $query .= "   AND S1.SEMESTER <> '9' ";
        $query .= "   AND NOT EXISTS (SELECT 'X' ";
        $query .= "                     FROM ";
        $query .= "                         admin_control_dat dat ";
        $query .= "                    WHERE ";
        $query .= "                         dat.year = '$year'  AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     dat.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                         dat.control_flg = '4' AND ";
        $query .= "                         dat.control_code = S1.SEMESTER ) ";
        $query .= " ORDER BY ";
        $query .= "     namecd1, ";
        $query .= "     namecd2 ";

        return $query;

    }

    //実力テスト表示データ
    function selectProficiencyQuery($year) {
        $query  = " SELECT ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     T1.PROFICIENCYCD, ";
        $query .= "     T1.PROFICIENCYNAME1, ";
        $query .= "     CASE WHEN L1.PROFICIENCYDIV IS NOT NULL ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS ADMIN_FLG ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST T1 ";
        $query .= "     LEFT JOIN ADMIN_CONTROL_PROFICIENCY_DAT L1 ON L1.YEAR = '{$year}' ";
        $query .= "          AND T1.PROFICIENCYDIV = L1.PROFICIENCYDIV ";
        $query .= "          AND T1.PROFICIENCYCD = L1.PROFICIENCYCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.PROFICIENCYDIV, ";
        $query .= "     T1.PROFICIENCYCD ";
        return $query;
    }

    //UPDATE
    function &getUpdateAttendProficiencyQuery(&$model)
    {
        //所属コード配列に格納
        $attend = explode (",", $model->selectdata2);
        $proficiency = explode (",", $model->selectdata3);
        $jview  = explode (",", $model->selectdataJview);
        $mypdp  = explode (",", $model->selectdataMypDp);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        //出欠入力
        if ($model->cmd === 'update3') {
            $query = "DELETE FROM admin_control_dat WHERE year = '" .$model->year ."' AND CONTROL_FLG = '2' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "                     AND SCHOOL_KIND = '".$model->setSchoolKind."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $db->query($query);

            if ($attend[0] != "") {
                for ($i = 0; $i < get_count($attend); $i++){
                    if ($model->Properties["use_prg_schoolkind"] == "1") {
                        $query  = "INSERT INTO admin_control_dat(year, SCHOOL_KIND, control_flg, control_code, registercd, updated) ";
                        $query .= "VALUES('$model->year', '".$model->setSchoolKind."', '2', '$attend[$i]', '" . STAFFCD . "', '$updated')";
                    } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $query  = "INSERT INTO admin_control_dat(year, SCHOOL_KIND, control_flg, control_code, registercd, updated) ";
                        $query .= "VALUES('$model->year', '".SCHOOLKIND."', '2', '$attend[$i]', '" . STAFFCD . "', '$updated')";
                    } else {
                        $query  = "INSERT INTO admin_control_dat(year, control_flg, control_code, registercd, updated) ";
                        $query .= "VALUES('$model->year', '2', '$attend[$i]', '" . STAFFCD . "', '$updated')";
                    }
                    $db->query($query);
                }
            }
            $db->commit(); // トランザクションをコミットする。
        }

        //実力テスト
        if ($model->cmd === 'update4') {
            $proficiency = explode (",", $model->selectdata3);
            $updated = date("Y-m-d H:i:s", time()); //更新日付
            $query = "DELETE FROM ADMIN_CONTROL_PROFICIENCY_DAT WHERE YEAR = '" .$model->year ."'";
            $db->query($query);

            if ($proficiency[0] != "") {
                for ($i = 0; $i < get_count($proficiency); $i++){
                    list($div, $cd) = preg_split("/:/", $proficiency[$i]);
                    $query  = "INSERT INTO ADMIN_CONTROL_PROFICIENCY_DAT (YEAR, PROFICIENCYDIV, PROFICIENCYCD, REGISTERCD, UPDATED) ";
                    $query .= "VALUES('$model->year', '{$div}', '{$cd}', '" . STAFFCD . "', '$updated')";
                    $db->query($query);
                }
            }
            $db->commit(); // トランザクションをコミットする。
        }
        
        //観点入力
        if ($model->cmd === 'updateJview') {
            $query = "DELETE FROM ADMIN_CONTROL_DAT WHERE YEAR = '" .$model->year ."' AND CONTROL_FLG = '3' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "                     AND SCHOOL_KIND = '".$model->setSchoolKind."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $db->query($query);

            if ($jview[0] != "") {
                for ($i = 0; $i < get_count($jview); $i++){
                    if ($model->Properties["use_prg_schoolkind"] == "1") {
                        $query  = "INSERT INTO ADMIN_CONTROL_DAT(YEAR, SCHOOL_KIND, CONTROL_FLG, CONTROL_CODE, REGISTERCD, UPDATED) ";
                        $query .= "VALUES('$model->year', '".$model->setSchoolKind."', '3', '$jview[$i]', '" . STAFFCD . "', '$updated')";
                    } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $query  = "INSERT INTO ADMIN_CONTROL_DAT(YEAR, SCHOOL_KIND, CONTROL_FLG, CONTROL_CODE, REGISTERCD, UPDATED) ";
                        $query .= "VALUES('$model->year', '".SCHOOLKIND."', '3', '$jview[$i]', '" . STAFFCD . "', '$updated')";
                    } else {
                        $query  = "INSERT INTO ADMIN_CONTROL_DAT(YEAR, CONTROL_FLG, CONTROL_CODE, REGISTERCD, UPDATED) ";
                        $query .= "VALUES('$model->year', '3', '$jview[$i]', '" . STAFFCD . "', '$updated')";
                    }
                    $db->query($query);
                }
            }
        }
        
        //ＭＹＰ・ＤＰ成績入力
        if ($model->cmd === 'updateMypDp') {
            $query = "DELETE FROM ADMIN_CONTROL_DAT WHERE YEAR = '" .$model->year ."' AND CONTROL_FLG = '4' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "                     AND SCHOOL_KIND = '".$model->setSchoolKind."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $db->query($query);

            if ($mypdp[0] != "") {
                for ($i = 0; $i < get_count($mypdp); $i++){
                    if ($model->Properties["use_prg_schoolkind"] == "1") {
                        $query  = "INSERT INTO ADMIN_CONTROL_DAT(YEAR, SCHOOL_KIND, CONTROL_FLG, CONTROL_CODE, REGISTERCD, UPDATED) ";
                        $query .= "VALUES('$model->year', '".$model->setSchoolKind."', '4', '$mypdp[$i]', '" . STAFFCD . "', '$updated')";
                    } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $query  = "INSERT INTO ADMIN_CONTROL_DAT(YEAR, SCHOOL_KIND, CONTROL_FLG, CONTROL_CODE, REGISTERCD, UPDATED) ";
                        $query .= "VALUES('$model->year', '".SCHOOLKIND."', '4', '$mypdp[$i]', '" . STAFFCD . "', '$updated')";
                    } else {
                        $query  = "INSERT INTO ADMIN_CONTROL_DAT(YEAR, CONTROL_FLG, CONTROL_CODE, REGISTERCD, UPDATED) ";
                        $query .= "VALUES('$model->year', '4', '$mypdp[$i]', '" . STAFFCD . "', '$updated')";
                    }
                    $db->query($query);
                }
            }
        }


        $db->autoCommit(true);
        Query::dbCheckIn($db);
        return;
    }

    //前年度からのコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "";
        $query .= " INSERT INTO ADMIN_CONTROL_DAT( ";
        $query .= " YEAR, ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " SCHOOL_KIND, ";
        }
        $query .= " CONTROL_FLG, ";
        $query .= " CONTROL_CODE, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "  (SELECT ";
        $query .= "       '" . CTRL_YEAR ."', ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOL_KIND, ";
        }
        $query .= "       CONTROL_FLG, ";
        $query .= "       CONTROL_CODE, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "   FROM ";
        $query .= "       ADMIN_CONTROL_DAT T0 ";
        $query .= "   WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "       not exists ( ";
        $query .= "                   SELECT 'x' FROM ADMIN_CONTROL_DAT T1 ";
        $query .= "                   WHERE ";
        $query .= "                   T1.YEAR = '" . CTRL_YEAR ."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     T1.SCHOOL_KIND = '".$model->setSchoolKind."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     T1.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                   T1.CONTROL_FLG = T0.CONTROL_FLG AND ";
        $query .= "                   T1.CONTROL_CODE = T0.CONTROL_CODE ";
        $query .= "                   ) ";
        $query .= "  ) ";

        $db->query($query);


        $query  = "";
        $query .= " INSERT INTO ADMIN_CONTROL_SDIV_DAT( ";
        $query .= " YEAR, ";
        $query .= " SEMESTER, ";
        $query .= " TESTKINDCD, ";
        $query .= " TESTITEMCD, ";
        $query .= " SCORE_DIV, ";
        $query .= " CLASSCD, ";
        $query .= " SCHOOL_KIND, ";
        $query .= " CURRICULUM_CD, ";
        $query .= " SUBCLASSCD, ";
        $query .= " CONTROL_FLG, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "  (SELECT ";
        $query .= "       '" . CTRL_YEAR ."', ";
        $query .= "       SEMESTER, ";
        $query .= "       TESTKINDCD, ";
        $query .= "       TESTITEMCD, ";
        $query .= "       SCORE_DIV, ";
        $query .= "       CLASSCD, ";
        $query .= "       SCHOOL_KIND, ";
        $query .= "       CURRICULUM_CD, ";
        $query .= "       SUBCLASSCD, ";
        $query .= "       CONTROL_FLG, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "   FROM ";
        $query .= "       ADMIN_CONTROL_SDIV_DAT T0 ";
        $query .= "   WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "       not exists ( ";
        $query .= "                   SELECT 'x' FROM ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "                   WHERE ";
        $query .= "                   T1.YEAR = '" . CTRL_YEAR ."' AND ";
        $query .= "                   T1.SEMESTER = T0.SEMESTER AND ";
        $query .= "                   T1.TESTKINDCD = T0.TESTKINDCD AND ";
        $query .= "                   T1.TESTITEMCD = T0.TESTITEMCD AND ";
        $query .= "                   T1.SCORE_DIV = T0.SCORE_DIV AND ";
        $query .= "                   T1.CLASSCD = T0.CLASSCD AND ";
        $query .= "                   T1.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
        $query .= "                   T1.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        $query .= "                   T1.SUBCLASSCD = T0.SUBCLASSCD ";
        $query .= "                   ) ";
        $query .= "  ) ";

        $db->query($query);

        $query  = "";
        $query .= " INSERT INTO ADMIN_CONTROL_PROFICIENCY_DAT( ";
        $query .= " YEAR, ";
        $query .= " PROFICIENCYDIV, ";
        $query .= " PROFICIENCYCD, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "  (SELECT ";
        $query .= "       '" . CTRL_YEAR ."', ";
        $query .= "       PROFICIENCYDIV, ";
        $query .= "       PROFICIENCYCD, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "   FROM ";
        $query .= "       ADMIN_CONTROL_PROFICIENCY_DAT T0 ";
        $query .= "   WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "       not exists ( ";
        $query .= "                   SELECT 'x' FROM ADMIN_CONTROL_PROFICIENCY_DAT T1 ";
        $query .= "                   WHERE ";
        $query .= "                   T1.YEAR = '" . CTRL_YEAR ."' AND ";
        $query .= "                   T1.PROFICIENCYDIV = T0.PROFICIENCYDIV AND ";
        $query .= "                   T1.PROFICIENCYCD = T0.PROFICIENCYCD ";
        $query .= "                   ) ";
        $query .= "  ) ";

        $db->query($query);

        $query  = "";
        $query .= " INSERT INTO ADMIN_CONTROL_CALC_SDIV_DAT( ";
        $query .= " YEAR, ";
        $query .= " SEMESTER, ";
        $query .= " TESTKINDCD, ";
        $query .= " TESTITEMCD, ";
        $query .= " SCORE_DIV, ";
        $query .= " CLASSCD, ";
        $query .= " SCHOOL_KIND, ";
        $query .= " CURRICULUM_CD, ";
        $query .= " SUBCLASSCD, ";
        $query .= " MOTO_SEMESTER, ";
        $query .= " MOTO_TESTKINDCD, ";
        $query .= " MOTO_TESTITEMCD, ";
        $query .= " MOTO_SCORE_DIV, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "  (SELECT ";
        $query .= "       '" . CTRL_YEAR ."', ";
        $query .= "       SEMESTER, ";
        $query .= "       TESTKINDCD, ";
        $query .= "       TESTITEMCD, ";
        $query .= "       SCORE_DIV, ";
        $query .= "       CLASSCD, ";
        $query .= "       SCHOOL_KIND, ";
        $query .= "       CURRICULUM_CD, ";
        $query .= "       SUBCLASSCD, ";
        $query .= "       MOTO_SEMESTER, ";
        $query .= "       MOTO_TESTKINDCD, ";
        $query .= "       MOTO_TESTITEMCD, ";
        $query .= "       MOTO_SCORE_DIV, ";
        $query .= "       '".STAFFCD."', ";
        $query .= "       SYSDATE() ";
        $query .= "   FROM ";
        $query .= "       ADMIN_CONTROL_CALC_SDIV_DAT T0 ";
        $query .= "   WHERE YEAR = '" .(CTRL_YEAR-1) ."' AND ";
        $query .= "       not exists ( ";
        $query .= "                   SELECT 'x' FROM ADMIN_CONTROL_CALC_SDIV_DAT T1 ";
        $query .= "                   WHERE ";
        $query .= "                   T1.YEAR = '" . CTRL_YEAR ."' AND ";
        $query .= "                   T1.SEMESTER = T0.SEMESTER AND ";
        $query .= "                   T1.TESTKINDCD = T0.TESTKINDCD AND ";
        $query .= "                   T1.TESTITEMCD = T0.TESTITEMCD AND ";
        $query .= "                   T1.SCORE_DIV = T0.SCORE_DIV AND ";
        $query .= "                   T1.CLASSCD = T0.CLASSCD AND ";
        $query .= "                   T1.SCHOOL_KIND = T0.SCHOOL_KIND AND ";
        $query .= "                   T1.CURRICULUM_CD = T0.CURRICULUM_CD AND ";
        $query .= "                   T1.SUBCLASSCD = T0.SUBCLASSCD AND ";
        $query .= "                   T1.MOTO_SEMESTER = T0.MOTO_SEMESTER AND ";
        $query .= "                   T1.MOTO_TESTKINDCD = T0.MOTO_TESTKINDCD AND ";
        $query .= "                   T1.MOTO_TESTITEMCD = T0.MOTO_TESTITEMCD AND ";
        $query .= "                   T1.MOTO_SCORE_DIV = T0.MOTO_SCORE_DIV ";
        $query .= "                   ) ";
        $query .= "  ) ";

        $db->query($query);


        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }
}
?>
