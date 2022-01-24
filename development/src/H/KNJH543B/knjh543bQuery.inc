<?php

require_once('for_php7.php');

class knjh543bQuery extends Query {

    //基準の計算方法(4:合算) と 計算方法コンボ(1:平均値 3:最大値 4:合算)
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
    //合併先科目一覧
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
        ////////////////////////////////////////////////////////////
        //子のチェックボックスは表示しない
        $query .= "     AND T1.COMBINED_SUBCLASSCD NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             A1.ATTEND_SUBCLASSCD ";
        $query .= "         FROM ";
        $query .= "             PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT A1 ";
        $query .= "         WHERE ";
        $query .= "                 A1.REPLACECD        = '1' ";
        $query .= "             AND A1.YEAR             = '{$year}' ";
        $query .= "             AND A1.SEMESTER         = '{$model->field["SEMESTER"]}' ";
        $query .= "             AND A1.PROFICIENCYDIV   = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "             AND A1.PROFICIENCYCD    = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "             AND A1.DIV              = '{$model->field["DIV"]}' ";
        $query .= "             AND A1.GRADE            = '{$model->field["GRADE"]}' ";
        //区分ラジオ 01:02:ダミー 03:コース 04:コースグループ
        if ($model->field["DIV"] == "04") {
            $query .= "         AND A1.COURSECD || A1.MAJORCD || A1.COURSECODE = '0{$model->field["GROUP_CD"]}0000' ";
        } else {
            $query .= "         AND A1.COURSECD || A1.MAJORCD || A1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        $query .= "         GROUP BY ";
        $query .= "             A1.ATTEND_SUBCLASSCD ";
        $query .= "         ) ";
        ////////////////////////////////////////////////////////////
        $query .= " ORDER BY ";
        $query .= "     T1.COMBINED_SUBCLASSCD ";
        return $query;
    }
    //履歴一覧
    function getListRireki($model) {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     L1.SUBCLASS_NAME, ";
        $query .= "     T1.GVAL_CALC, ";
        $query .= "     L2.NAME1 as GVAL_NAME ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_COMB_GCALC_EXEC_DAT T1 ";
        $query .= "     LEFT JOIN PROFICIENCY_SUBCLASS_MST L1 ON L1.PROFICIENCY_SUBCLASS_CD = T1.COMBINED_SUBCLASSCD ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'Z027' AND L2.NAMECD2 = T1.GVAL_CALC ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR='{$year}' ";
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
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     T1.COMBINED_SUBCLASSCD ";
        return $query;
    }
    //実行日付・時間を取得
    function getCalcDateTime() {
        $query  = " with t_date_time (CALC_DATE,CALC_TIME) as ( ";
        $query .= " values( ";
        $query .= "     date(sysdate()), ";
        $query .= "     time(sysdate()) ";
        $query .= " )) ";
        $query .= "  ";
        $query .= " select * from t_date_time ";
        return $query;
    }
    //実力科目合併自動計算履歴データ・追加
    function getInsertRireki($combSubclass, $gvalCalc, $calcDate, $calcTime, $model) {
        $year = CTRL_YEAR;

        $data = array();
        $data["CALC_DATE"][TEXT]            = $calcDate;
        $data["CALC_TIME"][TEXT]            = $calcTime;
        $data["COMBINED_SUBCLASSCD"][TEXT]  = $combSubclass;
        $data["YEAR"][TEXT]                 = $year;
        $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
        $data["PROFICIENCYDIV"][TEXT]       = $model->field["PROFICIENCYDIV"];
        $data["PROFICIENCYCD"][TEXT]        = $model->field["PROFICIENCYCD"];
        $data["DIV"][TEXT]                  = $model->field["DIV"];
        $data["GRADE"][TEXT]                = $model->field["GRADE"];
        $data["COURSECD"][TEXT]             = $model->field["DIV"] == "03" ? substr($model->field["COURSE"],0,1) : "0";
        $data["MAJORCD"][TEXT]              = $model->field["DIV"] == "03" ? substr($model->field["COURSE"],1,3) : $model->field["GROUP_CD"];
        $data["COURSECODE"][TEXT]           = $model->field["DIV"] == "03" ? substr($model->field["COURSE"],4,4) : "0000";
        $data["GVAL_CALC"][TEXT]            = $gvalCalc;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "PROFICIENCY_COMB_GCALC_EXEC_DAT");
        return $query;
    }
    //合併元先科目があればそれを先に生成する・・・合併設定（親・子・孫）の対応
    function getListMotoSaki($model, $combSubclass) {
        $year = CTRL_YEAR;
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        $query .= "     value(T3.GVAL_CALC, '4') AS GVAL_CALC "; //基準の計算方法 4:合算
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
        $query .= "     AND T1.COMBINED_SUBCLASSCD IN ( ";
        $query .= "         SELECT ";
        $query .= "             A1.ATTEND_SUBCLASSCD ";
        $query .= "         FROM ";
        $query .= "             PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT A1 ";
        $query .= "         WHERE ";
        $query .= "                 A1.REPLACECD        = '1' ";
        $query .= "             AND A1.YEAR             = '{$year}' ";
        $query .= "             AND A1.SEMESTER         = '{$model->field["SEMESTER"]}' ";
        $query .= "             AND A1.PROFICIENCYDIV   = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "             AND A1.PROFICIENCYCD    = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "             AND A1.DIV              = '{$model->field["DIV"]}' ";
        $query .= "             AND A1.GRADE            = '{$model->field["GRADE"]}' ";
        //区分ラジオ 01:02:ダミー 03:コース 04:コースグループ
        if ($model->field["DIV"] == "04") {
            $query .= "         AND A1.COURSECD || A1.MAJORCD || A1.COURSECODE = '0{$model->field["GROUP_CD"]}0000' ";
        } else {
            $query .= "         AND A1.COURSECD || A1.MAJORCD || A1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        $query .= "             AND A1.COMBINED_SUBCLASSCD = '{$combSubclass}' ";
        $query .= "         ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.COMBINED_SUBCLASSCD ";
        return $query;
    }
    //処理生徒の合併先科目の素点を自動計算
    function getScore($model, $combSubclass, $gvalCalc) {
        $year = CTRL_YEAR;
        if ($model->Properties["weightingHyouki"] == "1") {
            $score = "CASE WHEN T1.WEIGHTING IS NOT NULL AND 0 < T1.WEIGHTING THEN (T0.SCORE * T1.WEIGHTING)/100 ELSE T0.SCORE END";
        } else {
            $score = "CASE WHEN T1.WEIGHTING IS NOT NULL AND 0 < T1.WEIGHTING THEN T0.SCORE * T1.WEIGHTING ELSE T0.SCORE END";
        }
        $query  = " SELECT ";
        $query .= "     T0.SCHREGNO, ";
        //計算方法(1:平均値 3:最大値 4:合算)
        $query .= "     CASE WHEN MAX(T0.SCORE_DI) = '*' THEN NULL ELSE ";
        if ($gvalCalc == "1") {
            $query .= "     SMALLINT(ROUND(FLOAT(SUM({$score}))/COUNT(T0.PROFICIENCY_SUBCLASS_CD),0)) ";
        } else if ($gvalCalc == "3") {
            $query .= "     SMALLINT(ROUND(FLOAT(MAX({$score})),0)) ";
        } else {
            $query .= "     SMALLINT(ROUND(FLOAT(SUM({$score})),0)) ";
        }
        $query .= "          END AS SCORE, ";
        $query .= "     MAX(T0.SCORE_DI) AS SCORE_DI ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_DAT T0 ";
        $query .= "     INNER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             T1.ATTEND_SUBCLASSCD, ";
        $query .= "             T1.WEIGHTING ";
        $query .= "         FROM ";
        $query .= "             PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT T1 ";
        $query .= "         WHERE ";
        $query .= "                 T1.REPLACECD        = '1' ";
        $query .= "             AND T1.YEAR             = '{$year}' ";
        $query .= "             AND T1.SEMESTER         = '{$model->field["SEMESTER"]}' ";
        $query .= "             AND T1.PROFICIENCYDIV   = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "             AND T1.PROFICIENCYCD    = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "             AND T1.DIV              = '{$model->field["DIV"]}' ";
        $query .= "             AND T1.GRADE            = '{$model->field["GRADE"]}' ";
        //区分ラジオ 01:02:ダミー 03:コース 04:コースグループ
        if ($model->field["DIV"] == "04") {
            $query .= "         AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '0{$model->field["GROUP_CD"]}0000' ";
        } else {
            $query .= "         AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        $query .= "             AND T1.COMBINED_SUBCLASSCD = '{$combSubclass}' ";
        $query .= "         ) T1 ON T1.ATTEND_SUBCLASSCD = T0.PROFICIENCY_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "         T0.YEAR                    = '{$year}' ";
        $query .= "     AND T0.SEMESTER                = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T0.PROFICIENCYDIV          = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     AND T0.PROFICIENCYCD           = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "     AND T0.SCHREGNO                IN ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T1 ";
        $query .= "         WHERE ";
        $query .= "                 T1.YEAR     = '{$year}' ";
        $query .= "             AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "             AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //区分ラジオ 01:02:ダミー 03:コース 04:コースグループ
        if ($model->field["DIV"] == "04") {
            $query .= "         AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN ( ";
            $query .= "             SELECT ";
            $query .= "                 G1.COURSECD || G1.MAJORCD || G1.COURSECODE AS COURSE ";
            $query .= "             FROM ";
            $query .= "                 COURSE_GROUP_CD_DAT G1 ";
            $query .= "             WHERE ";
            $query .= "                     G1.YEAR     = '{$year}' ";
            $query .= "                 AND G1.GRADE    = '{$model->field["GRADE"]}' ";
            $query .= "                 AND G1.GROUP_CD = '{$model->field["GROUP_CD"]}' ";
            $query .= "             ) ";
        } else {
            $query .= "         AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        $query .= "         ) ";
        $query .= " GROUP BY ";
        $query .= "     T0.SCHREGNO ";

        return $query;
    }
    //削除
    function getDeleteQuery($model, $schregNo, $combSubclass) {
        $year = CTRL_YEAR;
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR                    = '{$year}' ";
        $query .= "     AND SEMESTER                = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND PROFICIENCYDIV          = '{$model->field["PROFICIENCYDIV"]}' ";
        $query .= "     AND PROFICIENCYCD           = '{$model->field["PROFICIENCYCD"]}' ";
        $query .= "     AND SCHREGNO                = '{$schregNo}' ";
        $query .= "     AND PROFICIENCY_SUBCLASS_CD = '{$combSubclass}' ";
        return $query;
    }
    //追加
    function getInsertQuery($model, $schregNo, $combSubclass, $score, $scoreDI) {
        $year = CTRL_YEAR;

        $data = array();
        $data["YEAR"][TEXT]                     = $year;
        $data["SEMESTER"][TEXT]                 = $model->field["SEMESTER"];
        $data["PROFICIENCYDIV"][TEXT]           = $model->field["PROFICIENCYDIV"];
        $data["PROFICIENCYCD"][TEXT]            = $model->field["PROFICIENCYCD"];
        $data["SCHREGNO"][TEXT]                 = $schregNo;
        $data["PROFICIENCY_SUBCLASS_CD"][TEXT]  = $combSubclass;
        $data["SCORE"][NUMBER]                  = $score;
        $data["SCORE_DI"][TEXT]                 = $scoreDI;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $query = Query::insertSQL($data, "PROFICIENCY_DAT");
        return $query;
    }
}
?>