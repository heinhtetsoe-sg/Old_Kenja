<?php

require_once('for_php7.php');

class knjz065mQuery extends Query {
    //校種取得
    function getSchkind($model, $gakubuSchoolKind = "") {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        if ($gakubuSchoolKind) {
            $query .= "     AND NAME1 = '".$model->gakubu_school_kind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //リスト取得（左フレーム）
    function getList($model, $condition, $groupcd) {
        if ($groupcd) {
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GAKUBU_SCHOOL_KIND ";
            $query .= "     , T1.GHR_CD ";
            $query .= "     , T1.GRADE ";
            $query .= "     , T1.HR_CLASS ";
            $query .= "     , T1.CONDITION ";
            $query .= "     , T1.GROUPCD ";
            $query .= "     , T2.GROUPNAME ";
            $query .= "     , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T3.SUBCLASSNAME AS SUBCLASS ";
            $query .= "     , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SET_SUBCLASSCD ";
            $query .= "     , T1.SCHOOL_KIND ";
            $query .= "     , T1.CURRICULUM_CD ";
            $query .= "     , T1.CLASSCD ";
            $query .= "     , T1.SUBCLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT T1 ";
        $query .= "     INNER JOIN GRADE_KIND_COMP_GROUP_YMST T2 ";
        $query .= "         ON  T1.YEAR               = T2.YEAR ";
        $query .= "         AND T1.SEMESTER           = T2.SEMESTER ";
        $query .= "         AND T1.GAKUBU_SCHOOL_KIND = T2.GAKUBU_SCHOOL_KIND ";
        $query .= "         AND T1.GHR_CD             = T2.GHR_CD ";
        $query .= "         AND T1.GRADE              = T2.GRADE ";
        $query .= "         AND T1.HR_CLASS           = T2.HR_CLASS ";
        $query .= "         AND T1.CONDITION          = T2.CONDITION ";
        $query .= "         AND T1.GROUPCD            = T2.GROUPCD ";
        $query .= "     LEFT JOIN GRADE_KIND_UNIT_GROUP_YMST L1 ";
        $query .= "         ON  T1.YEAR                = L1.YEAR ";
        $query .= "         AND T1.SEMESTER            = L1.SEMESTER ";
        $query .= "         AND T1.GAKUBU_SCHOOL_KIND  = L1.GAKUBU_SCHOOL_KIND ";
        $query .= "         AND T1.GHR_CD              = L1.GHR_CD ";
        $query .= "         AND T1.GRADE               = L1.GRADE ";
        $query .= "         AND T1.HR_CLASS            = L1.HR_CLASS ";
        $query .= "         AND T1.CONDITION           = L1.CONDITION ";
        $query .= "         AND T1.GROUPCD             = L1.GROUPCD ";
        $query .= "         AND T1.CLASSCD             = L1.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND         = L1.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD       = L1.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD          = L1.SUBCLASSCD ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST T3 ";
        $query .= "         ON  T1.YEAR               = T3.YEAR ";
        $query .= "         AND T1.CLASSCD            = T3.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND        = T3.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD      = T3.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD         = T3.SUBCLASSCD ";
        $query .= "         AND T1.GAKUBU_SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                   = '".$model->year."' ";
        $query .= "     AND T1.SEMESTER           = '".$model->semester."' ";
        $query .= "     AND T1.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "     AND T1.GHR_CD             = '".$model->ghr_cd."' ";
        $query .= "     AND T1.GRADE              = '".$model->grade."' ";
        $query .= "     AND T1.HR_CLASS           = '".$model->hr_class."' ";
        if ($condition) {
            $query .= "     AND T1.CONDITION = '".$condition."' ";
        }
        if ($groupcd) {
            $query .= "     AND T1.GROUPCD = '".$groupcd."' ";
        }
        if ($condition == "" && $groupcd == "") {
            $query .= " ORDER BY ";
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.GROUPCD, ";
            $query .= "     T1.GAKUBU_SCHOOL_KIND, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SUBCLASSCD ";
        }

        return $query;
    }

    //状態区分取得
    function getCondition($condition="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1     = 'A033' AND ";
        $query .= "     NAMESPARE3  = '1' ";
        if ($condition) {
            $query .= "     AND NAMECD2 = '".$condition."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //GRADE_KIND_UNIT_GROUP_YMST取得
    function getUnitGroupYmst($model, $ghrCd, $grade, $hrClass, $condition, $groupCd, $subclass, $unitcd = "") {
        $query  = " SELECT ";
        if ($unitcd) {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     UNITCD ";
            $query .= "     , UNITNAME ";
        }
        $query .= " FROM ";
        $query .= "     GRADE_KIND_UNIT_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SEMESTER = '".$model->semester."' ";
        $query .= "     AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "     AND GHR_CD = '".$ghrCd."' ";
        $query .= "     AND GRADE = '".$grade."' ";
        $query .= "     AND HR_CLASS = '".$hrClass."' ";
        $query .= "     AND CONDITION = '".$condition."' ";
        $query .= "     AND GROUPCD = '".$groupCd."' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclass."' ";
        if ($unitcd) {
            $query .= "     AND UNITCD = '".$unitcd."' ";
        }

        return $query;
    }

    //GRADE_KIND_UNIT_GROUP_YMST取得
    function getUnitGroupYmstRow($model) {
        $db = Query::dbCheckOut();

        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->set_subclasscd);

        $query  = " SELECT ";
        $query .= "     CONDITION ";
        $query .= "     , GROUPCD ";
        $query .= "     , CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SET_SUBCLASSCD ";
        $query .= "     , UNITCD ";
        $query .= "     , UNITNAME ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_UNIT_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR                   = '".$model->year."' ";
        $query .= "     AND SEMESTER           = '".$model->semester."' ";
        $query .= "     AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "     AND GHR_CD             = '".$model->ghr_cd."' ";
        $query .= "     AND GRADE              = '".$model->grade."' ";
        $query .= "     AND HR_CLASS           = '".$model->hr_class."' ";
        $query .= "     AND CONDITION          = '".$model->condition."' ";
        $query .= "     AND GROUPCD            = '".$model->groupcd."' ";
        $query .= "     AND CLASSCD            = '".$classcd."' ";
        $query .= "     AND SCHOOL_KIND        = '".$school_kind."' ";
        $query .= "     AND CURRICULUM_CD      = '".$curriculum_cd."' ";
        $query .= "     AND SUBCLASSCD         = '".$subclasscd."' ";
        $query .= "     AND UNITCD             = '".$model->unitcd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //科目グループ名コンボ取得
    function getGroupNameCombo($model, $condition) {
        $query  = " SELECT ";
        $query .= "     GROUPCD AS VALUE ";
        $query .= "     , GROUPCD || ' ' || GROUPNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SEMESTER = '".$model->semester."' ";
        $query .= "     AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "     AND GHR_CD = '".$model->ghr_cd."' ";
        $query .= "     AND GRADE = '".$model->grade."' ";
        $query .= "     AND HR_CLASS = '".$model->hr_class."' ";
        $query .= "     AND CONDITION = '".$condition."' ";

        return $query;
    }

    //科目コンボ取得
    function getSubclassCombo($model, $condition, $groupcd) {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT T1 ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST T2 ";
        $query .= "         ON  T1.YEAR             = T2.YEAR ";
        $query .= "         AND T1.CLASSCD          = T2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD    = T2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                   = '".$model->year."' ";
        $query .= "     AND T1.SEMESTER           = '".$model->semester."' ";
        $query .= "     AND T1.CONDITION          = '".$condition."' ";
        $query .= "     AND T1.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "     AND T1.GHR_CD             = '".$model->ghr_cd."' ";
        $query .= "     AND T1.GRADE              = '".$model->grade."' ";
        $query .= "     AND T1.HR_CLASS           = '".$model->hr_class."' ";
        $query .= "     AND T1.GROUPCD            = '".$groupcd."' ";
        $query .= "     AND T1.SCHOOL_KIND        = '".$model->gakubu_school_kind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //更新
    function &UpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        if ($model->cmd != "insert") {
            $query  = " DELETE FROM  ";
            $query .= "     GRADE_KIND_UNIT_GROUP_YMST ";
            $query .= " WHERE ";
            $query .= "     YEAR               = '".$model->year."' ";
            $query .= " AND SEMESTER           = '".$model->semester."' ";
            $query .= " AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
            $query .= " AND GHR_CD             = '".$model->ghr_cd."' ";
            $query .= " AND GRADE              = '".$model->grade."' ";
            $query .= " AND HR_CLASS           = '".$model->hr_class."' ";
            $query .= " AND CONDITION          = '".$model->condition."' ";
            $query .= " AND GROUPCD            = '".$model->groupcd."' ";
            $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->set_subclasscd."' ";
            $query .= " AND UNITCD             = '".$model->field["UNITCD"]."' ";

            $db->query($query);
        }

        //追加
        if ($model->cmd == "insert" || $model->cmd == "update") {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->field["SET_SUBCLASSCD"]);

            $data = array();
            $data["YEAR"][TEXT]                 = $model->year;
            $data["SEMESTER"][TEXT]             = $model->semester;
            $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $model->gakubu_school_kind;
            $data["GHR_CD"][TEXT]               = $model->ghr_cd;
            $data["GRADE"][TEXT]                = $model->grade;
            $data["HR_CLASS"][TEXT]             = $model->hr_class;
            $data["CONDITION"][TEXT]            = $model->field["CONDITION"];
            $data["GROUPCD"][TEXT]              = $model->field["GROUPCD"];
            $data["CLASSCD"][TEXT]              = $classcd;
            $data["SCHOOL_KIND"][TEXT]          = $school_kind;
            $data["CURRICULUM_CD"][TEXT]        = $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]           = $subclasscd;
            $data["UNITCD"][TEXT]               = $model->field["UNITCD"];
            $data["UNITNAME"][TEXT]             = $model->field["UNITNAME"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "GRADE_KIND_UNIT_GROUP_YMST"));
        }
        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return;
    }

    //存在チェック（コピー） -- GRADE_KIND_COMP_GROUP_DAT
    function checkCompGroupDat($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SEMESTER = '9' ";

        return $query;
    }

    //存在チェック（コピー） -- GRADE_KIND_UNIT_GROUP_YMST
    function checkUnitGroupDat($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_UNIT_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SEMESTER = '9' ";

        return $query;
    }

    //コピー用データ取得
    function copyUnitGroupYmst($model) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_UNIT_GROUP_YMST T1 ";
        $query .= "     INNER JOIN GRADE_KIND_COMP_GROUP_DAT T2 ON T2.YEAR         = '".$model->year."' AND ";
        $query .= "         T1.SEMESTER     = T2.SEMESTER AND ";
        $query .= "         T1.GAKUBU_SCHOOL_KIND = T2.GAKUBU_SCHOOL_KIND AND ";
        $query .= "         T1.GHR_CD       = T2.GHR_CD AND ";
        $query .= "         T1.GRADE        = T2.GRADE AND ";
        $query .= "         T1.HR_CLASS     = T2.HR_CLASS AND ";
        $query .= "         T1.CONDITION    = T2.CONDITION AND ";
        $query .= "         T1.GROUPCD      = T2.GROUPCD AND ";
        $query .= "         T1.CLASSCD      = T2.CLASSCD AND ";
        $query .= "         T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
        $query .= "         T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        $query .= "         T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".intval($model->year - 1)."' ";
        $query .= "     AND T1.SEMESTER = '9' ";

        return $query;
    }

    //コピー
    function &copyInsertQuery($model, $Row) {
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SEMESTER"][TEXT]             = $Row["SEMESTER"];
        $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $Row["GAKUBU_SCHOOL_KIND"];
        $data["GHR_CD"][TEXT]               = $Row["GHR_CD"];
        $data["GRADE"][TEXT]                = $Row["GRADE"];
        $data["HR_CLASS"][TEXT]             = $Row["HR_CLASS"];
        $data["CONDITION"][TEXT]            = $Row["CONDITION"];
        $data["GROUPCD"][TEXT]              = $Row["GROUPCD"];
        $data["CLASSCD"][TEXT]              = $Row["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]          = $Row["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]        = $Row["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]           = $Row["SUBCLASSCD"];
        $data["UNITCD"][TEXT]               = $Row["UNITCD"];
        $data["UNITNAME"][TEXT]             = $Row["UNITNAME"];
        $data["UNITABBV"][TEXT]             = $Row["UNITABBV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "GRADE_KIND_UNIT_GROUP_YMST");
        return $query;
    }
}
?>
