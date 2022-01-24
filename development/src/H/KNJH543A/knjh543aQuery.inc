<?php

require_once('for_php7.php');

class knjh543aQuery extends Query {

    //基準の計算方法(4:合算) と 計算方法コンボ(3:最大値 4:合算)
    function getNameMstZ027($namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z027' ";
        if ($namecd2 != "") {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    //学期コンボ
    function getSemester() {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER IN ( ";
        $query .= "         SELECT ";
        $query .= "             Y1.SEMESTER ";
        $query .= "         FROM ";
        $query .= "             PROFICIENCY_YMST Y1 ";
        $query .= "         WHERE ";
        $query .= "                 Y1.YEAR     = '{$year}' ";
        $query .= "         GROUP BY ";
        $query .= "             Y1.SEMESTER ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    //実力区分コンボ
    function getProficiencyDiv($model) {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H508' ";
        $query .= "     AND NAMECD2 IN ( ";
        $query .= "         SELECT ";
        $query .= "             Y1.PROFICIENCYDIV ";
        $query .= "         FROM ";
        $query .= "             PROFICIENCY_YMST Y1 ";
        $query .= "         WHERE ";
        $query .= "                 Y1.YEAR             = '{$year}' ";
        $query .= "             AND Y1.SEMESTER         = '{$model->field["SEMESTER"]}' ";
        $query .= "         GROUP BY ";
        $query .= "             Y1.PROFICIENCYDIV ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
        return $query;
    }
    //実力コードコンボ
    function getProficiencyCd($model) {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     PROFICIENCYCD || ':' || PROFICIENCYNAME1 AS LABEL, ";
        $query .= "     PROFICIENCYCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_MST ";
        $query .= " WHERE ";
        $query .= "     PROFICIENCYDIV = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     AND PROFICIENCYCD IN ( ";
        $query .= "         SELECT ";
        $query .= "             Y1.PROFICIENCYCD ";
        $query .= "         FROM ";
        $query .= "             PROFICIENCY_YMST Y1 ";
        $query .= "         WHERE ";
        $query .= "                 Y1.YEAR             = '{$year}' ";
        $query .= "             AND Y1.SEMESTER         = '{$model->field["SEMESTER"]}' ";
        $query .= "             AND Y1.PROFICIENCYDIV   = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "         GROUP BY ";
        $query .= "             Y1.PROFICIENCYCD ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    //学年コンボ
    function getGrade($model) {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND GRADE IN ( ";
        $query .= "         SELECT ";
        $query .= "             Y1.GRADE ";
        $query .= "         FROM ";
        $query .= "             PROFICIENCY_YMST Y1 ";
        $query .= "         WHERE ";
        $query .= "                 Y1.YEAR             = '{$year}' ";
        $query .= "             AND Y1.SEMESTER         = '{$model->field["SEMESTER"]}' ";
        $query .= "             AND Y1.PROFICIENCYDIV   = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "             AND Y1.PROFICIENCYCD    = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "         GROUP BY ";
        $query .= "             Y1.GRADE ";
        $query .= "     ) ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    //課程学科コースコンボ
    function getCourse($model) {
        $year = CTRL_YEAR;
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE || ':' || L3.COURSENAME || L3.MAJORNAME || L4.COURSECODENAME AS LABEL, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L3 ";
        $query .= "         ON  L3.YEAR = T1.YEAR ";
        $query .= "         AND L3.COURSECD = T1.COURSECD ";
        $query .= "         AND L3.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L4 ";
        $query .= "         ON  L4.YEAR = T1.YEAR ";
        $query .= "         AND L4.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    //コースグループコンボ
    function getGroupCd($model) {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     GROUP_CD || ':' || GROUP_NAME AS LABEL, ";
        $query .= "     GROUP_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    //合併先科目一覧：科目合併設定データより取得。単位加算のみ。
    function getList($model) {
        $year = CTRL_YEAR;
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     T2.SUBCLASS_NAME, ";
        $query .= "     T3.GVAL_CALC ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT T1 ";
        $query .= "     INNER JOIN PROFICIENCY_SUBCLASS_MST T2 ";
        $query .= "             ON T2.PROFICIENCY_SUBCLASS_CD=T1.COMBINED_SUBCLASSCD ";
        $query .= "      LEFT JOIN PROFICIENCY_COMB_GCALC_DAT T3 ";
        $query .= "             ON T3.YEAR=T1.YEAR ";
        $query .= "            AND T3.SEMESTER=T1.SEMESTER ";
        $query .= "            AND T3.PROFICIENCYDIV=T1.PROFICIENCYDIV ";
        $query .= "            AND T3.PROFICIENCYCD=T1.PROFICIENCYCD ";
        $query .= "            AND T3.DIV=T1.DIV ";
        $query .= "            AND T3.GRADE=T1.GRADE ";
        $query .= "            AND T3.COURSECD=T1.COURSECD ";
        $query .= "            AND T3.MAJORCD=T1.MAJORCD ";
        $query .= "            AND T3.COURSECODE=T1.COURSECODE ";
        $query .= "            AND T3.COMBINED_SUBCLASSCD=T1.COMBINED_SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.REPLACECD        = '1' ";
        $query .= "     AND T1.YEAR             = '{$year}' ";
        $query .= "     AND T1.SEMESTER         = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.PROFICIENCYDIV   = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     AND T1.PROFICIENCYCD    = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "     AND T1.DIV              = '{$model->field["DIV"]}' ";
        $query .= "     AND T1.GRADE            = '{$model->field["GRADE"]}' ";
        //区分ラジオ 01:02:ダミー 03:コース 04:コースグループ
        if ($model->field["DIV"] == "04") {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '0{$model->field["GROUP_CD"]}0000' ";
        } else {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.COMBINED_SUBCLASSCD ";
        return $query;
    }
    //レコード存在チェック用
    function getCntCombGcalcDat($combSubclass, $model) {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     COUNT(*) as CNT ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_COMB_GCALC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER         = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND PROFICIENCYDIV   = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     AND PROFICIENCYCD    = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "     AND DIV              = '{$model->field["DIV"]}' ";
        $query .= "     AND GRADE            = '{$model->field["GRADE"]}' ";
        //区分ラジオ 01:02:ダミー 03:コース 04:コースグループ
        if ($model->field["DIV"] == "04") {
            $query .= "     AND COURSECD || MAJORCD || COURSECODE = '0{$model->field["GROUP_CD"]}0000' ";
        } else {
            $query .= "     AND COURSECD || MAJORCD || COURSECODE = '{$model->field["COURSE"]}' ";
        }
        $query .= "     AND COMBINED_SUBCLASSCD = '{$combSubclass}' ";
        return $query;
    }
    //更新
    function getUpdateQuery($combSubclass, $gvalCalc, $model) {
        $year = CTRL_YEAR;

        $data = array();
        $data["GVAL_CALC"][TEXT]            = $gvalCalc;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "      YEAR = '{$year}' ";
        $where .= "     AND SEMESTER         = '{$model->field["SEMESTER"]}' ";
        $where .= "     AND PROFICIENCYDIV   = '{$model->field["PROFICIENCYDIV"]}' ";
        $where .= "     AND PROFICIENCYCD    = '{$model->field["PROFICIENCYCD"]}' ";
        $where .= "     AND DIV              = '{$model->field["DIV"]}' ";
        $where .= "     AND GRADE            = '{$model->field["GRADE"]}' ";
        //区分ラジオ 01:02:ダミー 03:コース 04:コースグループ
        if ($model->field["DIV"] == "04") {
            $where .= "     AND COURSECD || MAJORCD || COURSECODE = '0{$model->field["GROUP_CD"]}0000' ";
        } else {
            $where .= "     AND COURSECD || MAJORCD || COURSECODE = '{$model->field["COURSE"]}' ";
        }
        $where .= "     AND  COMBINED_SUBCLASSCD = '{$combSubclass}' ";

        $query = Query::updateSQL($data, "PROFICIENCY_COMB_GCALC_DAT", $where);
        return $query;
    }
    //追加
    function getInsertQuery($combSubclass, $gvalCalc, $model) {
        $year = CTRL_YEAR;

        $data = array();
        $data["YEAR"][TEXT]                 = $year;
        $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
        $data["PROFICIENCYDIV"][TEXT]       = $model->field["PROFICIENCYDIV"];
        $data["PROFICIENCYCD"][TEXT]        = $model->field["PROFICIENCYCD"];
        $data["DIV"][TEXT]                  = $model->field["DIV"];
        $data["GRADE"][TEXT]                = $model->field["GRADE"];
        $data["COURSECD"][TEXT]             = $model->field["DIV"] == "03" ? substr($model->field["COURSE"],0,1) : "0";
        $data["MAJORCD"][TEXT]              = $model->field["DIV"] == "03" ? substr($model->field["COURSE"],1,3) : $model->field["GROUP_CD"];
        $data["COURSECODE"][TEXT]           = $model->field["DIV"] == "03" ? substr($model->field["COURSE"],4,4) : "0000";
        $data["COMBINED_SUBCLASSCD"][TEXT]  = $combSubclass;
        $data["GVAL_CALC"][TEXT]            = $gvalCalc;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "PROFICIENCY_COMB_GCALC_DAT");
        return $query;
    }
}
?>