<?php

require_once('for_php7.php');

class knjz219aQuery extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //学校マスタ学校名
    function getSchoolMst($model, $year) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR='{$year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        return $query;
    }

    //基準の計算方法：学校マスタより取得
    function getStandard($model, $year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     T1.GVAL_CALC, ";
        $query .= "     L1.NAME1 as LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1  ";
        $query .= "            ON L1.NAMECD1 = '{$namecd1}'  ";
        $query .= "           AND L1.NAMECD2 = T1.GVAL_CALC ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR='{$year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        return $query;
    }
    //「評定計算方法」欄のコンボボックス：名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 as VALUE, ";
        $query .= "     NAME1 as LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1='{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }
    //合併先科目一覧：科目合併設定データより取得。単位加算のみ。
    function getList($year, $model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.COMBINED_CLASSCD, ";
            $query .= "     T1.COMBINED_SCHOOL_KIND, ";
            $query .= "     T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     T2.SUBCLASSNAME, ";
        $query .= "     T3.GVAL_CALC ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        $query .= "     INNER JOIN V_SUBCLASS_MST T2 ";
        $query .= "             ON T2.YEAR=T1.YEAR ";
        $query .= "            AND T2.SUBCLASSCD=T1.COMBINED_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            AND T2.CLASSCD=T1.COMBINED_CLASSCD ";
            $query .= "            AND T2.SCHOOL_KIND=T1.COMBINED_SCHOOL_KIND ";
            $query .= "            AND T2.CURRICULUM_CD=T1.COMBINED_CURRICULUM_CD ";
        }
        $query .= "      LEFT JOIN COMB_GCALC_DAT T3 ";
        $query .= "             ON T3.YEAR=T1.YEAR ";
        $query .= "            AND T3.COMBINED_SUBCLASSCD=T1.COMBINED_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            AND T3.COMBINED_CLASSCD=T1.COMBINED_CLASSCD ";
            $query .= "            AND T3.COMBINED_SCHOOL_KIND=T1.COMBINED_SCHOOL_KIND ";
            $query .= "            AND T3.COMBINED_CURRICULUM_CD=T1.COMBINED_CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.CALCULATE_CREDIT_FLG = '2' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.COMBINED_SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.COMBINED_SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.COMBINED_SUBCLASSCD ";
        return $query;
    }

    //割合設定科目
    function getListWeight($year, $model) {
        $query  = " WITH CNT_T AS ( ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT, ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     T1.COMBINED_CLASSCD, ";
        $query .= "     T1.COMBINED_SCHOOL_KIND, ";
        $query .= "     T1.COMBINED_CURRICULUM_CD ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        $query .= "      LEFT JOIN COMB_GCALC_DAT T3 ";
        $query .= "             ON T3.YEAR=T1.YEAR ";
        $query .= "            AND T3.COMBINED_SUBCLASSCD=T1.COMBINED_SUBCLASSCD ";
        $query .= "            AND T3.COMBINED_CLASSCD=T1.COMBINED_CLASSCD ";
        $query .= "            AND T3.COMBINED_SCHOOL_KIND=T1.COMBINED_SCHOOL_KIND ";
        $query .= "            AND T3.COMBINED_CURRICULUM_CD=T1.COMBINED_CURRICULUM_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.CALCULATE_CREDIT_FLG = '2' ";
        $query .= "     AND T3.GVAL_CALC = '3' ";
        $query .= " GROUP BY ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     T1.COMBINED_CLASSCD, ";
        $query .= "     T1.COMBINED_SCHOOL_KIND, ";
        $query .= "     T1.COMBINED_CURRICULUM_CD ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     T1.COMBINED_CLASSCD, ";
        $query .= "     T1.COMBINED_SCHOOL_KIND, ";
        $query .= "     T1.COMBINED_CURRICULUM_CD, ";
        $query .= "     I1.SUBCLASSNAME AS COMBINED_SUBCLASS_NAME, ";
        $query .= "     T1.ATTEND_SUBCLASSCD, ";
        $query .= "     T1.ATTEND_CLASSCD, ";
        $query .= "     T1.ATTEND_SCHOOL_KIND, ";
        $query .= "     T1.ATTEND_CURRICULUM_CD, ";
        $query .= "     T1.WEIGHTING, ";
        $query .= "     I2.SUBCLASSNAME AS ATTEND_SUBCLASS_NAME, ";
        $query .= "     CNT_T.CNT, ";
        $query .= "     T3.GVAL_CALC ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        $query .= "     INNER JOIN V_SUBCLASS_MST I1 ";
        $query .= "             ON I1.YEAR=T1.YEAR ";
        $query .= "            AND I1.SUBCLASSCD=T1.COMBINED_SUBCLASSCD ";
        $query .= "            AND I1.CLASSCD=T1.COMBINED_CLASSCD ";
        $query .= "            AND I1.SCHOOL_KIND=T1.COMBINED_SCHOOL_KIND ";
        $query .= "            AND I1.CURRICULUM_CD=T1.COMBINED_CURRICULUM_CD ";
        $query .= "     INNER JOIN V_SUBCLASS_MST I2 ";
        $query .= "             ON I2.YEAR=T1.YEAR ";
        $query .= "            AND I2.SUBCLASSCD=T1.ATTEND_SUBCLASSCD ";
        $query .= "            AND I2.CLASSCD=T1.ATTEND_CLASSCD ";
        $query .= "            AND I2.SCHOOL_KIND=T1.ATTEND_SCHOOL_KIND ";
        $query .= "            AND I2.CURRICULUM_CD=T1.ATTEND_CURRICULUM_CD ";
        $query .= "      LEFT JOIN COMB_GCALC_DAT T3 ";
        $query .= "             ON T3.YEAR=T1.YEAR ";
        $query .= "            AND T3.COMBINED_SUBCLASSCD=T1.COMBINED_SUBCLASSCD ";
        $query .= "            AND T3.COMBINED_CLASSCD=T1.COMBINED_CLASSCD ";
        $query .= "            AND T3.COMBINED_SCHOOL_KIND=T1.COMBINED_SCHOOL_KIND ";
        $query .= "            AND T3.COMBINED_CURRICULUM_CD=T1.COMBINED_CURRICULUM_CD ";
        $query .= "      LEFT JOIN CNT_T ON CNT_T.COMBINED_SUBCLASSCD=T1.COMBINED_SUBCLASSCD ";
        $query .= "            AND CNT_T.COMBINED_CLASSCD=T1.COMBINED_CLASSCD ";
        $query .= "            AND CNT_T.COMBINED_SCHOOL_KIND=T1.COMBINED_SCHOOL_KIND ";
        $query .= "            AND CNT_T.COMBINED_CURRICULUM_CD=T1.COMBINED_CURRICULUM_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.CALCULATE_CREDIT_FLG = '2' ";
        $query .= "     AND T3.GVAL_CALC = '3' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.COMBINED_SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.COMBINED_SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     T1.ATTEND_SUBCLASSCD ";
        return $query;
    }

    //レコード存在チェック用
    function getCntCombGcalcDat($year, $combSubclass, $combClass, $combSchoolkind, $combCurriculumcd, $model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) as CNT ";
        $query .= " FROM ";
        $query .= "     COMB_GCALC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " AND COMBINED_SUBCLASSCD = '{$combSubclass}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND COMBINED_CLASSCD = '{$combClass}' ";
            $query .= " AND COMBINED_SCHOOL_KIND = '{$combSchoolkind}' ";
            $query .= " AND COMBINED_CURRICULUM_CD = '{$combCurriculumcd}' ";
        }
        return $query;
    }
    //更新
    function getUpdateQuery($year, $combSubclass, $gvalCalc, $combClass, $combSchoolkind, $combCurriculumcd, $model) {
        $data = array();
        $data["GVAL_CALC"][TEXT]            = $gvalCalc;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "      YEAR = '{$year}' ";
        $where .= " AND  COMBINED_SUBCLASSCD = '{$combSubclass}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= " AND  COMBINED_CLASSCD = '{$combClass}' ";
            $where .= " AND  COMBINED_SCHOOL_KIND = '{$combSchoolkind}' ";
            $where .= " AND  COMBINED_CURRICULUM_CD = '{$combCurriculumcd}' ";
        }
        $query = Query::updateSQL($data, "COMB_GCALC_DAT", $where);
        return $query;
    }
    //追加
    function getInsertQuery($year, $combSubclass, $gvalCalc, $combClass, $combSchoolkind, $combCurriculumcd, $model) {
        $data = array();
        $data["YEAR"][TEXT]                 = $year;
        $data["COMBINED_SUBCLASSCD"][TEXT]  = $combSubclass;
        $data["COMBINED_CLASSCD"][TEXT]  = $combClass;
        $data["COMBINED_SCHOOL_KIND"][TEXT]  = $combSchoolkind;
        $data["COMBINED_CURRICULUM_CD"][TEXT]  = $combCurriculumcd;
        $data["GVAL_CALC"][TEXT]            = $gvalCalc;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "COMB_GCALC_DAT");
        return $query;
    }

    //割合以外の場合
    function getUpdateWeightClearQuery($year, $combSubclass, $model) {
        $data = array();
        $data["WEIGHTING"][NUMBER]          = NULL;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     REPLACECD = '1' ";
        $where .= "     AND YEAR = '{$year}' ";
        $where .= "     AND COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD = '{$combSubclass}' ";

        $query = Query::updateSQL($data, "SUBCLASS_REPLACE_COMBINED_DAT", $where);
        return $query;
    }

    //更新
    function getUpdateAttendQuery($year, $combSubclass, $attendSubclass, $setWeight, $model) {
        $data = array();
        $data["WEIGHTING"][NUMBER]          = $setWeight;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     REPLACECD = '1' ";
        $where .= "     AND YEAR = '{$year}' ";
        $where .= "     AND COMBINED_CLASSCD || COMBINED_SCHOOL_KIND || COMBINED_CURRICULUM_CD || COMBINED_SUBCLASSCD = '{$combSubclass}' ";
        $where .= "     AND ATTEND_CLASSCD || ATTEND_SCHOOL_KIND || ATTEND_CURRICULUM_CD || ATTEND_SUBCLASSCD = '{$attendSubclass}' ";

        $query = Query::updateSQL($data, "SUBCLASS_REPLACE_COMBINED_DAT", $where);
        return $query;
    }
}
?>