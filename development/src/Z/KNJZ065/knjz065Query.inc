<?php

require_once('for_php7.php');

class knjz065Query extends Query {
    //年度取得
    function getYear() {
        $query  = "  SELECT ";
        $query .= "      '".CTRL_YEAR."' AS VALUE, ";
        $query .= "      '".CTRL_YEAR."' AS LABEL ";
        $query .= "  FROM ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      YEAR AS VALUE, ";
        $query .= "      YEAR AS LABEL ";
        $query .= "  FROM ";
        $query .= "      GRADE_KIND_UNIT_GROUP_YMST ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = "  SELECT ";
        $query .= "      SEMESTER AS VALUE, ";
        $query .= "      SEMESTERNAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".$model->year."' AND ";
        $query .= "      SEMESTER <> '9' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //学部取得
    function getSchoolKind($gakubu_school_kind="") {
        $query  = " SELECT ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($gakubu_school_kind) {
            $query .= "     AND NAME1 = '".$gakubu_school_kind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //年組取得
    function getHrClass($model) {
        if ($model->hukusiki_radio == "2") {
            $query  = " SELECT DISTINCT ";
            $query .= "     L0.GHR_CD AS VALUE, ";
            $query .= "     L0.GHR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GHR_DAT T0 ";
            $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT L0 ON L0.YEAR       = T0.YEAR  ";
            $query .= "                                       AND L0.SEMESTER   = T0.SEMESTER ";
            $query .= "                                       AND L0.GHR_CD     = T0.GHR_CD, ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR       = T1.YEAR  ";
            $query .= "                                   AND T2.SEMESTER   = T1.SEMESTER  ";
            $query .= "                                   AND T2.GRADE      = T1.GRADE  ";
            $query .= "                                   AND T2.HR_CLASS   = T1.HR_CLASS ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO     = T1.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B2 ";
            $query .= "          ON B2.SCHREGNO     = T1.SCHREGNO ";
            $query .= "         AND B2.YEAR         = T1.YEAR ";
            $query .= "         AND B2.BASE_SEQ     = '004', ";
            $query .= "     SCHREG_REGD_GDAT T3 ";
            $query .= " WHERE ";
            $query .= "     T0.YEAR         = '{$model->year}' ";
            $query .= " AND T0.SEMESTER     = '{$model->regdSemester}' ";
            $query .= " AND T0.YEAR         = T1.YEAR ";
            $query .= " AND T0.SEMESTER     = T1.SEMESTER ";
            $query .= " AND T0.SCHREGNO     = T1.SCHREGNO ";
            $query .= " AND T1.YEAR         = T3.YEAR ";
            $query .= " AND T1.GRADE        = T3.GRADE ";
            $query .= " AND T3.SCHOOL_KIND  = '{$model->gakubu_school_kind}' ";
            $query .= " AND T2.GRADE || T2.HR_CLASS NOT IN (SELECT ";
            $query .= "                                         NAMECD2 || NAME1 AS HR_CLASS ";
            $query .= "                                     FROM ";
            $query .= "                                         V_NAME_MST ";
            $query .= "                                     WHERE ";
            $query .= "                                         NAMECD1 = 'A032' ";
            $query .= "                                     AND YEAR = '{$model->year}') ";
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (   L0.TR_CD1       = '".STAFFCD."' ";
                $query .= "      OR L0.TR_CD2       = '".STAFFCD."' ";
                $query .= "      OR L0.TR_CD3       = '".STAFFCD."' ";
                $query .= "      OR L0.SUBTR_CD1    = '".STAFFCD."' ";
                $query .= "      OR L0.SUBTR_CD2    = '".STAFFCD."' ";
                $query .= "      OR L0.SUBTR_CD3    = '".STAFFCD."') ";
            }
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT L1 ON L1.YEAR        = T1.YEAR  ";
            $query .= "                                  AND L1.SEMESTER    = T1.SEMESTER  ";
            $query .= "                                  AND L1.GRADE       = T1.GRADE  ";
            $query .= "                                  AND L1.HR_CLASS    = T1.HR_CLASS ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO     = L1.SCHREGNO";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B2 ";
            $query .= "          ON B2.SCHREGNO     = L1.SCHREGNO ";
            $query .= "         AND B2.YEAR         = L1.YEAR ";
            $query .= "         AND B2.BASE_SEQ     = '004', ";
            $query .= "     SCHREG_REGD_GDAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = '{$model->year}' ";
            $query .= " AND T1.SEMESTER     = '{$model->regdSemester}' ";
            $query .= " AND T1.YEAR         = T2.YEAR ";
            $query .= " AND T1.GRADE        = T2.GRADE ";
            $query .= " AND T2.SCHOOL_KIND  = '{$model->gakubu_school_kind}' ";
            $query .= " AND T1.GRADE || T1.HR_CLASS NOT IN (SELECT ";
            $query .= "                                         NAMECD2 || NAME1 AS HR_CLASS ";
            $query .= "                                     FROM ";
            $query .= "                                         V_NAME_MST ";
            $query .= "                                     WHERE ";
            $query .= "                                         NAMECD1 = 'A032' ";
            $query .= "                                     AND YEAR = '{$model->year}') ";
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (   T1.TR_CD1       = '".STAFFCD."' ";
                $query .= "      OR T1.TR_CD2       = '".STAFFCD."' ";
                $query .= "      OR T1.TR_CD3       = '".STAFFCD."' ";
                $query .= "      OR T1.SUBTR_CD1    = '".STAFFCD."' ";
                $query .= "      OR T1.SUBTR_CD2    = '".STAFFCD."' ";
                $query .= "      OR T1.SUBTR_CD3    = '".STAFFCD."') ";
            }
        }
        $query .= "     AND CASE WHEN B2.BASE_REMARK1 IS NULL THEN B1.HANDICAP ELSE B2.BASE_REMARK1 END ";
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（実クラス選択時）
    function getHrClass2($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     L1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_DAT T0 ";
        $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT L0 ON L0.YEAR       = T0.YEAR  ";
        $query .= "                                       AND L0.SEMESTER   = T0.SEMESTER ";
        $query .= "                                       AND L0.GHR_CD     = T0.GHR_CD, ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR       = T1.YEAR  ";
        $query .= "                                   AND L1.SEMESTER   = T1.SEMESTER  ";
        $query .= "                                   AND L1.GRADE      = T1.GRADE  ";
        $query .= "                                   AND L1.HR_CLASS   = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR       = T1.YEAR  ";
        $query .= "                                   AND L2.GRADE      = T1.GRADE  ";
        $query .= "                                   AND L2.SCHOOL_KIND  = '{$model->gakubu_school_kind}' ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B2 ";
        $query .= "          ON B2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND B2.YEAR         = T1.YEAR ";
        $query .= "         AND B2.BASE_SEQ     = '004' ";
        $query .= " WHERE ";
        $query .= "     T0.YEAR         = T1.YEAR ";
        $query .= " AND T0.YEAR         = '{$model->year}' ";
        $query .= " AND T0.SEMESTER     = T1.SEMESTER ";
        $query .= " AND T0.SEMESTER     = '{$model->regdSemester}' ";
        $query .= " AND T0.GHR_CD       = '{$model->hr_class}' ";
        $query .= " AND T0.SCHREGNO     = T1.SCHREGNO ";
        $query .= " AND T1.GRADE || T1.HR_CLASS NOT IN (SELECT ";
        $query .= "                                         NAMECD2 || NAME1 AS HR_CLASS ";
        $query .= "                                     FROM ";
        $query .= "                                         V_NAME_MST ";
        $query .= "                                     WHERE ";
        $query .= "                                         NAMECD1 = 'A032' ";
        $query .= "                                     AND YEAR    = '{$model->year}') ";
        $query .= " AND CASE WHEN B2.BASE_REMARK1 IS NULL THEN B1.HANDICAP ELSE B2.BASE_REMARK1 END ";
        $query .= "     IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        $query .= "     NAMECD1 = 'A033' ";
        $query .= " AND NAMECD2 = '".$condition."' ";

        return $query;
    }

    //グループコード取得
    function getGroupcd($model) {
        $query  = " SELECT ";
        $query .= "     GROUPCD || ':' || GROUPNAME AS LABEL, ";
        $query .= "     GROUPCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' ";
        $query .= " AND SEMESTER    = '".$model->semester."' ";
        $query .= " AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        if (strlen($model->hr_class) == "2") {
            $query .= " AND GHR_CD = '".$model->hr_class."' ";
            $query .= " AND GRADE || '-' || HR_CLASS = '".$model->hr_class2."' ";
        } else {
            $query .= " AND GHR_CD = '00' ";
            $query .= " AND GRADE || '-' || HR_CLASS = '".$model->hr_class."' ";
        }
        $query .= " AND CONDITION   = '".$model->condition."' ";
        $query .= " AND GROUPCD     = '".$model->groupcd."' ";

        return $query;
    }

    //科目名取得
    function getSubclassMst($model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' ' || SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->set_subclasscd."' ";

        return $query;
    }

    //リスト取得（左フレーム）
    function getList($model, $condition, $groupcd) {
        $query  = " SELECT ";
        if ($groupcd) {
            $query .= "      COUNT(*) AS CNT ";
        } else {
            $query .= "      L1.UNITCD, ";
            $query .= "      L1.UNITNAME, ";
            $query .= "      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS SUBCLASS, ";
            $query .= "      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SET_SUBCLASSCD, ";
            $query .= "      T1.* ";
        }
        $query .= "  FROM ";
        $query .= "      GRADE_KIND_COMP_GROUP_DAT T1 ";
        $query .= "      INNER JOIN GRADE_KIND_UNIT_GROUP_YMST L1 ON T1.YEAR         = L1.YEAR ";
        $query .= "                                              AND T1.SEMESTER     = L1.SEMESTER ";
        $query .= "                                              AND T1.GAKUBU_SCHOOL_KIND = L1.GAKUBU_SCHOOL_KIND ";
        $query .= "                                              AND T1.GHR_CD       = L1.GHR_CD ";
        $query .= "                                              AND T1.GRADE        = L1.GRADE ";
        $query .= "                                              AND T1.HR_CLASS     = L1.HR_CLASS ";
        $query .= "                                              AND T1.CONDITION    = L1.CONDITION ";
        $query .= "                                              AND T1.GROUPCD      = L1.GROUPCD ";
        $query .= "                                              AND T1.CLASSCD      = L1.CLASSCD ";
        $query .= "                                              AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
        $query .= "                                              AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "                                              AND T1.SUBCLASSCD   = L1.SUBCLASSCD, ";
        $query .= "      V_SUBCLASS_MST T2 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR     = '".$model->year."' ";
        $query .= "  AND T1.SEMESTER = '".$model->semester."' ";
        $query .= "  AND T1.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "  AND T1.GAKUBU_SCHOOL_KIND = T2.SCHOOL_KIND ";
        if (strlen($model->hr_class) == "2") {
            $query .= " AND T1.GHR_CD = '".$model->hr_class."' ";
            $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->hr_class2."' ";
        } else {
            $query .= " AND T1.GHR_CD = '00' ";
            $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->hr_class."' ";
        }
        if ($condition) {
            $query .= "  AND T1.CONDITION = '".$condition."' ";
        }
        if ($groupcd) {
            $query .= "  AND T1.GROUPCD = '".$groupcd."' ";
        }
        $query .= "  AND T1.YEAR             = T2.YEAR ";
        $query .= "  AND T1.CLASSCD          = T2.CLASSCD ";
        $query .= "  AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
        $query .= "  AND T1.CURRICULUM_CD    = T2.CURRICULUM_CD ";
        $query .= "  AND T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= "  AND UNIT_AIM_DIV        = '1' ";
        if ($condition == "" && $groupcd == "") {
            $query .= "  ORDER BY ";
            $query .= "      T1.CONDITION, ";
            $query .= "      T1.GROUPCD, ";
            $query .= "      T1.GAKUBU_SCHOOL_KIND, ";
            $query .= "      T1.SCHOOL_KIND, ";
            $query .= "      T1.CURRICULUM_CD, ";
            $query .= "      T1.CLASSCD, ";
            $query .= "      T1.SUBCLASSCD ";
        }

        return $query;
    }

    //GRADE_KIND_UNIT_GROUP_YMST取得
    function getUnitGroupYmst($model) {
        $db = Query::dbCheckOut();
        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->set_subclasscd);
        $query  = "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      GRADE_KIND_UNIT_GROUP_YMST ";
        $query .= "  WHERE ";
        $query .= "      YEAR           = '".$model->year."' ";
        $query .= "  AND SEMESTER       = '".$model->semester."' ";
        $query .= "  AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        if (strlen($model->hr_class) == "2") {
            $query .= " AND GHR_CD = '".$model->hr_class."' ";
            $query .= " AND GRADE || '-' || HR_CLASS = '".$model->hr_class2."' ";
        } else {
            $query .= " AND GHR_CD = '00' ";
            $query .= " AND GRADE || '-' || HR_CLASS = '".$model->hr_class."' ";
        }
        $query .= "  AND CONDITION      = '".$model->condition."' ";
        $query .= "  AND GROUPCD        = '".$model->groupcd."' ";
        $query .= "  AND CLASSCD        = '".$classcd."' ";
        $query .= "  AND SCHOOL_KIND    = '".$school_kind."' ";
        $query .= "  AND CURRICULUM_CD  = '".$curriculum_cd."' ";
        $query .= "  AND SUBCLASSCD     = '".$subclasscd."' ";
        if ($model->unitcd) {
            $query .= "  AND UNITCD         = '".$model->unitcd."' ";
        }
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //更新
    function &UpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (strlen($model->hr_class) == "2") {
            $ghr_cd = $model->hr_class;
            list ($grade, $hr_class) = explode('-', $model->hr_class2);
        } else {
            $ghr_cd = '00';
            list ($grade, $hr_class) = explode('-', $model->hr_class);
        }

        //削除
        $query  = " DELETE FROM  ";
        $query .= "     GRADE_KIND_UNIT_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' ";
        $query .= " AND SEMESTER    = '".$model->semester."' ";
        $query .= " AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= " AND GHR_CD      = '".$ghr_cd."' ";
        $query .= " AND GRADE       = '".$grade."' ";
        $query .= " AND HR_CLASS    = '".$hr_class."' ";
        $query .= " AND CONDITION   = '".$model->condition."' ";
        $query .= " AND GROUPCD     = '".$model->groupcd."' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->set_subclasscd."' ";
        $query .= " AND UNITCD      = '".$model->field["UNITCD"]."' ";

        $db->query($query);
        //追加
        if ($model->cmd == "update") {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->set_subclasscd);
            
            $data = array();
            $data["YEAR"][TEXT]                 = $model->year;
            $data["SEMESTER"][TEXT]             = $model->semester;
            $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $model->gakubu_school_kind;
            $data["GHR_CD"][TEXT]               = $ghr_cd;
            $data["GRADE"][TEXT]                = $grade;
            $data["HR_CLASS"][TEXT]             = $hr_class;
            $data["CONDITION"][TEXT]            = $model->condition;
            $data["GROUPCD"][TEXT]              = $model->groupcd;
            $data["CLASSCD"][TEXT]              = $classcd;
            $data["SCHOOL_KIND"][TEXT]          = $school_kind;
            $data["CURRICULUM_CD"][TEXT]        = $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]           = $subclasscd;
            $data["UNITCD"][TEXT]               = $model->field["UNITCD"];
            $data["UNITNAME"][TEXT]             = $model->field["UNITNAME"];
            $data["UNITABBV"][TEXT]             = $model->field["UNITABBV"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "GRADE_KIND_UNIT_GROUP_YMST"));
        }
        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //存在チェック（コピー） -- GRADE_KIND_COMP_GROUP_DAT
    function checkCompGroupDat($model, $year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            $query .= "     AND SEMESTER <> '9' ";
        } else {
            $query .= "     AND SEMESTER = '9' ";
        }
        return $query;
    }

    //存在チェック（コピー） -- GRADE_KIND_UNIT_GROUP_YMST
    function checkUnitGroupDat($model, $year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_UNIT_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            $query .= "     AND SEMESTER <> '9' ";
        } else {
            $query .= "     AND SEMESTER = '9' ";
        }

        return $query;
    }

    //コピー用データ取得
    function copyUnitGroupYMst($model) {
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
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            $query .= "     INNER JOIN SEMESTER_MST T3 ON T3.YEAR = T2.YEAR AND ";
            $query .= "         T1.SEMESTER = T3.SEMESTER ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".intval($model->year - 1)."' ";
        if ($model->Properties["useGradeKindCompGroupSemester"] != "1") {
            $query .= "     AND T1.SEMESTER = '9' ";
        }
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
