<?php

require_once('for_php7.php');
class knjz064mQuery extends Query {
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
        $query  = " SELECT ";
        if ($groupcd) {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.YEAR ";
            $query .= "     , T1.SEMESTER ";
            $query .= "     , T1.GAKUBU_SCHOOL_KIND ";
            $query .= "     , T1.GHR_CD ";
            $query .= "     , T1.GRADE ";
            $query .= "     , T1.HR_CLASS ";
            $query .= "     , T1.CONDITION ";
            $query .= "     , T1.GROUPCD ";
            $query .= "     , T1.GROUPNAME ";
            $query .= "     , T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD || ' ' || T3.SUBCLASSNAME AS SUBCLASS ";
            $query .= "     , T2.CLASSCD ";
            $query .= "     , T2.SCHOOL_KIND ";
            $query .= "     , T2.CURRICULUM_CD ";
            $query .= "     , T2.SUBCLASSCD ";
            $query .= "     , T2.UNIT_AIM_DIV ";
        }
        $query .= " FROM ";
        $query .= "      GRADE_KIND_COMP_GROUP_YMST T1 ";
        $query .= "      LEFT JOIN GRADE_KIND_COMP_GROUP_DAT T2 ";
        $query .= "          ON  T1.YEAR               = T2.YEAR ";
        $query .= "          AND T1.SEMESTER           = T2.SEMESTER ";
        $query .= "          AND T1.GAKUBU_SCHOOL_KIND = T2.GAKUBU_SCHOOL_KIND ";
        $query .= "          AND T1.GHR_CD             = T2.GHR_CD ";
        $query .= "          AND T1.GRADE              = T2.GRADE ";
        $query .= "          AND T1.HR_CLASS           = T2.HR_CLASS ";
        $query .= "          AND T1.CONDITION          = T2.CONDITION ";
        $query .= "          AND T1.GROUPCD            = T2.GROUPCD ";
        $query .= "      LEFT JOIN V_SUBCLASS_MST T3 ";
        $query .= "          ON  T1.YEAR               = T3.YEAR ";
        $query .= "          AND T2.CLASSCD            = T3.CLASSCD ";
        $query .= "          AND T2.SCHOOL_KIND        = T3.SCHOOL_KIND ";
        $query .= "          AND T2.CURRICULUM_CD      = T3.CURRICULUM_CD ";
        $query .= "          AND T2.SUBCLASSCD         = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                   = '".$model->year."' ";
        $query .= "     AND T1.SEMESTER           = '".$model->semester."' ";
        $query .= "     AND T1.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "     AND T1.GHR_CD             = '".$model->ghr_cd."' ";
        $query .= "     AND T1.GRADE              = '".$model->grade."' ";
        $query .= "     AND T1.HR_CLASS           = '".$model->hr_class."' ";
        if ($condition) {
            $query .= " AND T1.CONDITION        = '".$condition."' ";
        }
        if ($groupcd) {
            $query .= " AND T1.GROUPCD          = '".$groupcd."' ";
        }
        if ($condition == "" && $groupcd == "") {
            $query .= " ORDER BY ";
            $query .= "     T1.CONDITION ";
            $query .= "     , T1.GROUPCD ";
            $query .= "     , T1.GHR_CD ";
            $query .= "     , T1.GRADE ";
            $query .= "     , T1.HR_CLASS ";
        }
        return $query;
    }

    //GRADE_KIND_COMP_GROUP_YMST取得
    function getCompGroupYMst($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     HR_CLASS ";
        $query .= "     , CONDITION ";
        $query .= "     , GROUPCD ";
        $query .= "     , GROUPNAME ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR                   = '".$model->year."' ";
        $query .= "     AND SEMESTER           = '".$model->semester."' ";
        $query .= "     AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "     AND GHR_CD             = '".$model->ghr_cd."' ";
        $query .= "     AND GRADE              = '".$model->grade."' ";
        $query .= "     AND HR_CLASS           = '".$model->hr_class."' ";
        $query .= "     AND CONDITION          = '".$model->condition."' ";
        $query .= "     AND GROUPCD            = '".$model->groupcd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
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

    //GRADE_KIND_COMP_GROUP_DAT取得
    function getCompGroupDat($model, $setcondition, $setgroupcd) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.CLASSCD || '-' || T1.SUBCLASSCD AS VALUE, ";
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
        $query .= "     AND T1.CONDITION          = '".$setcondition."' ";
        $query .= "     AND T1.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "     AND T1.GHR_CD             = '".$model->ghr_cd."' ";
        $query .= "     AND T1.GRADE              = '".$model->grade."' ";
        $query .= "     AND T1.HR_CLASS           = '".$model->hr_class."' ";
        $query .= "     AND T1.GROUPCD            = '".$setgroupcd."' ";
        $query .= "     AND T1.SCHOOL_KIND        = '".$model->gakubu_school_kind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目マスタ取得
    function getGradeKindCompSubclassMst($model, $setcondition) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.CLASSCD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_DETAIL_MST T2 ";
        $query .= "         ON T1.CLASSCD           = T2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD    = T2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = '".$model->year."' ";
        $query .= "     AND T1.SCHOOL_KIND      = '".$model->gakubu_school_kind."' ";
        $query .= "     AND T2.SUBCLASS_SEQ     = '001' ";
        $query .= "     AND T2.SUBCLASS_REMARK1 = '".$setcondition."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //個別の指導計画（単元設定）取得
    function selectGradeKindUnitGroupYMst($model) {

        $query  = "";
        $query .= " SELECT * ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_UNIT_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR                        = '".$model->year."' ";
        $query .= " AND SEMESTER                    = '".$model->semester."' ";
        $query .= " AND GAKUBU_SCHOOL_KIND          = '".$model->gakubu_school_kind."' ";
        $query .= " AND GHR_CD                      = '".$model->ghr_cd."' ";
        $query .= " AND GRADE                       = '".$model->grade."' ";
        $query .= " AND HR_CLASS                    = '".$model->hr_class."' ";
        $query .= " AND CONDITION                   = '".$model->field["CONDITION"]."' ";
        $query .= " AND RIGHT('0000' || GROUPCD, 4) = '".$model->field["GROUPCD"]."' ";

        return $query;
    }

    //個別の指導計画（生徒登録）取得
    function selectGradeKindSchregGroupDat($model) {

        $query  = "";
        $query .= " SELECT * ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                        = '".$model->year."' ";
        $query .= " AND SEMESTER                    = '".$model->semester."' ";
        $query .= " AND GAKUBU_SCHOOL_KIND          = '".$model->gakubu_school_kind."' ";
        $query .= " AND GHR_CD                      = '".$model->ghr_cd."' ";
        $query .= " AND GRADE                       = '".$model->grade."' ";
        $query .= " AND HR_CLASS                    = '".$model->hr_class."' ";
        $query .= " AND CONDITION                   = '".$model->field["CONDITION"]."' ";
        $query .= " AND RIGHT('0000' || GROUPCD, 4) = '".$model->field["GROUPCD"]."' ";

        return $query;
    }

    //更新
    function &UpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        /********************************/
        /*  GRADE_KIND_COMP_GROUP_YMST  */
        /********************************/
        //削除
        if ($model->cmd != "insert") {
            $query  = " DELETE FROM  ";
            $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
            $query .= " WHERE ";
            $query .= "     YEAR                        = '".$model->year."' ";
            $query .= " AND SEMESTER                    = '".$model->semester."' ";
            $query .= " AND GAKUBU_SCHOOL_KIND          = '".$model->gakubu_school_kind."' ";
            $query .= " AND GHR_CD                      = '".$model->ghr_cd."' ";
            $query .= " AND GRADE                       = '".$model->grade."' ";
            $query .= " AND HR_CLASS                    = '".$model->hr_class."' ";
            $query .= " AND CONDITION                   = '".$model->field["CONDITION"]."' ";
            $query .= " AND RIGHT('0000' || GROUPCD, 4) = '".$model->field["GROUPCD"]."' ";

            $db->query($query);
        }

        //追加
        if ($model->cmd != "delete") {
            $data = array();
            $data["YEAR"][TEXT]                 = $model->year;
            $data["SEMESTER"][TEXT]             = $model->semester;
            $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $model->gakubu_school_kind;
            $data["GHR_CD"][TEXT]               = $model->ghr_cd;
            $data["GRADE"][TEXT]                = $model->grade;
            $data["HR_CLASS"][TEXT]             = $model->hr_class;
            $data["CONDITION"][TEXT]            = $model->field["CONDITION"];
            $data["GROUPCD"][TEXT]              = $model->field["GROUPCD"];
            $data["GROUPNAME"][TEXT]            = $model->field["GROUPNAME"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "GRADE_KIND_COMP_GROUP_YMST"));
        }

        /*******************************/
        /*  GRADE_KIND_COMP_GROUP_DAT  */
        /*******************************/
        //削除
        if ($model->cmd != "insert") {
            $query  = " DELETE FROM  ";
            $query .= "     GRADE_KIND_COMP_GROUP_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR                = '".$model->year."'  ";
            $query .= " AND SEMESTER            = '".$model->semester."' ";
            $query .= " AND GAKUBU_SCHOOL_KIND  = '".$model->gakubu_school_kind."' ";
            $query .= " AND GHR_CD              = '".$model->ghr_cd."' ";
            $query .= " AND GRADE               = '".$model->grade."' ";
            $query .= " AND HR_CLASS            = '".$model->hr_class."' ";
            $query .= " AND CONDITION           = '".$model->field["CONDITION"]."' ";
            $query .= " AND RIGHT('0000' || GROUPCD, 4) = '".$model->field["GROUPCD"]."' ";

            $db->query($query);
        }

        //追加
        $selectdata = explode(',', $model->selectdata);
        if ($model->selectdata) {
            for ($i=0; $i < get_count($selectdata); $i++) {
                list($school_kind, $curriculum_cd, $classcd, $subclasscd) = explode("-", $selectdata[$i]);

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
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "sysdate()";

                $db->query(Query::insertSQL($data, "GRADE_KIND_COMP_GROUP_DAT"));
            }
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return;
    }

    //存在チェック（コピー） -- GRADE_KIND_COMP_GROUP_YMST
    function checkCompGroupYMst($model, $year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SEMESTER = '9' ";

        return $query;
    }

    //存在チェック（コピー） -- GRADE_KIND_COMP_GROUP_DAT
    function checkCompGroupDat($model, $year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SEMESTER = '9' ";

        return $query;
    }

    //コピー用データ取得
    function copyCompGroupDat($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GROUPNAME, ";
        $query .= "     T1.GROUPABBV, ";
        $query .= "     T1.GUIDANCE_PATTERN, ";
        $query .= "     T2.* ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST T1 ";
        $query .= "     INNER JOIN GRADE_KIND_COMP_GROUP_DAT T2 ON T2.YEAR = T1.YEAR AND ";
        $query .= "         T1.SEMESTER         = T2.SEMESTER AND ";
        $query .= "         T1.GAKUBU_SCHOOL_KIND = T2.GAKUBU_SCHOOL_KIND AND ";
        $query .= "         T1.GHR_CD           = T2.GHR_CD AND ";
        $query .= "         T1.GRADE            = T2.GRADE AND ";
        $query .= "         T1.HR_CLASS         = T2.HR_CLASS AND ";
        $query .= "         T1.CONDITION        = T2.CONDITION AND ";
        $query .= "         T1.GROUPCD          = T2.GROUPCD ";
        $query .= "     INNER JOIN V_SUBCLASS_MST T3 ON T3.YEAR = '".$model->year."' AND ";
        $query .= "         T2.CLASSCD          = T3.CLASSCD AND ";
        $query .= "         T2.SCHOOL_KIND      = T3.SCHOOL_KIND AND ";
        $query .= "         T2.CURRICULUM_CD    = T3.CURRICULUM_CD AND ";
        $query .= "         T2.SUBCLASSCD       = T3.SUBCLASSCD ";
        $query .= "     INNER JOIN SUBCLASS_DETAIL_MST T5 ON T2.CLASSCD = T5.CLASSCD AND ";
        $query .= "         T2.SCHOOL_KIND      = T5.SCHOOL_KIND AND ";
        $query .= "         T2.CURRICULUM_CD    = T5.CURRICULUM_CD AND ";
        $query .= "         T2.SUBCLASSCD       = T5.SUBCLASSCD AND ";
        $query .= "         T5.SUBCLASS_SEQ     = '001' AND ";
        $query .= "         T1.CONDITION        = T5.SUBCLASS_REMARK1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".intval($model->year - 1)."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= " ORDER BY ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     T2.GAKUBU_SCHOOL_KIND, ";
        $query .= "     T2.GHR_CD, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.CONDITION, ";
        $query .= "     T2.GROUPCD ";

        return $query;
    }

    //コピー    -- GRADE_KIND_COMP_GROUP_YMST
    function &copyInsertQueryYMst($model, $Row) {
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SEMESTER"][TEXT]             = $Row["SEMESTER"];
        $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $Row["GAKUBU_SCHOOL_KIND"];
        $data["GHR_CD"][TEXT]               = $Row["GHR_CD"];
        $data["GRADE"][TEXT]                = $Row["GRADE"];
        $data["HR_CLASS"][TEXT]             = $Row["HR_CLASS"];
        $data["CONDITION"][TEXT]            = $Row["CONDITION"];
        $data["GROUPCD"][TEXT]              = $Row["GROUPCD"];
        $data["GROUPNAME"][TEXT]            = $Row["GROUPNAME"];
        $data["GROUPABBV"][TEXT]            = $Row["GROUPABBV"];
        $data["GUIDANCE_PATTERN"][TEXT]     = $Row["GUIDANCE_PATTERN"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "GRADE_KIND_COMP_GROUP_YMST");
        return $query;
    }

    //コピー    -- GRADE_KIND_COMP_GROUP_DAT
    function &copyInsertQueryDat($model, $Row) {
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
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "GRADE_KIND_COMP_GROUP_DAT");
        return $query;
    }
}
?>
