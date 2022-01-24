<?php

require_once('for_php7.php');

class knjz064nQuery extends Query
{
    //年度取得
    public function getYear()
    {
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
        $query .= "      GRADE_KIND_COMP_GROUP_YMST ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //学期取得
    public function getSemester($model)
    {
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
    public function getSchoolKind($gakubu_school_kind = "")
    {
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

    //クラスコンボ
    public function getHrClass($model)
    {
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
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 IN ('1', '2', '3')) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラスコンボ
    public function getHrClass2($model)
    {
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
        $query .= "     IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 IN ('1', '2', '3')) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //状態区分取得
    public function getCondition($condition = "")
    {
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

    //指導計画帳票パターン
    public function getGuidancePattern($model, $Row)
    {
        $query  = " SELECT ";
        $query .= "     T1.GUIDANCE_PATTERN || ':' || N1.NAME1 AS LABEL, ";
        $query .= "     T1.GUIDANCE_PATTERN AS VALUE ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT T1 ";
        $query .= "     INNER JOIN V_NAME_MST N1 ON N1.YEAR = T1.YEAR ";
        $query .= "                             AND N1.NAMECD1 = 'A035' ";
        $query .= "                             AND N1.NAMECD2 = T1.GUIDANCE_PATTERN ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " AND T1.SEMESTER = '".$model->semester."' ";
        $query .= " AND T1.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= " AND T1.CONDITION = '".$Row["CONDITION"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト取得（左フレーム）
    public function getList($model, $condition, $groupcd)
    {
        $query  = " SELECT ";
        if ($groupcd) {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.GROUPNAME, ";
            $query .= "     T1.GUIDANCE_PATTERN, ";
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.GROUPCD, ";
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD || ' ' || T3.SUBCLASSNAME AS SUBCLASS, ";
            // $query .= "     T2.* ";
            $query .= "     T2.YEAR, ";
            $query .= "     T2.SEMESTER, ";
            $query .= "     T2.GAKUBU_SCHOOL_KIND, ";
            $query .= "     T2.GHR_CD, ";
            $query .= "     T2.GRADE, ";
            $query .= "     T2.HR_CLASS, ";
            // $query .= "     T2.CONDITION, ";
            // $query .= "     GROUPCD, ";
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
            $query .= "     T2.SUBCLASSCD, ";
            $query .= "     T2.UNIT_AIM_DIV ";
        }
        $query .= " FROM ";
        $query .= "      GRADE_KIND_COMP_GROUP_YMST T1 ";
        $query .= "      LEFT JOIN GRADE_KIND_COMP_GROUP_DAT T2 ";
        $query .= "          ON T1.YEAR             = T2.YEAR ";
        $query .= "         AND T1.SEMESTER         = T2.SEMESTER ";
        $query .= "         AND T1.GAKUBU_SCHOOL_KIND = T2.GAKUBU_SCHOOL_KIND ";
        $query .= "         AND T1.GHR_CD           = T2.GHR_CD ";
        $query .= "         AND T1.GRADE            = T2.GRADE ";
        $query .= "         AND T1.HR_CLASS         = T2.HR_CLASS ";
        $query .= "         AND T1.CONDITION        = T2.CONDITION ";
        $query .= "         AND T1.GROUPCD          = T2.GROUPCD ";
        $query .= "      LEFT JOIN V_SUBCLASS_MST T3 ";
        $query .= "          ON T1.YEAR             = T3.YEAR ";
        $query .= "         AND T2.CLASSCD          = T3.CLASSCD ";
        $query .= "         AND T2.SCHOOL_KIND      = T3.SCHOOL_KIND ";
        $query .= "         AND T2.CURRICULUM_CD    = T3.CURRICULUM_CD ";
        $query .= "         AND T2.SUBCLASSCD       = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".$model->year."' AND ";
        $query .= "     T1.SEMESTER         = '".$model->semester."' AND ";
        $query .= "     T1.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' AND ";
        if (strlen($model->hr_class) == "2") {
            $query .= "     T1.GHR_CD = '".$model->hr_class."' AND ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->hr_class2."' AND ";
        } else {
            $query .= "     T1.GHR_CD = '00' AND ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->hr_class."' AND ";
        }
        $query .= "     VALUE(T2.SCHOOL_KIND, '".$model->gakubu_school_kind."') = '".$model->gakubu_school_kind."' ";
        if ($condition) {
            $query .= " AND T2.CONDITION        = '".$condition."' ";
        }
        if ($groupcd) {
            $query .= " AND T2.GROUPCD          = '".$groupcd."' ";
        }
        if ($condition == "" && $groupcd == "") {
            $query .= " ORDER BY ";
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.GROUPCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SUBCLASSCD ";
        }
        return $query;
    }

    //GRADE_KIND_COMP_GROUP_YMST取得
    public function getCompGroupYMst($model)
    {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     * ";
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

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //GRADE_KIND_COMP_GROUP_DAT取得
    public function getCompGroupDat($model, $setcondition, $setgroupcd)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.CLASSCD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT T1, ";
        $query .= "     V_SUBCLASS_MST T2, ";
        $query .= "     SUBCLASS_DETAIL_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".$model->year."' ";
        $query .= " AND T1.SEMESTER         = '".$model->semester."' ";
        $query .= " AND T1.CONDITION        = '".$setcondition."' ";
        $query .= " AND T1.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        if (strlen($model->hr_class) == "2") {
            $query .= " AND T1.GHR_CD = '".$model->hr_class."' ";
            $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->hr_class2."' ";
        } else {
            $query .= " AND T1.GHR_CD = '00' ";
            $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->hr_class."' ";
        }
        $query .= " AND T1.GROUPCD          = '".$setgroupcd."' ";
        $query .= " AND T1.SCHOOL_KIND      = '".$model->gakubu_school_kind."' ";
        $query .= " AND T1.YEAR             = T2.YEAR ";
        $query .= " AND T1.CLASSCD          = T2.CLASSCD ";
        $query .= " AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
        $query .= " AND T1.CURRICULUM_CD    = T2.CURRICULUM_CD ";
        $query .= " AND T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= " AND T1.CLASSCD          = T3.CLASSCD ";
        $query .= " AND T1.SCHOOL_KIND      = T3.SCHOOL_KIND ";
        $query .= " AND T1.CURRICULUM_CD    = T3.CURRICULUM_CD ";
        $query .= " AND T1.SUBCLASSCD       = T3.SUBCLASSCD ";
        $query .= " AND T3.SUBCLASS_SEQ     = '001' ";
        $query .= " AND T1.CONDITION        = T3.SUBCLASS_REMARK1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目マスタ取得
    public function getGradeKindCompSubclassMst($model, $setcondition)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.CLASSCD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1, ";
        $query .= "     SUBCLASS_DETAIL_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".$model->year."' ";
        $query .= " AND T1.SCHOOL_KIND      = '".$model->gakubu_school_kind."' ";
        $query .= " AND T1.CLASSCD          = T2.CLASSCD ";
        $query .= " AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
        $query .= " AND T1.CURRICULUM_CD    = T2.CURRICULUM_CD ";
        $query .= " AND T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= " AND T2.SUBCLASS_SEQ     = '001' ";
        $query .= " AND T2.SUBCLASS_REMARK1 = '".$setcondition."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getFixGuidancePattern($model)
    {
        if (strlen($model->hr_class) == "2") {
            $ghr_cd = $model->hr_class;
            list($grade, $hr_class) = explode('-', $model->hr_class2);
        } else {
            $ghr_cd = '00';
            list($grade, $hr_class) = explode('-', $model->hr_class);
        }
        $query  = " SELECT GUIDANCE_PATTERN FROM GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$model->year."' ";
        $query .= "   AND SEMESTER = '".$model->semester."' ";
        $query .= "   AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "   AND GHR_CD = '".$ghr_cd."' ";
        $query .= "   AND GRADE = '".$grade."' ";
        $query .= "   AND HR_CLASS = '".$hr_class."' ";
        $query .= "   AND CONDITION = '".$model->field["CONDITION"]."' ";
        $query .= "   AND GROUPCD = '".$model->field["GROUPCD"]."' ";

        return $query;
    }

    public function getOtherGuidancePattern($model)
    {
        $query  = " SELECT DISTINCT GUIDANCE_PATTERN FROM GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$model->year."' ";
        $query .= "   AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "   AND CONDITION = '".$model->field["CONDITION"]."' ";
        $query .= "   AND GUIDANCE_PATTERN IS NOT NULL ";

        return $query;
    }

    //更新
    public function &updateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (strlen($model->hr_class) == "2") {
            $ghr_cd = $model->hr_class;
            list($grade, $hr_class) = explode('-', $model->hr_class2);
        } else {
            $ghr_cd = '00';
            list($grade, $hr_class) = explode('-', $model->hr_class);
        }

        /********************************/
        /*  GRADE_KIND_COMP_GROUP_YMST  */
        /********************************/
        //削除
        $query  = " DELETE FROM  ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."' ";
        $query .= " AND SEMESTER            = '".$model->semester."' ";
        $query .= " AND GAKUBU_SCHOOL_KIND  = '".$model->gakubu_school_kind."' ";
        $query .= " AND GHR_CD              = '".$ghr_cd."' ";
        $query .= " AND GRADE               = '".$grade."' ";
        $query .= " AND HR_CLASS            = '".$hr_class."' ";
        $query .= " AND CONDITION           = '".$model->field["CONDITION"]."' ";
        $query .= " AND RIGHT('0000' || GROUPCD, 4) = '".$model->field["GROUPCD"]."' ";

        $db->query($query);

        //追加
        // if ($model->selectdata) {
        if ($model->cmd != "delete") {
            $data = array();
            $data["YEAR"][TEXT]                 = $model->year;
            $data["SEMESTER"][TEXT]             = $model->semester;
            $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $model->gakubu_school_kind;
            $data["GHR_CD"][TEXT]               = $ghr_cd;
            $data["GRADE"][TEXT]                = $grade;
            $data["HR_CLASS"][TEXT]             = $hr_class;
            $data["CONDITION"][TEXT]            = $model->field["CONDITION"];
            $data["GROUPCD"][TEXT]              = $model->field["GROUPCD"];
            $data["GROUPNAME"][TEXT]            = $model->field["GROUPNAME"];
            $data["GROUPABBV"][TEXT]            = $model->field["GROUPABBV"];
            $data["GUIDANCE_PATTERN"][TEXT]     = $model->field["GUIDANCE_PATTERN"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "GRADE_KIND_COMP_GROUP_YMST"));
        }

        /*******************************/
        /*  GRADE_KIND_COMP_GROUP_DAT  */
        /*******************************/
        //削除
        $query  = " DELETE FROM  ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."'  ";
        $query .= " AND SEMESTER            = '".$model->semester."' ";
        $query .= " AND GAKUBU_SCHOOL_KIND  = '".$model->gakubu_school_kind."' ";
        $query .= " AND GHR_CD              = '".$ghr_cd."' ";
        $query .= " AND GRADE               = '".$grade."' ";
        $query .= " AND HR_CLASS            = '".$hr_class."' ";
        $query .= " AND CONDITION           = '".$model->field["CONDITION"]."' ";
        $query .= " AND RIGHT('0000' || GROUPCD, 4) = '".$model->field["GROUPCD"]."' ";

        $db->query($query);

        //追加
        $selectdata = explode(',', $model->selectdata);
        if ($model->selectdata) {
            for ($i=0; $i < get_count($selectdata); $i++) {
                list($school_kind, $curriculum_cd, $classcd, $subclasscd) = explode("-", $selectdata[$i]);

                $data = array();
                $data["YEAR"][TEXT]                 = $model->year;
                $data["SEMESTER"][TEXT]             = $model->semester;
                $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $model->gakubu_school_kind;
                $data["GHR_CD"][TEXT]               = $ghr_cd;
                $data["GRADE"][TEXT]                = $grade;
                $data["HR_CLASS"][TEXT]             = $hr_class;
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

        /*******************************/
        /*  GRADE_KIND_SCHREG_GROUP_DAT*/
        /*******************************/
        if (!$model->selectdata) {
            //削除
            $query  = " DELETE FROM  ";
            $query .= "     GRADE_KIND_SCHREG_GROUP_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR                = '".$model->year."'  ";
            $query .= " AND SEMESTER            = '".$model->semester."' ";
            $query .= " AND GAKUBU_SCHOOL_KIND  = '".$model->gakubu_school_kind."' ";
            $query .= " AND GHR_CD              = '".$ghr_cd."' ";
            $query .= " AND GRADE               = '".$grade."' ";
            $query .= " AND HR_CLASS            = '".$hr_class."' ";
            $query .= " AND CONDITION           = '".$model->field["CONDITION"]."' ";
            $query .= " AND RIGHT('0000' || GROUPCD, 4) = '".$model->field["GROUPCD"]."' ";

            $db->query($query);
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //存在チェック（コピー） -- GRADE_KIND_COMP_GROUP_YMST
    public function checkCompGroupYMst($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            $query .= "     AND SEMESTER <> '9' ";
        } else {
            $query .= "     AND SEMESTER = '9' ";
        }
        return $query;
    }

    //存在チェック（コピー） -- GRADE_KIND_COMP_GROUP_DAT
    public function checkCompGroupDat($model, $year)
    {
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

    //コピー用データ取得
    public function copyCompGroupDat($model)
    {
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
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            $query .= "     INNER JOIN SEMESTER_MST T4 ON T1.SEMESTER = T4.SEMESTER AND ";
            $query .= "         T3.YEAR = T4.YEAR ";
        }
        $query .= "     INNER JOIN SUBCLASS_DETAIL_MST T5 ON T2.CLASSCD = T5.CLASSCD AND ";
        $query .= "         T2.SCHOOL_KIND      = T5.SCHOOL_KIND AND ";
        $query .= "         T2.CURRICULUM_CD    = T5.CURRICULUM_CD AND ";
        $query .= "         T2.SUBCLASSCD       = T5.SUBCLASSCD AND ";
        $query .= "         T5.SUBCLASS_SEQ     = '001' AND ";
        $query .= "         T1.CONDITION        = T5.SUBCLASS_REMARK1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".intval($model->year - 1)."' ";
        if ($model->Properties["useGradeKindCompGroupSemester"] != "1") {
            $query .= "     AND T1.SEMESTER = '9' ";
        }
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
    public function &copyInsertQueryYMst($model, $Row)
    {
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
    public function &copyInsertQueryDat($model, $Row)
    {
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
