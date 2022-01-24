<?php

require_once('for_php7.php');

class knjz350aQuery extends Query {
    
    function selectSchoolKindQuery($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.ABBV1 AS LABEL, ";
        $query .= "     T1.NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == '1') {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     INT(ABBV2) ";
        return $query;
    }

    //(左)入力可テキストボックス表示データ取得
    function selectListQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclassAll);
        }
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || CASE WHEN T1.TESTKINDCD = '99' THEN '00' ELSE '01' END AS VALUE, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || ':' || SEM_M.SEMESTERNAME || '-' || T1.TESTITEMNAME AS LABEL, ";
        $query .= "     CASE WHEN T1.SEMESTER != '9' AND T1.TESTKINDCD = '99' THEN '1' END AS GAKKI_HYOUKA_FLG, ";
        $query .= "     CASE WHEN T2.SCORE_DIV IS NOT NULL THEN '1' END AS PARTS_FLG, ";
        $query .= "     T2.CONTROL_FLG ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW T1 ";
        $query .= "     INNER JOIN SEMESTER_MST SEM_M ";
        $query .= "         ON  SEM_M.YEAR = T1.YEAR ";
        $query .= "         AND SEM_M.SEMESTER = T1.SEMESTER ";
        $query .= "     LEFT JOIN ADMIN_CONTROL_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV ";
        $query .= "           = T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || CASE WHEN T1.TESTKINDCD = '99' THEN '00' ELSE '01' END ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND T2.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND T2.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND T2.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND T2.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND T2.SUBCLASSCD     = '".$model->subclassAll."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
//echo $query;
        return $query;
    }

    //UPDATE(パーツ)
    function &getUpdateQuery(&$model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclassAll);
        }
        //配列
        $codeArray = array();
        $codeArray = explode (",", $model->selectdata);

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query = "DELETE FROM ADMIN_CONTROL_SDIV_DAT WHERE YEAR = '".$model->year."'";
        $db->query($query);

        //追加
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
                    $data["SUBCLASSCD"][TEXT]       = $model->subclassAll;
                }
                $data["CONTROL_FLG"][TEXT]  = "";
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                //追加
                $query = Query::insertSQL($data, "ADMIN_CONTROL_SDIV_DAT");
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
            $subclass_array = explode("-", $model->subclassAll);
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
                    $where .= " AND SUBCLASSCD      = '".$model->subclassAll."' ";
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
                    $where .= " AND SUBCLASSCD      = '".$model->subclassAll."' ";
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
            $setNameCd = "Z".$model->field["SEL_SCHOOL_KIND"]."05";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $setNameCd2 = "Z009";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd2 = "Z".$model->field["SEL_SCHOOL_KIND"]."09";
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
        $query .= "    admin_control_dat dat LEFT OUTER JOIN V_NAME_MST name ON dat.YEAR=name.YEAR AND name.namecd1 = '{$setNameCd}' AND dat.control_code=name.namecd2 ";
        $query .= "WHERE ";
        $query .= "    dat.year = '$year'  AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->field["SEL_SCHOOL_KIND"]."' AND ";
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
        $query .= "    admin_control_dat dat LEFT OUTER JOIN V_NAME_MST name ON dat.YEAR=name.YEAR AND name.namecd1 = '{$setNameCd2}' AND dat.control_code=name.namecd2 ";
        $query .= "WHERE ";
        $query .= "    dat.year = '$year'  AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                     dat.SCHOOL_KIND = '".$model->field["SEL_SCHOOL_KIND"]."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     dat.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "    dat.control_flg='3' ";
        $query .= "ORDER BY ";
        $query .= "    control_flg, namespare1, control_code ";

        return $query;
    }

    //(右)入力不可テキストボックス表示データ取得
    function selectRightQuery($year, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->field["SEL_SCHOOL_KIND"]."05";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $setNameCd2 = "Z009";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd2 = "Z".$model->field["SEL_SCHOOL_KIND"]."09";
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
            $query .= "                     dat.SCHOOL_KIND = '".$model->field["SEL_SCHOOL_KIND"]."' AND ";
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
            $query .= "                     dat.SCHOOL_KIND = '".$model->field["SEL_SCHOOL_KIND"]."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     dat.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                         dat.control_flg = '3' AND ";
        $query .= "                         dat.control_code = name.namecd2 ) ";
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

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $schoolKind = "";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $schoolKind .= $model->field["SEL_SCHOOL_KIND"];
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOKIND != "") {
            $schoolKind .= SCHOOLKIND;
        }
        
        //出欠入力
        if ($model->cmd === 'update3') {
            $query = "DELETE FROM admin_control_dat WHERE year = '" .$model->year ."' AND control_flg = '2' ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "                     AND SCHOOL_KIND = '".$schoolKind."' ";
            }
            $db->query($query);

            if ($attend[0] != "") {
                for ($i = 0; $i < get_count($attend); $i++){
                    if ($model->Properties["useSchool_KindField"] == "1") {
                        $query  = "INSERT INTO admin_control_dat(year, SCHOOL_KIND, control_flg, control_code, registercd, updated) ";
                        $query .= "VALUES('$model->year', '".$schoolKind."', '2', '$attend[$i]', '" . STAFFCD . "', '$updated')";
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
            $query = "DELETE FROM admin_control_dat WHERE year = '" .$model->year ."' AND control_flg = '3' ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "                     AND SCHOOL_KIND = '".$schoolKind."' ";
            }
            $db->query($query);

            if ($jview[0] != "") {
                for ($i = 0; $i < get_count($jview); $i++){
                    if ($model->Properties["useSchool_KindField"] == "1") {
                        $query  = "INSERT INTO admin_control_dat(year, SCHOOL_KIND, control_flg, control_code, registercd, updated) ";
                        $query .= "VALUES('$model->year', '".$schoolKind."', '3', '$jview[$i]', '" . STAFFCD . "', '$updated')";
                    } else {
                        $query  = "INSERT INTO admin_control_dat(year, control_flg, control_code, registercd, updated) ";
                        $query .= "VALUES('$model->year', '3', '$jview[$i]', '" . STAFFCD . "', '$updated')";
                    }
                    $db->query($query);
                }
            }
        }

        $db->autoCommit(true);
        Query::dbCheckIn($db);
        return;
    }
}
?>
