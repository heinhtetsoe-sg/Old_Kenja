<?php

require_once('for_php7.php');

class knjd420Query extends Query {
    //名称マスタより取得
    function getNameMst($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->exp_year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //印刷学期取得
    function getD078($year, $namecd1, $ctrlSemester = '')
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     CASE WHEN ABBV1 = '".CTRL_SEMESTER."' THEN 1 END AS DEF_VALUE_FLG ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " AND NAMECD1 = '{$namecd1}' ";
        if ($ctrlSemester) {
            $query .= "     AND ABBV1 = '".CTRL_SEMESTER."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    //学年の校種取得
    function getSchoolKind($year, $grade)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " AND GRADE = '{$grade}' ";

        return $query;
    }

    //グループ、履修情報取得
    function getViewGradeKindSchreg($model, $flg="")
    {
        $query  = " SELECT ";
        if ($flg == 'set') {
            $query .= " DISTINCT ";
            $query .= "     T1.GAKUBU_SCHOOL_KIND, ";
            $query .= "     T1.GHR_CD, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.GROUPCD ";
        } else {
            $query .= "     T1.* ";
        }
        $query .= " FROM ";
        $query .= "     V_GRADE_KIND_SCHREG_UNIT_GROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' ";
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            $query .= " AND T1.SEMESTER = '".$model->printGakki."' ";
        } else {
            $query .= " AND T1.SEMESTER = '9' ";
        }
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        if ($flg != 'set') {
            $query .= " ORDER BY ";
            $query .= "     T1.GROUPCD,  ";
            $query .= "     T1.CLASSCD,  ";
            $query .= "     T1.SCHOOL_KIND,  ";
            $query .= "     T1.CURRICULUM_CD,  ";
            $query .= "     T1.SUBCLASSCD,  ";
            $query .= "     T1.UNITCD  ";
        }

        return $query;
    }

    //グループコード取得
    function getGroupcd($model, $row) {
        $query  = " SELECT ";
        $query .= "     GROUPNAME ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            $query .= " AND SEMESTER    = '".$model->printGakki."' ";
        } else {
            $query .= " AND SEMESTER    = '9' ";
        }
        $query .= " AND GAKUBU_SCHOOL_KIND = '".$row["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= " AND GHR_CD      = '".$row["GHR_CD"]."' ";
        $query .= " AND GRADE       = '".$row["GRADE"]."' ";
        $query .= " AND HR_CLASS    = '".$row["HR_CLASS"]."' ";
        $query .= " AND CONDITION   = '".$row["CONDITION"]."' ";
        $query .= " AND GROUPCD     = '".$row["GROUPCD"]."' ";

        return $query;
    }

    //状態区分名称取得
    function getConditionName($model, $condition) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND NAMECD1     = 'A033' ";
        $query .= " AND NAMECD2     = '".$condition."' ";

        return $query;
    }

    //帳票パターン取得
    function getGuidancePattern($model)
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
        $query .= "     T1.YEAR = '".$model->exp_year."' ";
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            $query .= " AND T1.SEMESTER = '".$model->printGakki."' ";
        } else {
            $query .= " AND T1.SEMESTER = '9' ";
        }
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //UNIT数をカウント
    function getUnitSubclassCnt($model, $setSubclasscd)
    {
        $query .= " SELECT ";
        if ($model->schregInfo["GUIDANCE_PATTERN"] == "B") {
            $query .= "     COUNT(DISTINCT UNITCD) ";
            $query .= " FROM ";
            $query .= "     HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT ";
        } else {
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HREPORT_GUIDANCE_SCHREG_UNIT_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= " AND SEMESTER = '".$model->printGakki."' ";
        $query .= " AND SCHREGNO = '".$model->schregno."' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$setSubclasscd."' ";

        return $query;
    }

   //履歴情報の表示
    function getList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || L3.SUBCLASSNAME AS SET_SUBCLASSNAME, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SET_SUBCLASSCD, ";
        //パターン6のみ
        $query .= "     L4.VALUE, ";
        //パターン7のみ
        $query .= "     L2.YEAR_TARGET, ";
        //単元ごと
        $query .= "     L1_2.UNITCD, ";
        $query .= "     L1.REMARK1 AS UNIT_REMARK, ";
        $query .= "     L1.REMARK_VALUE1 AS UNIT_REMARK_VALUE, ";
        $query .= "     L1.REMARK2 AS UNIT_REMARK2 ";
        $query .= " FROM ( ";
        $query .= "     SELECT ";
        $query .= "       T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "       HREPORT_GUIDANCE_SCHREG_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "         T1.SEMESTER = '".$model->printGakki."' AND ";
        $query .= "         T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "       T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "       HREPORT_GUIDANCE_SCHREG_UNIT_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "         T1.SEMESTER = '".$model->printGakki."' AND ";
        $query .= "         T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "       T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "       HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "         T1.SEMESTER = '".$model->printGakki."' AND ";
        $query .= "         T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "      ) T1 ";
        $query .= "     LEFT JOIN HREPORT_GUIDANCE_SCHREG_UNIT_DAT L1 ";
        $query .= "          ON L1.YEAR         = T1.YEAR ";
        $query .= "         AND L1.SEMESTER     = T1.SEMESTER ";
        $query .= "         AND L1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND L1.CLASSCD      = T1.CLASSCD ";
        $query .= "         AND L1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        $query .= "         AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "         AND L1.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "     LEFT JOIN (SELECT YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, UNITCD ";
        $query .= "                FROM HREPORT_GUIDANCE_SCHREG_UNIT_DAT ";
        $query .= "                UNION ";
        $query .= "                SELECT YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, UNITCD ";
        $query .= "                FROM HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT) L1_2 ";
        $query .= "          ON L1_2.YEAR         = T1.YEAR ";
        $query .= "         AND L1_2.SEMESTER     = T1.SEMESTER ";
        $query .= "         AND L1_2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND L1_2.CLASSCD      = T1.CLASSCD ";
        $query .= "         AND L1_2.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        $query .= "         AND L1_2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "         AND L1_2.SUBCLASSCD   = T1.SUBCLASSCD ";
        //年間目標(パターン7のみ)
        $query .= "     LEFT JOIN HREPORT_GUIDANCE_SCHREG_YDAT L2 ";
        $query .= "          ON L2.YEAR         = T1.YEAR ";
        $query .= "         AND L2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND L2.CLASSCD      = T1.CLASSCD ";
        $query .= "         AND L2.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        $query .= "         AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "         AND L2.SUBCLASSCD   = T1.SUBCLASSCD ";
        //科目名
        $query .= "     LEFT JOIN SUBCLASS_MST L3 ";
        $query .= "          ON L3.CLASSCD      = T1.CLASSCD ";
        $query .= "         AND L3.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        $query .= "         AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "         AND L3.SUBCLASSCD   = T1.SUBCLASSCD ";
        //評定(パターン6のみ)
        $query .= "     LEFT JOIN RECORD_SCORE_DAT L4 ";
        $query .= "          ON L4.YEAR         = T1.YEAR ";
        $query .= "         AND L4.SEMESTER     = '9' ";
        $query .= "         AND L4.TESTKINDCD   = '00' ";
        $query .= "         AND L4.TESTITEMCD   = '00' ";
        $query .= "         AND L4.SCORE_DIV    = '00' ";
        $query .= "         AND L4.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND L4.CLASSCD      = T1.CLASSCD ";
        $query .= "         AND L4.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        $query .= "         AND L4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "         AND L4.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "     T1.SEMESTER = '".$model->printGakki."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L1_2.UNITCD ";

        return $query;
    }

    //データ取得 -- HREPORT_GUIDANCE_SCHREG_DAT
    function getHreportGuidanceSchregDat2($model, $subclasscd, $seq) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->printGakki."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' AND ";
        $query .= "     SEQ         = ".$seq." ";

        return $query;
    }

    //データ取得 -- HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT
    function getHreportGuidanceSchregUnitSeqDat2($model, $subclasscd, $unitcd, $seq) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->printGakki."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' AND ";
        $query .= "     UNITCD      = '".$unitcd."' AND ";
        $query .= "     SEQ         = ".$seq." ";

        return $query;
    }

    //科目取得
    function getSubclass($model)
    {
        $query  = " SELECT ";
        $query .= "     L3.CLASSCD || '-' || L3.SCHOOL_KIND || '-' || L3.CURRICULUM_CD || '-' || L3.SUBCLASSCD AS VALUE, ";
        $query .= "     L3.CLASSCD || '-' || L3.SCHOOL_KIND || '-' || L3.CURRICULUM_CD || '-' || L3.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL ";
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
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L3.CLASSCD = L1.CLASSCD ";
        $query .= "          AND L3.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND L3.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "          AND L3.SUBCLASSCD  = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->exp_year."' ";
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            $query .= "     AND T1.SEMESTER = '".$model->printGakki."' ";
        } else {
            $query .= "     AND T1.SEMESTER = '9' ";
        }
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //「指導のねらい」区分取得
    function getUnitAimDiv($model, $setSubclasscd="") {
        $query  = " SELECT ";
        $query .= "     UNIT_AIM_DIV ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            $query .= "     SEMESTER = '".$model->printGakki."' AND ";
        } else {
            $query .= "     SEMESTER = '9' AND ";
        }
        $query .= "     GAKUBU_SCHOOL_KIND = '".$model->schregInfo["GAKUBU_SCHOOL_KIND"]."' AND ";
        $query .= "     GHR_CD      = '".$model->schregInfo["GHR_CD"]."' AND ";
        $query .= "     GRADE       = '".$model->schregInfo["GRADE"]."' AND ";
        $query .= "     HR_CLASS    = '".$model->schregInfo["HR_CLASS"]."' AND ";
        $query .= "     CONDITION   = '".$model->schregInfo["CONDITION"]."' AND ";
        $query .= "     GROUPCD     = '".$model->schregInfo["GROUPCD"]."' AND ";
        if (!$setSubclasscd) {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        } else {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$setSubclasscd."' ";
        }
        
        return $query;
    }

    //単元取得
    function getUnit($model, $setSubclasscd="", $setUnitcd="")
    {
        $query  = " SELECT ";
        if (!$setSubclasscd) {
            $query .= "     T1.UNITCD AS VALUE, ";
            $query .= "     T1.UNITNAME AS LABEL ";
        } else {
            $query .= "     T1.UNITCD || ':' || T1.UNITNAME AS SET_UNITNAME ";
        }
        $query .= " FROM ";
        $query .= "     GRADE_KIND_UNIT_GROUP_YMST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$model->exp_year."' ";
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            $query .= "     AND T1.SEMESTER = '".$model->printGakki."' ";
        } else {
            $query .= "     AND T1.SEMESTER = '9' ";
        }
        $query .= " AND T1.GAKUBU_SCHOOL_KIND = '".$model->schregInfo["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= " AND T1.GHR_CD       = '".$model->schregInfo["GHR_CD"]."' ";
        $query .= " AND T1.GRADE        = '".$model->schregInfo["GRADE"]."' ";
        $query .= " AND T1.HR_CLASS     = '".$model->schregInfo["HR_CLASS"]."' ";
        $query .= " AND T1.CONDITION    = '".$model->schregInfo["CONDITION"]."' ";
        $query .= " AND T1.GROUPCD      = '".$model->schregInfo["GROUPCD"]."' ";
        if (!$setSubclasscd) {
            $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->subclasscd}' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$setSubclasscd}' ";
            $query .= " AND T1.UNITCD = '{$setUnitcd}' ";
        }

        return $query;
    }

    //HREPORT_GUIDANCE_ITEM_NAME_DAT
    function getItemName($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
            $query .= "     AND SEMESTER = '".$model->printGakki."' ";
        } else {
            $query .= "     AND SEMESTER = '9' ";
        }
        $query .= " AND GAKUBU_SCHOOL_KIND  = '".$model->schregInfo["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= " AND CONDITION   = '".$model->schregInfo["CONDITION"]."' ";
        $query .= " AND GUIDANCE_PATTERN    = '".$model->schregInfo["GUIDANCE_PATTERN"]."' ";

        return $query;
    }

    //GRADE_KIND_ASSESS_DAT
    function getGradeKindAssess($model, $schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     rtrim(CAST(T1.ASSESSLEVEL AS CHAR(3))) || ':' || T1.ASSESSMARK AS LABEL, ";
        $query .= "     T1.ASSESSLEVEL AS VALUE ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_ASSESS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exp_year}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        $query .= "     AND T1.CONDITION = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //重点目標取得
    function getHreportGuidanceSchregImpptDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_IMPPT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->exp_year}' AND ";
        $query .= "     SEMESTER    = '{$model->printGakki}' AND ";
        $query .= "     SCHREGNO    = '{$model->schregno}' ";

        return $query;
    }

    //１レコード取得 -- HREPORT_GUIDANCE_SCHREG_DAT
    function getHreportGuidanceSchregDat($model, $seq="") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->printGakki."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        if ($seq) {
            $query .= " AND SEQ         = ".$seq." ";
        }

        return $query;
    }

    //１レコード取得 -- HREPORT_GUIDANCE_SCHREG_UNIT_DAT
    function getHreportGuidanceSchregUnitDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->printGakki."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' AND ";
        $query .= "     UNITCD      = '".$model->unitcd."' ";

        return $query;
    }

    //１レコード取得 -- HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT
    function getHreportGuidanceSchregUnitSeqDat($db, $model, $seq = "") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->printGakki."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' AND ";
        $query .= "     UNITCD      = '".$model->unitcd."' ";
        if ($seq) {
            $query .= "     AND SEQ         = {$seq} ";
        }
        $result = $db->query($query);
        $Row = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $Row["REMARK".$row["SEQ"]] = $row["REMARK"];
            $Row["SCHREGNO"] = $row["SCHREGNO"];
        }
        $result->free();
        return $Row;
    }

    //１レコード取得 -- HREPORT_GUIDANCE_SCHREG_YDAT
    function getHreportGuidanceSchregYdat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_YDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";

        return $query;
    }

    //１レコード取得 -- RECORD_SCORE_DAT
    function getRecordScoreDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '9' AND ";
        $query .= "     TESTKINDCD  = '99' AND ";
        $query .= "     TESTITEMCD  = '00' AND ";
        $query .= "     SCORE_DIV   = '00' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";

        return $query;
    }

    //項目数取得
    function getGroupRemarkCnt($model, $row, $flg="") {
        $query  = " SELECT ";
        if (!$flg) {
            $query .= "     GROUP_REMARK_CNT ";
        } else {
            $query .= "     MAX(GROUP_REMARK_CNT) AS MAX_GROUP_REMARK_CNT ";
        }
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_GROUP_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER            = '".$model->printGakki."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND  = '".$row["GAKUBU_SCHOOL_KIND"]."' AND ";
        $query .= "     GHR_CD              = '".$row["GHR_CD"]."' AND ";
        $query .= "     GRADE               = '".$row["GRADE"]."' AND ";
        $query .= "     HR_CLASS            = '".$row["HR_CLASS"]."' AND ";
        $query .= "     CONDITION           = '".$row["CONDITION"]."' AND ";
        $query .= "     GROUPCD             = '".$row["GROUPCD"]."' ";
        if (!$flg) {
            $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        } else {
            $query .= " GROUP BY ";
            $query .= "     YEAR, ";
            $query .= "     SEMESTER, ";
            $query .= "     GAKUBU_SCHOOL_KIND, ";
            $query .= "     GHR_CD, ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     CONDITION, ";
            $query .= "     GROUPCD ";
        }
        return $query;
    }

    //INSERT -- HREPORT_GUIDANCE_SCHREG_DAT
    function &getInsertQuery1($db, $model, $upd_field, $upd_data, $seq)
    {
        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $model->subclasscd);

        //存在チェック
        $query = knjd420Query::getHreportGuidanceSchregDat($model, $seq);
        $check = $db->getOne($query);

        if ($check == "") {
            $data = array();
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->printGakki;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["CLASSCD"][TEXT]          = $classCd;
            $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
            $data["SUBCLASSCD"][TEXT]       = $subclassCd;
            $data["SEQ"][NUMBER]            = $seq;
            $data[$upd_field][TEXT]         = $upd_data;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_DAT");
            $db->query($query);

        } else {
            $data = array();
            $data[$upd_field][TEXT]         = $upd_data;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER    = '".$model->printGakki."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' AND ";
            $where .= "     SEQ         = ".$seq." ";

            //更新
            $query = Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_DAT", $where);
            $db->query($query);

        }

        return;
    }

    //INSERT -- HREPORT_GUIDANCE_SCHREG_UNIT_DAT
    function &getInsertQuery2($db, $model)
    {
        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $model->subclasscd);

        //存在チェック
        $check = $db->getOne(knjd420Query::getHreportGuidanceSchregUnitDat($model));

        if ($check == "") {
            $data = array();
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->printGakki;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["CLASSCD"][TEXT]          = $classCd;
            $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
            $data["SUBCLASSCD"][TEXT]       = $subclassCd;
            $data["UNITCD"][TEXT]           = $model->unitcd;
            $data["GUIDANCE_PATTERN"][TEXT] = $model->schregInfo["GUIDANCE_PATTERN"];
            $data["REMARK1"][TEXT]          = $model->field2["REMARK1"];
            $data["REMARK_VALUE1"][TEXT]    = $model->field2["REMARK_VALUE1"];
            $data["REMARK2"][TEXT]          = $model->field2["REMARK2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_UNIT_DAT");
            $db->query($query);

        } else {
            $data = array();

            $data["GUIDANCE_PATTERN"][TEXT] = $model->schregInfo["GUIDANCE_PATTERN"];
            foreach ($model->paternInfo[$model->unit_aim_div][$model->schregInfo["GUIDANCE_PATTERN"]] as $key => $val) {
                if ($val["SEQ"]) {
                    continue;
                }
                $data[$val["UPD_FIELD"]][TEXT]  = $model->field2[$val["UPD_FIELD"]];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER    = '".$model->printGakki."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' AND ";
            $where .= "     UNITCD    = '".$model->unitcd."' ";

            //更新
            $query = Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_UNIT_DAT", $where);
            $db->query($query);

        }

        return;
    }

    // INSERT -- HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT
    function &getInsertQuery2seq($db, $model, $seq, $field, $value)
    {
        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $model->subclasscd);

        //存在チェック
        $Row = knjd420Query::getHreportGuidanceSchregUnitSeqDat($db, $model, $seq);
        if (empty($Row)) {
            $data = array();
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->printGakki;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["CLASSCD"][TEXT]          = $classCd;
            $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
            $data["SUBCLASSCD"][TEXT]       = $subclassCd;
            $data["UNITCD"][TEXT]           = $model->unitcd;
            $data["SEQ"][NUMBER]            = $seq;
            $data["REMARK"][TEXT]           = $value;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT");
            $db->query($query);

        } else {
            $data = array();

            $data[$field][TEXT]  = $value;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER    = '".$model->printGakki."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' AND ";
            $where .= "     UNITCD    = '".$model->unitcd."' AND ";
            $where .= "     SEQ         = {$seq} ";

            //更新
            $query = Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT", $where);
            $db->query($query);

        }

        return;
    }

    //INSERT -- HREPORT_GUIDANCE_SCHREG_YDAT
    function &getInsertQuery3($db, $model)
    {
        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $model->subclasscd);

        //存在チェック
        $check = $db->getOne(knjd420Query::getHreportGuidanceSchregYdat($model));

        if ($check == "") {
            $data = array();
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["CLASSCD"][TEXT]          = $classCd;
            $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
            $data["SUBCLASSCD"][TEXT]       = $subclassCd;
            $data["GUIDANCE_PATTERN"][TEXT] = $model->schregInfo["GUIDANCE_PATTERN"];
            $data["YEAR_TARGET"][TEXT]      = $model->field3["YEAR_TARGET"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            //追加
            $db->query(Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_YDAT"));

        } else {
            $data = array();

            $data["GUIDANCE_PATTERN"][TEXT] = $model->schregInfo["GUIDANCE_PATTERN"];
            foreach ($model->paternInfo[8][$model->schregInfo["GUIDANCE_PATTERN"]] as $key => $val) {
                $data[$val["UPD_FIELD"]][TEXT]  = $model->field3[$val["UPD_FIELD"]];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";

            //更新
            $db->query(Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_YDAT", $where));

        }

        return;
    }

    //INSERT -- RECORD_SCORE_DAT
    function &getInsertQuery4($db, $model)
    {
        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $model->subclasscd);

        //存在チェック
        $check = $db->getOne(knjd420Query::getRecordScoreDat($model));

        if ($check == "") {
            $data = array();
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = "9";
            $data["TESTKINDCD"][TEXT]       = "99";
            $data["TESTITEMCD"][TEXT]       = "00";
            $data["SCORE_DIV"][TEXT]        = "00";
            $data["CLASSCD"][TEXT]          = $classCd;
            $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
            $data["SUBCLASSCD"][TEXT]       = $subclassCd;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["VALUE"][NUMBER]          = $model->field4["VALUE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            //追加
            $db->query(Query::insertSQL($data, "RECORD_SCORE_DAT"));

        } else {
            $data = array();
            $data["VALUE"][NUMBER]          = $model->field4["VALUE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER    = '9' AND ";
            $where .= "     TESTKINDCD  = '99' AND ";
            $where .= "     TESTITEMCD  = '00' AND ";
            $where .= "     SCORE_DIV   = '00' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";

            //更新
            $db->query(Query::updateSQL($data, "RECORD_SCORE_DAT", $where));

        }

        return;
    }


   //DELETE -- HREPORT_GUIDANCE_SCHREG_DAT
   function &getDeleteQuery1($db, $model, $seq)
   {
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->printGakki."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' AND ";
        $query .= "     SEQ         = ".$seq." ";

        $db->query($query);
        return;
   }

   //DELETE -- HREPORT_GUIDANCE_SCHREG_UNIT_DAT
   function &getDeleteQuery2($db, $model)
   {
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->printGakki."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' AND ";
        $query .= "     UNITCD      = '".$model->unitcd."' ";

        $db->query($query);
        return;
   }

   //DELETE -- HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT
   function &getDeleteQuery2Seq($db, $model)
   {
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->printGakki."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' AND ";
        $query .= "     UNITCD      = '".$model->unitcd."' ";

        $db->query($query);
        return;
   }

   //DELETE -- HREPORT_GUIDANCE_SCHREG_YDAT
   function &getDeleteQuery3($db, $model)
   {
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_YDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";

        $db->query($query);
        return;
   }

   //DELETE -- RECORD_SCORE_DAT
   function &getDeleteQuery4($db, $model)
   {
        $query  = " DELETE FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '9' AND ";
        $query .= "     TESTKINDCD  = '99' AND ";
        $query .= "     TESTITEMCD  = '00' AND ";
        $query .= "     SCORE_DIV   = '00' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";

        $db->query($query);
        return;
   }

   // COPY CHECK
   function copyHreportGuidanceSchregDatBpattern($model, $printGakki, $countDiv = "") {
        $query .= " SELECT ";
        if ($countDiv == "COUNT") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$printGakki."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     SEQ        IN (71, 72) ";

        return $query;
   }

   // COPY CHECK
   function copyHreportGuidanceSchregUnitSeqDatBpattern($model, $printGakki, $countDiv = '') {
        $query .= " SELECT ";
        if ($countDiv == "COUNT") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$printGakki."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     SEQ        IN (71, 72) ";

        return $query;
   }

   // INSERT
   function getInsertHreportGuidanceSchregDatBpatternQuery($row, $printGakki) {
        $data = array();
        $data["YEAR"][TEXT]             = $row["YEAR"];
        $data["SEMESTER"][TEXT]         = $printGakki;
        $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
        $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
        $data["SEQ"][NUMBER]            = $row["SEQ"];
        $data["REMARK"][TEXT]           = $row["REMARK"];
        $data["REMARK_VALUE"][TEXT]     = $row["REMARK_VALUE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        //追加
        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_DAT");
        return $query;
   }

   // INSERT
   function getInsertHreportGuidanceSchregUnitSeqDatBpatternQuery($row, $printGakki) {
        $data = array();
        $data["YEAR"][TEXT]             = $row["YEAR"];
        $data["SEMESTER"][TEXT]         = $printGakki;
        $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
        $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
        $data["UNITCD"][TEXT]           = $row["UNITCD"];
        $data["SEQ"][NUMBER]            = $row["SEQ"];
        $data["REMARK"][TEXT]           = $row["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        //追加
        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT");
        return $query;
   }

}
?>
