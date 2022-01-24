<?php

require_once('for_php7.php');

class knje063bQuery extends Query
{
    //入学区分取得
    public function selectEnt($schregno)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= "     LEFT JOIN NAME_MST ON NAMECD1 = 'A002' ";
        $query .= "                       AND NAMECD2 = ENT_DIV ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     ENT_DIV IN ('4','5') ";

        return $query;
    }

    //指導要録文言評定データ取得
    public function selectQuery($model, $flg = "", $getSchoolKind = "")
    {
        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         (SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             YEAR, ";
        $query .= "             MAX(SEMESTER) AS SEMESTER ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT ";
        $query .= "         GROUP BY ";
        $query .= "             SCHREGNO, ";
        $query .= "             YEAR ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.GRADE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ";
        $query .= "     T2.CLASSNAME, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASS_SHOW, ";
        $query .= "     T3.SUBCLASSNAME, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.REMARK1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DETAIL_DAT T1 ";
        $query .= "     INNER JOIN CLASS_MST T2 ON T1.CLASSCD       = T2.CLASSCD ";
        $query .= "                            AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        $query .= "     INNER JOIN SUBCLASS_MST T3 ON T1.SUBCLASSCD     = T3.SUBCLASSCD ";
        $query .= "                               AND T1.CLASSCD        = T3.CLASSCD ";
        $query .= "                               AND T1.SCHOOL_KIND    = T3.SCHOOL_KIND ";
        $query .= "                               AND T1.CURRICULUM_CD  = T3.CURRICULUM_CD ";
        $query .= "     LEFT JOIN SCH_INFO L1 ON T1.YEAR        = L1.YEAR ";
        $query .= "                          AND T1.SCHREGNO    = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."' AND ";
        if ($getSchoolKind) {
            $query .= "     T1.SCHOOL_KIND = '" .$getSchoolKind ."' AND ";
        }
        $query .= "     T1.SEQ      = '001' AND ";
        $query .= "     T1.SCHOOLCD = '0' ";
        if ($flg) {
            $query .= "     AND ";
            $query .= "     T1.YEAR     = '".$model->editYear."' AND ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->subclasscd."' ";
        } else {
            $query .= " ORDER BY ";
            if (is_array($model->sort)) {
                foreach ($model->sort as $field => $order) {
                    switch ($field) {
                        case "YEAR":        //年度順
                            $query .= " T1.YEAR ";
                            $query .= ($order > 0) ? " ASC, " : " DESC, ";
                            $query .= " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
                            break 2;
                        case "SUBCLASSCD":  //科目順
                            $query .= " T1.YEAR, ";
                            $query .= " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
                            $query .= ($order > 0) ? " ASC " : " DESC ";
                            break 2;
                    }
                }
            } else {
                $query .= "   T1.YEAR, ";
                $query .= "   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' ||T 1.SUBCLASSCD ";
            }
        }

        return $query;
    }

    //科目マスタ取得
    public function getGradeKindSubclassCount($model, $year)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(L3.CLASSCD || '-' || L3.SCHOOL_KIND || '-' || L3.CURRICULUM_CD || '-' || L3.SUBCLASSCD) AS COUNT ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT T1 ";
        $query .= "     INNER JOIN GRADE_KIND_COMP_GROUP_DAT L3 ON T1.YEAR = L3.YEAR ";
        $query .= "          AND T1.SEMESTER    = L3.SEMESTER ";
        $query .= "          AND T1.GAKUBU_SCHOOL_KIND = L3.GAKUBU_SCHOOL_KIND ";
        $query .= "          AND T1.GHR_CD      = L3.GHR_CD ";
        $query .= "          AND T1.GRADE       = L3.GRADE ";
        $query .= "          AND T1.HR_CLASS    = L3.HR_CLASS ";
        $query .= "          AND T1.CONDITION   = L3.CONDITION ";
        $query .= "          AND T1.GROUPCD     = L3.GROUPCD ";
        $query .= "     INNER JOIN GRADE_KIND_COMP_GROUP_YMST L1 ON L1.YEAR = L3.YEAR ";
        $query .= "          AND L1.SEMESTER    = L3.SEMESTER ";
        $query .= "          AND L1.GAKUBU_SCHOOL_KIND = L3.GAKUBU_SCHOOL_KIND ";
        $query .= "          AND L1.GHR_CD      = L3.GHR_CD ";
        $query .= "          AND L1.GRADE       = L3.GRADE ";
        $query .= "          AND L1.HR_CLASS    = L3.HR_CLASS ";
        $query .= "          AND L1.CONDITION   = L3.CONDITION ";
        $query .= "          AND L1.GROUPCD     = L3.GROUPCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".$year."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //年度取得
    public function &selectQueryYear($model, $getSchoolKind)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '0' ";
        $query .= " AND SCHREGNO = '".$model->schregno."' ";
        $query .= " AND SCHOOL_KIND = '".$getSchoolKind."' ";

        return $query;
    }

    //教科マスタ取得
    public function getClassMst($model, $year, $getSchoolKind)
    {
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '：' || T1.CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLASS_MST T1 ";
        $query .= "     INNER JOIN ";
        $query .= "         CLASS_DETAIL_MST T2 ";
        $query .= "              ON T2.CLASSCD = T1.CLASSCD ";
        $query .= "             AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "             AND T2.CLASS_SEQ = '002' ";
        $query .= "             AND T2.CLASS_REMARK1 = '1' "; //知的用のみ
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '".$getSchoolKind."' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '：' || T1.CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLASS_MST T1 ";
        $query .= "     INNER JOIN V_NAME_MST T2 ";
        $query .= "          ON T2.YEAR = '".$year."' ";
        $query .= "         AND T2.NAMECD1 = 'E076' ";
        $query .= "         AND T2.NAME1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教育課程取得
    public function getCurriculum($model, $year, $classKey)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z018' ";
        if ($classKey) {
            $query .= " AND ";
            $query .= "     NAMECD2 IN ( ";
            $query .= "          SELECT ";
            $query .= "              T1.CURRICULUM_CD ";
            $query .= "          FROM ";
            $query .= "              SUBCLASS_MST T1";
            $query .= "              INNER JOIN SUBCLASS_DETAIL_MST T2 ";
            $query .= "                   ON T2.CLASSCD          = T1.CLASSCD ";
            $query .= "                  AND T2.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "                  AND T2.CURRICULUM_CD    = T1.CURRICULUM_CD ";
            $query .= "                  AND T2.SUBCLASSCD       = T1.SUBCLASSCD ";
            $query .= "                  AND T2.SUBCLASS_SEQ     = '001' ";
            $query .= "                  AND T2.SUBCLASS_REMARK1 = '1' "; //知的用のみ
            $query .= "          WHERE ";
            $query .= "              T1.CLASSCD || '-' || T1.SCHOOL_KIND = '".$classKey."' ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目マスタ取得
    public function getSubclassMst($model, $classKey, $curriculum_cd)
    {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '　' || T1.SUBCLASSNAME AS SUBCLASSNAME, ";
        $query .= "     T1.SUBCLASSCD || '：' || T1.SUBCLASSNAME AS LABEL, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST T1 ";
        $query .= "     INNER JOIN SUBCLASS_DETAIL_MST T2 ";
        $query .= "          ON T2.CLASSCD          = T1.CLASSCD ";
        $query .= "         AND T2.SCHOOL_KIND      = T1.SCHOOL_KIND ";
        $query .= "         AND T2.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        $query .= "         AND T2.SUBCLASSCD       = T1.SUBCLASSCD ";
        $query .= "         AND T2.SUBCLASS_SEQ     = '001' ";
        $query .= "         AND T2.SUBCLASS_REMARK1 = '1' "; //知的用のみ
        $query .= " WHERE ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND = '".$classKey."' ";
        $query .= "     AND T1.CURRICULUM_CD = '".$curriculum_cd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 合わせた指導参照の科目取得
    public function getSubclassMstClassSeq001($model, $getSchoolKind, $year)
    {

        $query  = " SELECT ";
        $query .= "     T1.SUBCLASSCD || '：' || T1.SUBCLASSNAME AS LABEL, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST T1 ";
        $query .= "     INNER JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ";
        $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "     INNER JOIN CLASS_DETAIL_MST T3 ON T3.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "         AND T3.CLASS_SEQ = '001' ";
        $query .= "         AND T3.CLASS_REMARK1 = '1' ";
        $query .= "     INNER JOIN SUBCLASS_YDAT T4 ON T4.YEAR = '".$year."' ";
        $query .= "         AND T4.CLASSCD = T1.CLASSCD ";
        $query .= "         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "         AND T4.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($getSchoolKind) {
            $query .= " WHERE ";
            $query .= "     T1.SCHOOL_KIND = '".$getSchoolKind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //署名チェック
    public function getOpinionsWk($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    // INSERT
    public function &getInsertQuery($model, $fields, $db)
    {
        list ($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $fields["SUBCLASSCD"]);

        $data = array();
        $data["SCHOOLCD"][TEXT]         = '0';
        $data["YEAR"][TEXT]             = $fields["YEAR"];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["CLASSCD"][TEXT]          = $classcd;
        $data["SCHOOL_KIND"][TEXT]      = $school_kind;
        $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
        $data["SUBCLASSCD"][TEXT]       = $subclasscd;
        $data["SEQ"][TEXT]              = '001';
        $data["REMARK1"][TEXT]          = $fields["REMARK1"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_STUDYREC_DETAIL_DAT");

        return $query;
    }

    // DELETE
    public function &getDeleteQuery($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_STUDYREC_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD = '0' AND ";
        $query .= "   SCHREGNO = '".$model->schregno."' AND ";
        if ($model->cmd == "update" || $model->cmd == "delete2") {
            $query .= "   YEAR = '".$model->field["YEAR"]."' AND ";
            $query .= "   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        } elseif ($model->cmd == "delete") {
            $query .= "   YEAR || ',' || CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN ('".implode($model->checked, "','")."') AND ";
        }
        $query .= "   SEQ = '001'  ";

        return $query;
    }

    //名称マスタ取得
    public function getD057()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D057' ";

        return $query;
    }

    //名称マスタ取得
    public function getZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    /**********************/
    /* 指導計画参照ボタン */
    /**********************/
    //学期マスタ取得
    public function getSemesterMst($year)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR      = '".$year."' AND ";
        $query .= "     SEMESTER != '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //指導計画データ取得
    public function getHreportremarkGuidanceDat($model, $year, $subclasscd, $semester, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";
        if ($seq) {
            $query .= " AND SEQ     = ".$seq." ";
        }

        return $query;
    }

    //帳票パターン取得
    public function getGuidancePattern($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     L1.YEAR, ";
        $query .= "     L1.SEMESTER, ";
        $query .= "     L1.GAKUBU_SCHOOL_KIND, ";
        $query .= "     L1.GHR_CD, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.CONDITION, ";
        $query .= "     L1.GROUPCD, ";
        $query .= "     L1.GUIDANCE_PATTERN ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT T1 ";
        $query .= "     LEFT JOIN GRADE_KIND_COMP_GROUP_DAT L3 ON T1.YEAR = L3.YEAR ";
        $query .= "          AND T1.SEMESTER    = L3.SEMESTER ";
        $query .= "          AND T1.GAKUBU_SCHOOL_KIND = L3.GAKUBU_SCHOOL_KIND ";
        $query .= "          AND T1.GHR_CD      = L3.GHR_CD ";
        $query .= "          AND T1.GRADE       = L3.GRADE ";
        $query .= "          AND T1.HR_CLASS    = L3.HR_CLASS ";
        $query .= "          AND T1.CONDITION   = L3.CONDITION ";
        $query .= "          AND T1.GROUPCD     = L3.GROUPCD ";
        $query .= "     LEFT JOIN GRADE_KIND_COMP_GROUP_YMST L1 ON L1.YEAR = L3.YEAR ";
        $query .= "          AND L1.SEMESTER    = L3.SEMESTER ";
        $query .= "          AND L1.GAKUBU_SCHOOL_KIND = L3.GAKUBU_SCHOOL_KIND ";
        $query .= "          AND L1.GHR_CD      = L3.GHR_CD ";
        $query .= "          AND L1.GRADE       = L3.GRADE ";
        $query .= "          AND L1.HR_CLASS    = L3.HR_CLASS ";
        $query .= "          AND L1.CONDITION   = L3.CONDITION ";
        $query .= "          AND L1.GROUPCD     = L3.GROUPCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".$year."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //HREPORT_GUIDANCE_ITEM_NAME_DAT
    public function getItemName($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR                = '".$year."' ";
        $query .= "     AND SEMESTER            = '9' ";
        $query .= "     AND GAKUBU_SCHOOL_KIND  = '".$model->schregInfo["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= "     AND CONDITION           = '".$model->schregInfo["CONDITION"]."' ";
        if ($model->Properties["useKNJZ064"] == "KNJZ064M") {
            $query .= "     AND GUIDANCE_PATTERN    = '3' ";
        } else {
            $query .= "     AND GUIDANCE_PATTERN    = '".$model->schregInfo["GUIDANCE_PATTERN"]."' ";
        }

        return $query;
    }

    //パターン取得
    public function getNameMstPattern($model, $year, $name2, $fieldName)
    {
        $query  = " SELECT ";
        $query .= "     {$fieldName} ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = 'A035' ";
        $query .= "     AND NAMECD2 = '{$name2}' ";

        return $query;
    }

    //単元取得
    public function getUnit($model, $year, $setSubclasscd)
    {
        $query  = " SELECT ";
        $query .= "     T1.UNITCD AS VALUE, ";
        $query .= "     T1.UNITNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_UNIT_GROUP_YMST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".$year."' ";
        $query .= "     AND T1.SEMESTER     = '9' ";
        $query .= "     AND T1.GAKUBU_SCHOOL_KIND = '".$model->schregInfo["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= "     AND T1.GHR_CD       = '".$model->schregInfo["GHR_CD"]."' ";
        $query .= "     AND T1.GRADE        = '".$model->schregInfo["GRADE"]."' ";
        $query .= "     AND T1.HR_CLASS     = '".$model->schregInfo["HR_CLASS"]."' ";
        $query .= "     AND T1.CONDITION    = '".$model->schregInfo["CONDITION"]."' ";
        $query .= "     AND T1.GROUPCD      = '".$model->schregInfo["GROUPCD"]."' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$setSubclasscd}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.UNITCD ";

        return $query;
    }

   //指導計画取得
    public function getGuidanceSchregSubclassDat($model, $year, $subclassCd)
    {
        $query  = " SELECT ";
        $query .= "     MAIN.SEMESTER, ";
        $query .= "     MAIN.UNITCD, ";
        $query .= "     YMST.UNITNAME, ";
        $query .= "     MAIN.SEQ, ";
        $query .= "     MAIN.REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT MAIN ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD       = MAIN.CLASSCD ";
        $query .= "                                AND SUBM.SCHOOL_KIND   = MAIN.SCHOOL_KIND ";
        $query .= "                                AND SUBM.CURRICULUM_CD = MAIN.CURRICULUM_CD ";
        $query .= "                                AND SUBM.SUBCLASSCD    = MAIN.SUBCLASSCD ";
        $query .= "     LEFT JOIN GRADE_KIND_UNIT_GROUP_YMST YMST ";
        $query .= "          ON YMST.YEAR               = MAIN.YEAR ";
        $query .= "         AND YMST.GAKUBU_SCHOOL_KIND = '".$model->schregInfo["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= "         AND YMST.GHR_CD             = '".$model->schregInfo["GHR_CD"]."' ";
        $query .= "         AND YMST.GRADE              = '".$model->schregInfo["GRADE"]."' ";
        $query .= "         AND YMST.HR_CLASS           = '".$model->schregInfo["HR_CLASS"]."' ";
        $query .= "         AND YMST.CONDITION          = '".$model->schregInfo["CONDITION"]."' ";
        $query .= "         AND YMST.GROUPCD            = '".$model->schregInfo["GROUPCD"]."' ";
        $query .= "         AND YMST.CLASSCD || YMST.SCHOOL_KIND || YMST.CURRICULUM_CD || YMST.SUBCLASSCD = MAIN.CLASSCD || MAIN.SCHOOL_KIND || MAIN.CURRICULUM_CD || MAIN.SUBCLASSCD ";
        $query .= "         AND YMST.UNITCD             = MAIN.UNITCD ";
        $query .= "     LEFT JOIN GRADE_KIND_COMP_GROUP_DAT COMP ";
        $query .= "          ON YMST.YEAR               = COMP.YEAR ";
        $query .= "         AND YMST.SEMESTER           = COMP.SEMESTER ";
        $query .= "         AND YMST.GAKUBU_SCHOOL_KIND = COMP.GAKUBU_SCHOOL_KIND ";
        $query .= "         AND YMST.GHR_CD             = COMP.GHR_CD ";
        $query .= "         AND YMST.GRADE              = COMP.GRADE ";
        $query .= "         AND YMST.HR_CLASS           = COMP.HR_CLASS ";
        $query .= "         AND YMST.CONDITION          = COMP.CONDITION ";
        $query .= "         AND YMST.GROUPCD            = COMP.GROUPCD ";
        $query .= "         AND YMST.CLASSCD || YMST.SCHOOL_KIND || YMST.CURRICULUM_CD || YMST.SUBCLASSCD = COMP.CLASSCD || COMP.SCHOOL_KIND || COMP.CURRICULUM_CD || COMP.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         MAIN.YEAR     = '{$year}' ";
        $query .= "     AND MAIN.SCHREGNO = '{$model->schregno}' ";
        if ($model->Properties["useKNJZ064"] == "KNJZ064M") {
            $query .= "     AND MAIN.GUIDANCE_PATTERN = '3' ";
        } else {
            $query .= "     AND MAIN.GUIDANCE_PATTERN = '{$model->schregInfo["GUIDANCE_PATTERN"]}' ";
        }
        $query .= "     AND MAIN.CLASSCD || '-' || MAIN.SCHOOL_KIND || '-' || MAIN.CURRICULUM_CD || '-' || MAIN.SUBCLASSCD = '{$subclassCd}' ";
        $query .= " ORDER BY ";
        $query .= "     MAIN.UNITCD, ";
        $query .= "     MAIN.SEQ ";

        return $query;
    }

    //学校校種を取得
    public function getSchoolKind($model)
    {
        $query  = "SELECT ";
        $query .= "    L1.SCHOOL_KIND ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                 AND L1.GRADE = T1.GRADE ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "AND T1.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //科目名を取得
    public function getSubclassName($subclassCd)
    {
        $query .= " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' ' || SUBCLASSNAME AS SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclassCd."' ";
    
        return $query;
    }
}
?>
