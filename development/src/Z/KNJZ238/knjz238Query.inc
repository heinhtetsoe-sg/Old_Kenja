<?php

require_once('for_php7.php');

class knjz238Query extends Query {
    //科目コードコンボ
    function getSubclassCd($model) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL, ";
            $query .= "     SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //左フレームのリスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.SPECIAL_GROUP_CD, ";
        $query .= "     L1.SPECIAL_GROUP_NAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }         
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L2.SUBCLASSNAME, ";
        $query .= "     T1.MINUTES ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_MST L1 ON L1.SPECIAL_GROUP_CD = T1.SPECIAL_GROUP_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND L2.CLASSCD       = T1.CLASSCD ";
            $query .= "                 AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                 AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }         
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SPECIAL_GROUP_CD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }         
        $query .= "     T1.SUBCLASSCD ";
        

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getSpecialCnt($model, $group_cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR             = '". CTRL_YEAR ."' ";
        $query .= "     AND SPECIAL_GROUP_CD = '{$group_cd}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //ATTEND_SUBCLASS_SPECIAL_DAT から１レコード取得
    function getAttendSubclassSpecial($special_group_cd, $subclasscd, $model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_MST L1 ON L1.SPECIAL_GROUP_CD = T1.SPECIAL_GROUP_CD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR             = '". CTRL_YEAR ."' ";
        $query .= "     AND T1.SPECIAL_GROUP_CD = '{$special_group_cd}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD       = '{$model->field["CLASSCD"]}' ";
            $query .= "     AND T1.SCHOOL_KIND   = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= "     AND T1.CURRICULUM_CD = '{$model->field["CURRICULUM_CD"]}' ";
        } 
        $query .= "     AND T1.SUBCLASSCD       = '{$subclasscd}' ";

        return $query;
    }

    //更新の時にレコードが存在するのかチェックする
    function getAttendSubclassSpecialDat($model) {
        //教育課程用
        $subclasscd = substr($model->field["SUBCLASSCD"], 7, 6);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR             = '". CTRL_YEAR ."' ";
        $query .= "     AND SPECIAL_GROUP_CD = '{$model->field["SPECIAL_GROUP_CD"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD         = '{$model->field["CLASSCD"]}' ";
            $query .= " AND SCHOOL_KIND     = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= " AND CURRICULUM_CD   = '{$model->field["CURRICULUM_CD"]}' ";
            $query .= " AND SUBCLASSCD      = '{$subclasscd}' ";
        } else {         
            $query .= "     AND SUBCLASSCD  = '{$model->field["SUBCLASSCD"]}' ";
        }
        
        return $query;
    }

    //更新の時にレコードが存在するのかチェックする
    function getAttendSubclassSpecialDat_insert($model) {
        //教育課程用
        $subclasscd = substr($model->field["SUBCLASSCD"], 7, 6);
        
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR             = '". CTRL_YEAR ."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD         = '{$model->field["CLASSCD"]}' ";
            $query .= " AND SCHOOL_KIND     = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= " AND CURRICULUM_CD   = '{$model->field["CURRICULUM_CD"]}' ";
            $query .= " AND SUBCLASSCD      = '{$subclasscd}' ";
        } else {         
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }

        return $query;
    }

    //更新の時にレコードが存在するのかチェックする
    function getAttendSubclassSpecialMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_MST ";
        $query .= " WHERE ";
        $query .= "     SPECIAL_GROUP_CD = '{$model->field["SPECIAL_GROUP_CD"]}' ";

        return $query;
    }

    //前年度コピーの時のチェック
    function cntAttendSubclassSpecialDat($model, $year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    //ATTEND_SUBCLASS_SPECIAL_MST を更新
    function &getInsertMstQuery($model) {
        $db = Query::dbCheckOut();

        $data["SPECIAL_GROUP_CD"][TEXT]   = $model->field["SPECIAL_GROUP_CD"];
        $data["SPECIAL_GROUP_NAME"][TEXT] = $model->field["SPECIAL_GROUP_NAME"];
        $data["SPECIAL_GROUP_ABBV"][TEXT] = $model->field["SPECIAL_GROUP_ABBV"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $query = Query::insertSQL($data, "ATTEND_SUBCLASS_SPECIAL_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //ATTEND_SUBCLASS_SPECIAL_DAT を更新
    function &getInsertDatQuery($model) {
        $db = Query::dbCheckOut();
        //教育課程用
        $subclasscd = substr($model->field["SUBCLASSCD"], 7, 6);

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SPECIAL_GROUP_CD"][TEXT] = $model->field["SPECIAL_GROUP_CD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $model->field["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]    = $model->field["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]  = $model->field["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]     = $subclasscd;
        } else {         
            $data["SUBCLASSCD"][TEXT]     = $model->field["SUBCLASSCD"];
        }
        $data["MINUTES"][TEXT]          = $model->field["MINUTES"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "ATTEND_SUBCLASS_SPECIAL_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* UPDATE */
    /**********/
    //ATTEND_SUBCLASS_SPECIAL_MST を更新
    function &getUpdateMstQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["SPECIAL_GROUP_CD"][TEXT]   = $model->field["SPECIAL_GROUP_CD"];
        $data["SPECIAL_GROUP_NAME"][TEXT] = $model->field["SPECIAL_GROUP_NAME"];
        $data["SPECIAL_GROUP_ABBV"][TEXT] = $model->field["SPECIAL_GROUP_ABBV"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SPECIAL_GROUP_CD = '{$model->field["SPECIAL_GROUP_CD"]}' ";

        $query = Query::updateSQL($data, "ATTEND_SUBCLASS_SPECIAL_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //ATTEND_SUBCLASS_SPECIAL_DAT を更新
    function &getUpdateDatQuery($model) {
        $db = Query::dbCheckOut();
        //教育課程用
        $subclasscd = substr($model->field["SUBCLASSCD"], 7, 6);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SPECIAL_GROUP_CD"][TEXT] = $model->field["SPECIAL_GROUP_CD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $model->field["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]    = $model->field["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]  = $model->field["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]     = $subclasscd;
        } else {         
            $data["SUBCLASSCD"][TEXT]     = $model->field["SUBCLASSCD"];
        }
        $data["MINUTES"][TEXT]          = $model->field["MINUTES"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "         YEAR             = '". CTRL_YEAR ."' ";
        $where .= "     AND SPECIAL_GROUP_CD = '{$model->field["SPECIAL_GROUP_CD"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= "     AND CLASSCD         = '{$model->field["CLASSCD"]}' ";
            $where .= "     AND SCHOOL_KIND     = '{$model->field["SCHOOL_KIND"]}' ";
            $where .= "     AND CURRICULUM_CD   = '{$model->field["CURRICULUM_CD"]}' ";
            $where .= "     AND SUBCLASSCD      = '{$subclasscd}' ";
        } else {
            $where .= "     AND SUBCLASSCD       = '{$model->field["SUBCLASSCD"]}' ";
        }

        $query = Query::updateSQL($data, "ATTEND_SUBCLASS_SPECIAL_DAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        //教育課程用
        $subclasscd = substr($model->field["SUBCLASSCD"], 7, 6);

        //ATTEND_SUBCLASS_SPECIAL_DAT から削除
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR             = '". CTRL_YEAR ."' ";
        $query .= "     AND SPECIAL_GROUP_CD = '{$model->field["SPECIAL_GROUP_CD"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD         = '{$model->field["CLASSCD"]}' ";
            $query .= " AND SCHOOL_KIND     = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= " AND CURRICULUM_CD   = '{$model->field["CURRICULUM_CD"]}' ";
            $query .= " AND SUBCLASSCD      = '{$subclasscd}' ";
        } else {         
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }

        $db->query($query);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_DAT ";
        $query .= " WHERE ";
        $query .= "     SPECIAL_GROUP_CD = '{$model->field["SPECIAL_GROUP_CD"]}' ";

        //ATTEND_SUBCLASS_SPECIAL_DAT が0件になっていたら
        //ATTEND_SUBCLASS_SPECIAL_MST も削除する
        $cnt = $db->getOne($query);
        if ($cnt <= 0) {
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_SUBCLASS_SPECIAL_MST ";
            $query .= " WHERE ";
            $query .= "     SPECIAL_GROUP_CD = '{$model->field["SPECIAL_GROUP_CD"]}' ";

            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

    /********/
    /* COPY */
    /********/
    function getInsertCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $fromYear = CTRL_YEAR - 1;
        $toYear   = CTRL_YEAR;

        $query  = " INSERT INTO ATTEND_SUBCLASS_SPECIAL_DAT ";
        $query .= " SELECT ";
        $query .= "      '{$toYear}', ";
        $query .= "      SPECIAL_GROUP_CD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      CLASSCD, ";
            $query .= "      SCHOOL_KIND, ";
            $query .= "      CURRICULUM_CD, ";
        }
        $query .= "      SUBCLASSCD, ";
        $query .= "      MINUTES, ";
        $query .= "      registercd, ";
        $query .= "      SYSDATE()";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_SPECIAL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$fromYear}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
