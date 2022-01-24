<?php

require_once('for_php7.php');


class knjb0200Query extends Query {

    //学年
    function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種
    function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND T1.GRADE = '" .$model->setGrade. "' ";

        return $query;
    }

    //設定科目
    function getSetSubclass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || T1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND DESC, ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧
    function getSubclass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || T1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CURRICULUM_CD DESC, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //条件取得
    function getJouken($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.CONDITION_SEQ, ";
        $query .= "     T1.CONDITION, ";
        $query .= "     T1.CONDITION_NUM ";
        $query .= " FROM ";
        $query .= "     COMP_CONDITION_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->setSubclassCd}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        $query .= "     AND T1.CONDITION_DIV = '{$model->type_div}' ";
        $query .= "     AND T1.GRADE = '{$model->setGrade}' ";
        $query .= "     AND T1.HR_CLASS = '000' ";
        $query .= "     AND T1.COURSECD = '0' ";
        $query .= "     AND T1.MAJORCD = '000' ";
        $query .= "     AND T1.COURSECODE = '0000' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CONDITION_SEQ ";

        return $query;
    }

    //科目名
    function getSubclassName($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.CON_CLASSCD || '-' || T1.CON_SCHOOL_KIND || '-' || T1.CON_CURRICULUM_CD || '-' || T1.CON_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.HEIKOU_FLG, ";
        $query .= "     T1.CREDITS, ";
        $query .= "     L1.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     COMP_CONDITION_SUBCLASS_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON T1.CON_CLASSCD || T1.CON_SCHOOL_KIND || T1.CON_CURRICULUM_CD || T1.CON_SUBCLASSCD = L1.CLASSCD || L1.SCHOOL_KIND || L1.CURRICULUM_CD || L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->setSubclassCd}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        $query .= "     AND T1.CONDITION_DIV = '{$model->type_div}' ";
        $query .= "     AND T1.GRADE = '{$model->setGrade}' ";
        $query .= "     AND T1.HR_CLASS = '000' ";
        $query .= "     AND T1.COURSECD = '0' ";
        $query .= "     AND T1.MAJORCD = '000' ";
        $query .= "     AND T1.COURSECODE = '0000' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CON_CLASSCD, ";
        $query .= "     T1.CON_SCHOOL_KIND, ";
        $query .= "     T1.CON_CURRICULUM_CD, ";
        $query .= "     T1.CON_SUBCLASSCD ";

        return $query;
    }

    //DELETE(更新処理)
    function getDeleteQuery($model)
    {

        $query  = " DELETE FROM ";
        $query .= "     COMP_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->setSubclassCd}' ";
        $query .= "     AND CONDITION_DIV = '{$model->type_div}' ";
        $query .= "     AND GRADE = '{$model->setGrade}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
        }

        return $query;
    }

    //UPDATE(更新処理)
    function getUpdateQuery($db, $model, $setData)
    {

        list($classCd, $schoolKind, $curriculum, $subclassCd) = preg_split("/-/", $model->setSubclassCd);

        foreach ($setData as $key => $val) {
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["CLASSCD"][TEXT]          = $classCd;
            $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculum;
            $data["SUBCLASSCD"][TEXT]       = $subclassCd;
            $data["CONDITION_SEQ"][NUMBER]  = $key;
            $data["CONDITION_DIV"][TEXT]    = $model->type_div;
            $data["GRADE"][TEXT]            = $model->setGrade;
            $data["HR_CLASS"][TEXT]         = "000";
            $data["COURSECD"][TEXT]         = "0";
            $data["MAJORCD"][TEXT]          = "000";
            $data["COURSECODE"][TEXT]       = "0000";
            $data["CONDITION"][TEXT]        = $val["CONDITION"];
            $data["CONDITION_NUM"][TEXT]    = $val["CONDITION_NUM"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = Query::insertSQL($data, "COMP_CONDITION_DAT");
            $db->query($query);
        }

    }

    //DELETE(更新処理)
    function getDeleteSubQuery($model)
    {

        $query  = " DELETE FROM ";
        $query .= "     COMP_CONDITION_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->setSubclassCd}' ";
        $query .= "     AND CONDITION_DIV = '{$model->type_div}' ";
        $query .= "     AND GRADE = '{$model->setGrade}' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
        }

        return $query;
    }

    //UPDATE科目(更新処理)
    function getUpdateSubQuery($db, $model)
    {

        $subclassArray = preg_split("/,/", $model->selectleft);
        list($classCd, $schoolKind, $curriculum, $subclassCd) = preg_split("/-/", $model->setSubclassCd);

        foreach ($subclassArray as $key => $val) {
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["CLASSCD"][TEXT]              = $classCd;
            $data["SCHOOL_KIND"][TEXT]          = $schoolKind;
            $data["CURRICULUM_CD"][TEXT]        = $curriculum;
            $data["SUBCLASSCD"][TEXT]           = $subclassCd;
            $data["CONDITION_DIV"][TEXT]        = $model->type_div;
            $data["GRADE"][TEXT]                = $model->setGrade;
            $data["HR_CLASS"][TEXT]             = "000";
            $data["COURSECD"][TEXT]             = "0";
            $data["MAJORCD"][TEXT]              = "000";
            $data["COURSECODE"][TEXT]           = "0000";
            list($conClassCd, $conSchoolKind, $conCurriculum, $conSubclassCd) = preg_split("/-/", $val);
            $data["CON_CLASSCD"][TEXT]          = $conClassCd;
            $data["CON_SCHOOL_KIND"][TEXT]      = $conSchoolKind;
            $data["CON_CURRICULUM_CD"][TEXT]    = $conCurriculum;
            $data["CON_SUBCLASSCD"][TEXT]       = $conSubclassCd;
            if ($model->type_div == "1") {
                $data["HEIKOU_FLG"][TEXT]           = $model->heikouTouroku[$val];
            } else {
                $data["CREDITS"][NUMBER]            = $model->heikouTouroku[$val];
            }
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $query = Query::insertSQL($data, "COMP_CONDITION_SUBCLASS_DAT");
            $db->query($query);
        }

    }

    //UPDATE科目(更新処理)
    function getUpdateHeikouFlgQuery($db, $model)
    {

        list($classCd, $schoolKind, $curriculum, $subclassCd) = preg_split("/-/", $model->setSubclassCd);

        foreach ($model->heikouTouroku as $key => $val) {
            if ($model->type_div == "1") {
                $data["HEIKOU_FLG"][TEXT]           = $val;
            } else {
                $data["CREDITS"][NUMBER]            = $val;
            }
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            list($conClassCd, $conSchoolKind, $conCurriculum, $conSubclassCd) = preg_split("/-/", $key);
            $where  = " WHERE ";
            $where .= "     YEAR = '" .CTRL_YEAR. "' ";
            $where .= "     AND CLASSCD = '{$classCd}' ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $where .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
            }
            $where .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
            $where .= "     AND CURRICULUM_CD = '{$curriculum}' ";
            $where .= "     AND SUBCLASSCD = '{$subclassCd}' ";
            $where .= "     AND CONDITION_DIV = '{$model->type_div}' ";
            $where .= "     AND GRADE = '{$model->setGrade}' ";
            $where .= "     AND HR_CLASS = '000' ";
            $where .= "     AND COURSECD = '0' ";
            $where .= "     AND MAJORCD = '000' ";
            $where .= "     AND COURSECODE = '0000' ";
            $where .= "     AND CON_CLASSCD = '{$conClassCd}' ";
            $where .= "     AND CON_SCHOOL_KIND = '{$conSchoolKind}' ";
            $where .= "     AND CON_CURRICULUM_CD = '{$conCurriculum}' ";
            $where .= "     AND CON_SUBCLASSCD = '{$conSubclassCd}' ";

            $query = Query::updateSQL($data, "COMP_CONDITION_SUBCLASS_DAT", $where);
            $db->query($query);
        }

    }

    //ログイン年度データカウント
    function cntThisOrder($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COMP_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
        }

        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);

        return $data_check;
    }

    //前年度データカウント
    function cntLastOrder($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COMP_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR - 1)."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
        }

        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);

        return $data_check;
    }

    //前年度からコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO COMP_CONDITION_DAT ";
        $query .= "           ( YEAR, ";
        $query .= "             CLASSCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             CURRICULUM_CD, ";
        $query .= "             SUBCLASSCD, ";
        $query .= "             CONDITION_SEQ, ";
        $query .= "             CONDITION_DIV, ";
        $query .= "             GRADE, ";
        $query .= "             HR_CLASS, ";
        $query .= "             COURSECD, ";
        $query .= "             MAJORCD, ";
        $query .= "             COURSECODE, ";
        $query .= "             CONDITION, ";
        $query .= "             CONDITION_NUM, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         CONDITION_SEQ, ";
        $query .= "         CONDITION_DIV, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
        $query .= "         CONDITION, ";
        $query .= "         CONDITION_NUM, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COMP_CONDITION_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".(CTRL_YEAR - 1)."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        $query .= "     ) ";

        $db->query($query);

        //科目
        $query  = " DELETE FROM ";
        $query .= "     COMP_CONDITION_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR. "' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND (SCHOOL_KIND = '".$model->schoolKind."' ";
            $query .= "  OR CON_SCHOOL_KIND = '".$model->schoolKind."') ";
        }

        $db->query($query);

        $query  = " INSERT INTO COMP_CONDITION_SUBCLASS_DAT ";
        $query .= "           ( YEAR, ";
        $query .= "             CLASSCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             CURRICULUM_CD, ";
        $query .= "             SUBCLASSCD, ";
        $query .= "             CONDITION_DIV, ";
        $query .= "             GRADE, ";
        $query .= "             HR_CLASS, ";
        $query .= "             COURSECD, ";
        $query .= "             MAJORCD, ";
        $query .= "             COURSECODE, ";
        $query .= "             CON_CLASSCD, ";
        $query .= "             CON_SCHOOL_KIND, ";
        $query .= "             CON_CURRICULUM_CD, ";
        $query .= "             CON_SUBCLASSCD, ";
        $query .= "             HEIKOU_FLG, ";
        $query .= "             CREDITS, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         CONDITION_DIV, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
        $query .= "         CON_CLASSCD, ";
        $query .= "         CON_SCHOOL_KIND, ";
        $query .= "         CON_CURRICULUM_CD, ";
        $query .= "         CON_SUBCLASSCD, ";
        $query .= "         HEIKOU_FLG, ";
        $query .= "         CREDITS, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COMP_CONDITION_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".(CTRL_YEAR - 1)."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
            $query .= " AND CON_SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        $query .= "     ) ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

}
?>
