<?php

require_once('for_php7.php');

class knjd425_6Query extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER as VALUE, ";
        $query .= "     SEMESTERNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //取得
    function getNameMstPattern($model, $name2, $fieldName) {
        $query  = " SELECT ";
        $query .= "     {$fieldName} ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->exp_year}' ";
        $query .= "     AND NAMECD1 = 'A035' ";
        $query .= "     AND NAMECD2 = '{$name2}' ";

        return $query;
    }

    //グループ、履修情報取得
    function getViewGradeKindSchreg($model, $flg="") {
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
        $query .= " AND T1.SEMESTER = '9' ";
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
        $query .= " AND SEMESTER    = '9' ";
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
    function getGuidancePattern($model) {
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
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

   //履歴情報の表示
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     MAIN.YEAR, ";
        $query .= "     MAIN.SEMESTER, ";
        $query .= "     SEMS.SEMESTERNAME, ";
        $query .= "     MAIN.SCHREGNO, ";
        $query .= "     MAIN.CLASSCD, ";
        $query .= "     MAIN.SCHOOL_KIND, ";
        $query .= "     MAIN.CURRICULUM_CD, ";
        $query .= "     MAIN.SUBCLASSCD, ";
        $query .= "     case ";
        $query .= "         when COMP.UNIT_AIM_DIV = '1' then ";
        $query .= "              MAIN.CLASSCD || '-' || MAIN.SCHOOL_KIND || '-' || MAIN.CURRICULUM_CD || '-' || MAIN.SUBCLASSCD || ':' || SUBM.SUBCLASSNAME || '(' || YMST.UNITNAME || ')' ";
        $query .= "         else MAIN.CLASSCD || '-' || MAIN.SCHOOL_KIND || '-' || MAIN.CURRICULUM_CD || '-' || MAIN.SUBCLASSCD || ':' || SUBM.SUBCLASSNAME ";
        $query .= "     end  AS SET_SUBCLASSNAME, ";
        $query .= "     MAIN.CLASSCD || '-' || MAIN.SCHOOL_KIND || '-' || MAIN.CURRICULUM_CD || '-' || MAIN.SUBCLASSCD AS SET_SUBCLASSCD, ";
        $query .= "     MAIN.UNITCD, ";
        $query .= "     MAIN.SEQ, ";
        $query .= "     MAIN.REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT MAIN ";
        $query .= "     LEFT JOIN SEMESTER_MST SEMS ON SEMS.YEAR     = MAIN.YEAR ";
        $query .= "                                AND SEMS.SEMESTER = MAIN.SEMESTER ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD       = MAIN.CLASSCD ";
        $query .= "                                AND SUBM.SCHOOL_KIND   = MAIN.SCHOOL_KIND ";
        $query .= "                                AND SUBM.CURRICULUM_CD = MAIN.CURRICULUM_CD ";
        $query .= "                                AND SUBM.SUBCLASSCD    = MAIN.SUBCLASSCD ";
        $query .= "     LEFT JOIN GRADE_KIND_UNIT_GROUP_YMST YMST ";
        $query .= "          ON YMST.YEAR               = MAIN.YEAR ";
        $query .= "         AND YMST.SEMESTER           = '9' ";
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
        $query .= "         MAIN.YEAR     = '{$model->exp_year}' ";
        $query .= "     AND MAIN.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND MAIN.GUIDANCE_PATTERN = '{$model->schregInfo["GUIDANCE_PATTERN"]}' ";
        $query .= " ORDER BY ";
        if ($model->sort[$model->getSort] == 1) {
            $query .= "     MAIN.CLASSCD, ";
            $query .= "     MAIN.CURRICULUM_CD, ";
            $query .= "     MAIN.SUBCLASSCD, ";
            $query .= "     MAIN.UNITCD, ";
            $query .= "     MAIN.SEMESTER, ";
            $query .= "     MAIN.SEQ ";
        } else {
            $query .= "     MAIN.SEMESTER, ";
            $query .= "     MAIN.CLASSCD, ";
            $query .= "     MAIN.CURRICULUM_CD, ";
            $query .= "     MAIN.SUBCLASSCD, ";
            $query .= "     MAIN.UNITCD, ";
            $query .= "     MAIN.SEQ ";
        }

        return $query;
    }

    //科目取得
    function getSubclass($model) {
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
        $query .= "         T1.YEAR = '".$model->exp_year."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //単元がセットされているか取得
    function getUnitAimDiv($model, $setSubclasscd="") {
        $query  = " SELECT ";
        $query .= "     UNIT_AIM_DIV ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR               = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER           = '9' ";
        $query .= "     AND GAKUBU_SCHOOL_KIND = '".$model->schregInfo["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= "     AND GHR_CD             = '".$model->schregInfo["GHR_CD"]."' ";
        $query .= "     AND GRADE              = '".$model->schregInfo["GRADE"]."' ";
        $query .= "     AND HR_CLASS           = '".$model->schregInfo["HR_CLASS"]."' ";
        $query .= "     AND CONDITION          = '".$model->schregInfo["CONDITION"]."' ";
        $query .= "     AND GROUPCD            = '".$model->schregInfo["GROUPCD"]."' ";
        if (!$setSubclasscd) {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        } else {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$setSubclasscd."' ";
        }

        return $query;
    }

    //単元取得
    function getUnit($model, $setSubclasscd="", $setUnitcd="") {
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
        $query .= " AND T1.SEMESTER     = '9' ";
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
    function getItemName($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR                = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER            = '9' ";
        $query .= "     AND GAKUBU_SCHOOL_KIND  = '".$model->schregInfo["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= "     AND CONDITION           = '".$model->schregInfo["CONDITION"]."' ";
        $query .= "     AND GUIDANCE_PATTERN    = '".$model->schregInfo["GUIDANCE_PATTERN"]."' ";

        return $query;
    }

    //１レコード取得 -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT
    function getHreportGuidanceSchregSubclassDat($model) {
        $semester = ($model->patternB) ? $model->semester: '9';

        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEQ, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER    = '{$semester}' ";
        $query .= "     AND SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        if ($model->cmd == 'list_set') {
            $query .= "     AND UNITCD      = '{$model->unitcd}' ";
        } else {
            if ($model->unit_aim_div == '1') {
                $query .= "     AND UNITCD      = '{$model->unitcd}' ";
            } else {
                $query .= "     AND UNITCD      = '{$model->unitcd00}' ";
            }
        }
        $query .= "     AND GUIDANCE_PATTERN = '{$model->schregInfo["GUIDANCE_PATTERN"]}' ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $model->subclasscd);

        foreach($model->itemNameArr as $nameCd2 => $name1) {
            $semester = ($model->patternB) ? $model->semester: '9';

            //存在チェック
            $query = knjd425_6Query::getHreportGuidanceSchregSubclassDatCnt($model, $semester, $nameCd2);
            $check = $db->getOne($query);

            $data = array();
            $data["REMARK"][TEXT]           = $this->field["REMARK_{$nameCd2}"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            if ($check == 0) {
                $data["YEAR"][TEXT]             = $model->exp_year;
                $data["SEMESTER"][TEXT]         = $semester;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["CLASSCD"][TEXT]          = $classCd;
                $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
                $data["SUBCLASSCD"][TEXT]       = $subclassCd;
                $data["UNITCD"][TEXT]           = ($model->unit_aim_div == '1') ? $model->unitcd: $model->unitcd00;
                $data["GUIDANCE_PATTERN"][TEXT] = $model->schregInfo["GUIDANCE_PATTERN"];
                $data["SEQ"][NUMBER]            = $nameCd2;
                //追加
                $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT");
                $db->query($query);

            } else {
                $where  = " WHERE ";
                $where .= "         YEAR         = '{$model->exp_year}' ";
                $where .= "     AND SEMESTER     = '{$semester}' ";
                $where .= "     AND SCHREGNO     = '{$model->schregno}' ";
                $where .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
                if ($model->unit_aim_div == '1') {
                    $where .= "     AND UNITCD           = '{$model->unitcd}' ";
                } else {
                    $where .= "     AND UNITCD           = '{$model->unitcd00}' ";
                }
                $where .= "     AND GUIDANCE_PATTERN = '{$model->schregInfo["GUIDANCE_PATTERN"]}' ";
                $where .= "     AND SEQ              = {$nameCd2} ";
                //更新
                $query = Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT", $where);
                $db->query($query);

            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

   //DELETE -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT
   function &getDeleteQuery($model) {
        $semester = ($model->patternB) ? $model->semester: '9';

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER    = '{$semester}' ";
        $query .= "     AND SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        if ($model->unit_aim_div == '1') {
            $query .= "     AND UNITCD           = '{$model->unitcd}' ";
        } else {
            $query .= "     AND UNITCD           = '{$model->unitcd00}' ";
        }
        $query .= "     AND GUIDANCE_PATTERN = '{$model->schregInfo["GUIDANCE_PATTERN"]}' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
   }

   // カウント -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT 
   function getHreportGuidanceSchregSubclassDatCnt($model, $semester, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER    = '{$semester}' ";
        $query .= "     AND SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        if ($model->unit_aim_div == '1') {
            $query .= "     AND UNITCD           = '{$model->unitcd}' ";
        } else {
            $query .= "     AND UNITCD           = '{$model->unitcd00}' ";
        }
        $query .= "     AND GUIDANCE_PATTERN = '{$model->schregInfo["GUIDANCE_PATTERN"]}' ";
        $query .= "     AND SEQ              = {$seq} ";

        return $query;
   }
}
?>
