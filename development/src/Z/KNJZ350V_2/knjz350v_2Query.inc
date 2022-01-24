<?php

require_once('for_php7.php');

class knjz350v_2Query extends Query {

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
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND T1.SCHOOL_KIND = '".$model->sendSchoolKind."' ";
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
    
    //算出先取得
    function selectTestQuery($db, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || SEM_M.SEMESTERNAME || '-' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST SEM_M ";
        $query .= "         ON  SEM_M.YEAR = T1.YEAR ";
        $query .= "         AND SEM_M.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["SAKI_TESTCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //表示データ取得
    function selectListQuery($db, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.NOT_AUTO_KANSAN_FLG, ";
        $query .= "     T1.KANSAN_AFTER_UPDATE_FLG ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_KANSAN_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["SAKI_TESTCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD     = '".$model->field["SUBCLASSCD"]."' ";
        }
        return $query;
    }

    //UPDATE(パーツ)
    function &getUpdateQuery(&$model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = "DELETE FROM ADMIN_CONTROL_KANSAN_SDIV_DAT WHERE YEAR = '".$model->year."'";
        $query .= "         AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->field["SAKI_TESTCD"]."' ";
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

        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        $data["SEMESTER"][TEXT]         = substr($model->field["SAKI_TESTCD"], 0, 1);
        $data["TESTKINDCD"][TEXT]       = substr($model->field["SAKI_TESTCD"], 1, 2);
        $data["TESTITEMCD"][TEXT]       = substr($model->field["SAKI_TESTCD"], 3, 2);
        $data["SCORE_DIV"][TEXT]        = substr($model->field["SAKI_TESTCD"], 5, 2);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]          = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
        }
        $data["NOT_AUTO_KANSAN_FLG"][TEXT]      = $model->field["NOT_AUTO_KANSAN_FLG"];
        $data["KANSAN_AFTER_UPDATE_FLG"][TEXT]  = $model->field["KANSAN_AFTER_UPDATE_FLG"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "ADMIN_CONTROL_KANSAN_SDIV_DAT");
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        //DB切断
        Query::dbCheckIn($db);
        return;
    }
}
?>
