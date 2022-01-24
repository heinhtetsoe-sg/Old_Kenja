<?php
class knjd219fQuery extends Query {

    //-------------------- 画面 --------------------

    //学校名取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtn = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtn;
    }

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
    function getSchoolMst($model) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //テスト種別取得
    function getTestcd($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "     SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV || '：' || TESTITEMNAME AS LABEL, ";
        $query .= "     TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SCORE_DIV   <> '09' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //基準の計算方法：学校マスタより取得
    function getStandard($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     T1.GVAL_CALC, ";
        $query .= "     L1.NAME1 as LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1  ";
        $query .= "            ON L1.NAMECD1 = '{$namecd1}'  ";
        $query .= "           AND L1.NAMECD2 = T1.GVAL_CALC ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //計算方法欄のコンボボックス：名称マスタより取得
    function getNameMst($namecd1) {
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

    //合併先科目一覧：科目合併設定データより取得
    function getList($model) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASS, ";
        $query .= "     T2.SUBCLASSNAME, ";
        $query .= "     T3.GVAL_CALC ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        $query .= "     INNER JOIN V_SUBCLASS_MST T2 ";
        $query .= "             ON T2.YEAR = T1.YEAR ";
        $query .= "            AND T2.CLASSCD = T1.COMBINED_CLASSCD ";
        $query .= "            AND T2.SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
        $query .= "            AND T2.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
        $query .= "            AND T2.SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
        $query .= "      LEFT JOIN COMB_GCALC_DAT T3 ";
        $query .= "             ON T3.YEAR = T1.YEAR ";
        $query .= "            AND T3.COMBINED_CLASSCD = T1.COMBINED_CLASSCD ";
        $query .= "            AND T3.COMBINED_SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
        $query .= "            AND T3.COMBINED_CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
        $query .= "            AND T3.COMBINED_SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        //$query .= "     AND T1.CALCULATE_CREDIT_FLG = '2' ";//単位加算
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.COMBINED_SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.COMBINED_SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     COMBINED_SUBCLASS ";

        return $query;
    }

    //履歴一覧：科目合併成績自動計算履歴データより取得。
    function getListRireki($model) {
        $query  = " SELECT ";
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASS, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     T1.GVAL_CALC, ";
        $query .= "     L2.NAME1 as GVAL_NAME, ";
        $query .= "     L3.TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     COMB_GCALC_EXEC_TESTITEM_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ";
        $query .= "          ON L1.CLASSCD = T1.COMBINED_CLASSCD ";
        $query .= "         AND L1.SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
        $query .= "         AND L1.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
        $query .= "         AND L1.SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'Z017' AND L2.NAMECD2 = T1.GVAL_CALC ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L3 ";
        $query .= "          ON L3.YEAR = T1.YEAR ";
        $query .= "         AND L3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND L3.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND L3.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND L3.SCORE_DIV = T1.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR='".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$model->field["TEST_CD"]}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.COMBINED_SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.COMBINED_SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CALC_DATE DESC, ";
        $query .= "     T1.CALC_TIME DESC, ";
        $query .= "     COMBINED_SUBCLASS ";

        return $query;
    }

    //-------------------- 実行(チェック) --------------------

    //合併先科目の講座名簿の存在チェック
    function getExistsChair($combSubclass, $model) {
        $query  = " SELECT ";
        $query .= "     count(*) as CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T1.CHAIRCD  = T2.CHAIRCD ";
        $query .= "     AND T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '{$model->semChair}' ";
        $query .= "     AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '{$combSubclass}' ";

        return $query;
    }

    //-------------------- 実行(取得) --------------------

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

    //-------------------- 実行(追加・更新) --------------------

    //科目合併成績自動計算履歴データ・追加
    function getInsertRireki($combSubclass, $gvalCalc, $calcRow, $model) {
        $arrSub = explode("-", $combSubclass);
        $sem    = substr($model->field["TEST_CD"], 0, 1);
        $kind   = substr($model->field["TEST_CD"], 1, 2);
        $item   = substr($model->field["TEST_CD"], 3, 2);
        $div    = substr($model->field["TEST_CD"], 5, 2);

        $data = array();
        $data["CALC_DATE"][TEXT]                = $calcRow["CALC_DATE"];
        $data["CALC_TIME"][TEXT]                = $calcRow["CALC_TIME"];
        $data["COMBINED_CLASSCD"][TEXT]         = $arrSub[0];
        $data["COMBINED_SCHOOL_KIND"][TEXT]     = $arrSub[1];
        $data["COMBINED_CURRICULUM_CD"][TEXT]   = $arrSub[2];
        $data["COMBINED_SUBCLASSCD"][TEXT]      = $arrSub[3];
        $data["GVAL_CALC"][TEXT]                = $gvalCalc;
        $data["YEAR"][TEXT]                     = CTRL_YEAR;
        $data["SEMESTER"][TEXT]                 = $sem;
        $data["TESTKINDCD"][TEXT]               = $kind;
        $data["TESTITEMCD"][TEXT]               = $item;
        $data["SCORE_DIV"][TEXT]                = $div;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "sysdate()";

        $query = Query::insertSQL($data, "COMB_GCALC_EXEC_TESTITEM_DAT");
        return $query;
    }

    //①成績データに存在しない生徒を追加
    function getInsertNonStdRec($combSubclass, $model) {
        $staffcd = STAFFCD;
        $sem    = substr($model->field["TEST_CD"], 0, 1);
        $kind   = substr($model->field["TEST_CD"], 1, 2);
        $item   = substr($model->field["TEST_CD"], 3, 2);
        $div    = substr($model->field["TEST_CD"], 5, 2);

        $query  = " INSERT INTO RECORD_SCORE_DAT ";
        $query .= " (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO,REGISTERCD,UPDATED) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     '{$sem}' as SEMESTER, ";
        $query .= "     '{$kind}' as TESTKINDCD, ";
        $query .= "     '{$item}' as TESTITEMCD, ";
        $query .= "     '{$div}' as SCORE_DIV, ";
        $query .= "     T2.CLASSCD, ";
        $query .= "     T2.SCHOOL_KIND, ";
        $query .= "     T2.CURRICULUM_CD, ";
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '{$staffcd}' as REGISTERCD, ";
        $query .= "     sysdate() as UPDATED ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = T2.YEAR AND ";
        $query .= "     T1.SEMESTER   = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD    = T2.CHAIRCD AND ";
        $query .= "     T2.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER   = '{$model->semChair}' AND ";
        $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '{$combSubclass}' AND ";
        $query .= "     T1.SCHREGNO NOT IN (SELECT ";
        $query .= "                             R1.SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             RECORD_SCORE_DAT R1 ";
        $query .= "                         WHERE ";
        $query .= "                             R1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                             R1.SEMESTER || R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV = '{$model->field["TEST_CD"]}' AND ";
        $query .= "                             R1.CLASSCD || '-' || R1.SCHOOL_KIND || '-' || R1.CURRICULUM_CD || '-' || R1.SUBCLASSCD = '{$combSubclass}' ";
        $query .= "                         ) ";

        return $query;
    }

    //科目毎・生徒毎に合併先科目の成績を自動計算し、成績データに登録する。
    function getUpdateList($combSubclass, $gvalCalc, $model) {
        $sem    = substr($model->field["TEST_CD"], 0, 1);
        $kind   = substr($model->field["TEST_CD"], 1, 2);
        $item   = substr($model->field["TEST_CD"], 3, 2);
        $div    = substr($model->field["TEST_CD"], 5, 2);
        $arr    = explode("-", $combSubclass);
        //②－Ａ）合併先科目のレコードが存在する生徒
        $query  = " WITH T_SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         R1.YEAR, ";
        $query .= "         R1.SCHREGNO, ";
        $query .= "         T2.COURSECD, ";
        $query .= "         T2.MAJORCD, ";
        $query .= "         T2.COURSECODE, ";
        $query .= "         T2.GRADE, ";
        //小分類「01」の時、満点を参照する
        if (substr($model->field["TEST_CD"], 5, 2) == "01") {
            $query .= "         VALUE(P1.PERFECT, 100) AS COMBINED_PERFECT, ";
        }
        $query .= "         S1.ATTEND_CLASSCD, ";
        $query .= "         S1.ATTEND_SCHOOL_KIND, ";
        $query .= "         S1.ATTEND_CURRICULUM_CD, ";
        $query .= "         S1.ATTEND_SUBCLASSCD, ";
        $query .= "         S1.WEIGHTING ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT R1 ";
        $query .= "         INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = R1.SCHREGNO ";
        $query .= "             AND T2.YEAR = R1.YEAR ";
        $query .= "             AND T2.SEMESTER = '{$model->semChair}' ";
        $query .= "         INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT S1 ON S1.YEAR = R1.YEAR ";
        $query .= "             AND S1.COMBINED_CLASSCD = R1.CLASSCD ";
        $query .= "             AND S1.COMBINED_SCHOOL_KIND = R1.SCHOOL_KIND ";
        $query .= "             AND S1.COMBINED_CURRICULUM_CD = R1.CURRICULUM_CD ";
        $query .= "             AND S1.COMBINED_SUBCLASSCD = R1.SUBCLASSCD ";
        //小分類「01」の時、満点を参照する
        if (substr($model->field["TEST_CD"], 5, 2) == "01") {
            //先科目の満点
            $query .= "         LEFT JOIN PERFECT_RECORD_DAT P1 ON P1.YEAR = R1.YEAR ";
            $query .= "             AND P1.SEMESTER = R1.SEMESTER ";
            $query .= "             AND P1.TESTKINDCD = R1.TESTKINDCD ";
            $query .= "             AND P1.TESTITEMCD = R1.TESTITEMCD ";
            $query .= "             AND P1.CLASSCD = R1.CLASSCD ";
            $query .= "             AND P1.SCHOOL_KIND = R1.SCHOOL_KIND ";
            $query .= "             AND P1.CURRICULUM_CD = R1.CURRICULUM_CD ";
            $query .= "             AND P1.SUBCLASSCD = R1.SUBCLASSCD ";
            $query .= "             AND P1.GRADE = CASE WHEN P1.DIV = '01' THEN '00' ELSE T2.GRADE END ";
            $query .= "             AND P1.COURSECD || P1.MAJORCD || P1.COURSECODE = CASE WHEN P1.DIV IN ('01','02') THEN '00000000' ELSE T2.COURSECD || T2.MAJORCD || T2.COURSECODE END ";
        }
        $query .= "     WHERE ";
        $query .= "         R1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         R1.SEMESTER = '{$sem}' AND R1.TESTKINDCD = '{$kind}' AND R1.TESTITEMCD = '{$item}' AND R1.SCORE_DIV = '{$div}' AND ";
        $query .= "         R1.CLASSCD = '{$arr[0]}' AND R1.SCHOOL_KIND = '{$arr[1]}' AND R1.CURRICULUM_CD = '{$arr[2]}' AND R1.SUBCLASSCD = '{$arr[3]}' ";
        $query .= "     ) ";
        //②－Ｂ）対応する合併元科目のレコード
        $query .= " , T_REC_MOTO AS ( ";
        $query .= "     SELECT ";
        $query .= "         R1.CLASSCD, ";
        $query .= "         R1.SCHOOL_KIND, ";
        $query .= "         R1.CURRICULUM_CD, ";
        $query .= "         R1.SUBCLASSCD, ";
        $query .= "         R1.SCHREGNO, ";
        $query .= "         R1.SCORE, ";
        //小分類「01」の時、満点を参照する
        if (substr($model->field["TEST_CD"], 5, 2) == "01") {
            $query .= "         R1.SCORE * S1.COMBINED_PERFECT / VALUE(P1.PERFECT, 100) AS SCORE100, "; //SCOREを100点満点に換算
        } else {
            $query .= "         R1.SCORE AS SCORE100, ";
        }
        $query .= "         S1.WEIGHTING, ";
        $query .= "         R1.VALUE_DI, ";
        $query .= "         C1.CREDITS ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT R1, ";
        $query .= "         T_SCHNO S1 ";
        $query .= "         LEFT JOIN CREDIT_MST C1 ON C1.YEAR = S1.YEAR ";
        $query .= "             AND C1.COURSECD = S1.COURSECD ";
        $query .= "             AND C1.MAJORCD = S1.MAJORCD ";
        $query .= "             AND C1.COURSECODE = S1.COURSECODE ";
        $query .= "             AND C1.GRADE = S1.GRADE ";
        $query .= "             AND C1.CLASSCD = S1.ATTEND_CLASSCD ";
        $query .= "             AND C1.SCHOOL_KIND = S1.ATTEND_SCHOOL_KIND ";
        $query .= "             AND C1.CURRICULUM_CD = S1.ATTEND_CURRICULUM_CD ";
        $query .= "             AND C1.SUBCLASSCD = S1.ATTEND_SUBCLASSCD ";
        //小分類「01」の時、満点を参照する
        if (substr($model->field["TEST_CD"], 5, 2) == "01") {
            //元科目の満点
            $query .= "         LEFT JOIN PERFECT_RECORD_DAT P1 ON P1.YEAR = S1.YEAR ";
            $query .= "             AND P1.SEMESTER = '{$sem}' AND P1.TESTKINDCD = '{$kind}' AND P1.TESTITEMCD = '{$item}' ";
            $query .= "             AND P1.CLASSCD = S1.ATTEND_CLASSCD ";
            $query .= "             AND P1.SCHOOL_KIND = S1.ATTEND_SCHOOL_KIND ";
            $query .= "             AND P1.CURRICULUM_CD = S1.ATTEND_CURRICULUM_CD ";
            $query .= "             AND P1.SUBCLASSCD = S1.ATTEND_SUBCLASSCD ";
            $query .= "             AND P1.GRADE = CASE WHEN P1.DIV = '01' THEN '00' ELSE S1.GRADE END ";
            $query .= "             AND P1.COURSECD || P1.MAJORCD || P1.COURSECODE = CASE WHEN P1.DIV IN ('01','02') THEN '00000000' ELSE S1.COURSECD || S1.MAJORCD || S1.COURSECODE END ";
        }
        $query .= "     WHERE ";
        $query .= "         R1.YEAR = S1.YEAR AND  ";
        $query .= "         R1.SEMESTER = '{$sem}' AND R1.TESTKINDCD = '{$kind}' AND R1.TESTITEMCD = '{$item}' AND R1.SCORE_DIV = '{$div}' AND ";
        $query .= "         R1.SCHREGNO = S1.SCHREGNO AND  ";
        $query .= "         R1.CLASSCD = S1.ATTEND_CLASSCD AND ";
        $query .= "         R1.SCHOOL_KIND = S1.ATTEND_SCHOOL_KIND AND ";
        $query .= "         R1.CURRICULUM_CD = S1.ATTEND_CURRICULUM_CD AND ";
        $query .= "         R1.SUBCLASSCD = S1.ATTEND_SUBCLASSCD ";
        $query .= "     ) ";
        //③合併先科目の成績を自動計算し、成績データに登録する。
        $query .= " , T_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         ROUND(AVG(FLOAT(SCORE100)),0) AS SCORE_AVG, "; //0:平均
        $query .= "         ROUND(SUM(FLOAT(SCORE)*CREDITS)/SUM(CREDITS),0) AS SCORE_CREDITS, ";//1:単位による重み付け
        $query .= "         MAX(SCORE) AS SCORE_MAX, ";                 //2:最大値
        if ($model->Properties["KNJD219F_wariai_round"] == "1") {
            $query .= "         SUM(ROUND(FLOAT(SCORE100)*WEIGHTING,0)) AS SCORE_WARIAI, ";//3:割合
        } else {
            $query .= "         ROUND(SUM(FLOAT(SCORE100)*WEIGHTING),0) AS SCORE_WARIAI, ";//3:割合
        }
        $query .= "         SUM(SCORE) AS SCORE_SUM ";                  //4:合算
        $query .= "     FROM ";
        $query .= "         T_REC_MOTO ";
        if ($gvalCalc == 1) {
            $query .= "     WHERE ";
            $query .= "         0 < CREDITS ";
        }
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        if ($model->Properties["kessiJogai_create_sakiScore"] == "1") {
            $query .= "     HAVING ";
            if ($model->z010name1 == "nichi-ni") {
                $query .= "         SUM(CASE WHEN VALUE_DI = '*' OR VALUE_DI = '**' THEN 1 ELSE 0 END) = 0 ";
            } else {
                $query .= "         SUM(CASE WHEN VALUE_DI = '*' THEN 1 ELSE 0 END) = 0 ";
            }
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.SCORE_AVG, ";
        $query .= "     L1.SCORE_CREDITS, ";
        $query .= "     L1.SCORE_MAX, ";
        $query .= "     L1.SCORE_WARIAI, ";
        $query .= "     L1.SCORE_SUM ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         T_SCHNO ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     ) T1 LEFT JOIN T_SCORE  L1 ON L1.SCHREGNO=T1.SCHREGNO ";

        return $query;
    }

    //更新
    function getUpdateRecordDat($seme, $kind, $item, $div, $subclasscdArray, $gvalCalc, $row, $model) {
        $data = array();
        if ($gvalCalc == 0) {
            $data["SCORE"][NUMBER]      = $row["SCORE_AVG"];
        }
        if ($gvalCalc == 1) {
            $data["SCORE"][NUMBER]      = $row["SCORE_CREDITS"];
        }
        if ($gvalCalc == 2) {
            $data["SCORE"][NUMBER]      = $row["SCORE_MAX"];
        }
        if ($gvalCalc == 3) {
            $data["SCORE"][NUMBER]      = $row["SCORE_WARIAI"];
        }
        if ($gvalCalc == 4) {
            $data["SCORE"][NUMBER]      = $row["SCORE_SUM"];
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "      YEAR = '".CTRL_YEAR."' ";
        $where .= " AND  SEMESTER = '{$seme}' ";
        $where .= " AND  TESTKINDCD = '{$kind}' ";
        $where .= " AND  TESTITEMCD = '{$item}' ";
        $where .= " AND  SCORE_DIV = '{$div}' ";
        $where .= " AND  CLASSCD = '{$subclasscdArray[0]}' ";
        $where .= " AND  SCHOOL_KIND = '{$subclasscdArray[1]}' ";
        $where .= " AND  CURRICULUM_CD = '{$subclasscdArray[2]}' ";
        $where .= " AND  SUBCLASSCD = '{$subclasscdArray[3]}' ";
        $where .= " AND  SCHREGNO = '".$row["SCHREGNO"]."' ";

        $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
        return $query;
    }

    //削除
    function getDeleteRecordDat($seme, $kind, $item, $div, $subclasscdArray, $row) {

        $query  = " DELETE FROM ";
        $query .= "      RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        $query .= " AND  SEMESTER = '{$seme}' ";
        $query .= " AND  TESTKINDCD = '{$kind}' ";
        $query .= " AND  TESTITEMCD = '{$item}' ";
        $query .= " AND  SCORE_DIV = '{$div}' ";
        $query .= " AND  CLASSCD = '{$subclasscdArray[0]}' ";
        $query .= " AND  SCHOOL_KIND = '{$subclasscdArray[1]}' ";
        $query .= " AND  CURRICULUM_CD = '{$subclasscdArray[2]}' ";
        $query .= " AND  SUBCLASSCD = '{$subclasscdArray[3]}' ";
        $query .= " AND  SCHREGNO = '".$row["SCHREGNO"]."' ";

        return $query;
    }

}
?>
