<?php

require_once('for_php7.php');

class knjd219aQuery extends Query {

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
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.COMBINED_CLASSCD, ";
            $query .= " T1.COMBINED_SCHOOL_KIND, ";
            $query .= " T1.COMBINED_CURRICULUM_CD, ";
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
            $query .= "        AND T2.CLASSCD=T1.COMBINED_CLASSCD ";
            $query .= "        AND T2.SCHOOL_KIND=T1.COMBINED_SCHOOL_KIND ";
            $query .= "        AND T2.CURRICULUM_CD=T1.COMBINED_CURRICULUM_CD ";
        }
        $query .= "      LEFT JOIN COMB_GCALC_DAT T3 ";
        $query .= "             ON T3.YEAR=T1.YEAR ";
        $query .= "            AND T3.COMBINED_SUBCLASSCD=T1.COMBINED_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        AND T3.COMBINED_CLASSCD=T1.COMBINED_CLASSCD ";
            $query .= "        AND T3.COMBINED_SCHOOL_KIND=T1.COMBINED_SCHOOL_KIND ";
            $query .= "        AND T3.COMBINED_CURRICULUM_CD=T1.COMBINED_CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.CALCULATE_CREDIT_FLG = '2' ";//単位加算
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.COMBINED_SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.COMBINED_SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.COMBINED_SUBCLASSCD ";
        return $query;
    }
    //履歴一覧：科目合併評定自動計算履歴データより取得。
    function getListRireki($year, $model) {
        $query  = " SELECT ";
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     T1.COMBINED_SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.COMBINED_CLASSCD, ";
            $query .= " T1.COMBINED_SCHOOL_KIND, ";
            $query .= " T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     T1.GVAL_CALC, ";
        $query .= "     L2.NAME1 as GVAL_NAME ";
        $query .= " FROM ";
        $query .= "     COMB_GCALC_EXEC_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            AND L1.CLASSCD = T1.COMBINED_CLASSCD ";
            $query .= "            AND L1.SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
            $query .= "            AND L1.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'Z017' AND L2.NAMECD2 = T1.GVAL_CALC ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR='{$year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.COMBINED_SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.COMBINED_SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     T1.COMBINED_SUBCLASSCD ";
        return $query;
    }
    //合併先科目の講座名簿の存在チェック
    function getExistsChair($year, $combSubclass, $combClass, $combSchoolkind, $combCurriculumcd, $model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) as CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = T2.YEAR AND ";
        $query .= "     T1.SEMESTER   = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD    = T2.CHAIRCD AND ";
        $query .= "     T1.YEAR       = '{$year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T2.CLASSCD = '{$combClass}' AND ";
            $query .= " T2.SCHOOL_KIND = '{$combSchoolkind}' AND ";
            $query .= " T2.CURRICULUM_CD = '{$combCurriculumcd}' AND ";
        }
        $query .= "     T2.SUBCLASSCD = '{$combSubclass}' ";
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
    //科目合併評定自動計算履歴データ・追加
    function getInsertRireki($year, $combSubclass, $gvalCalc, $calcDate, $calcTime, $combClass, $combSchoolkind, $combCurriculumcd, $model) {
        $data = array();
        $data["CALC_DATE"][TEXT]            = $calcDate;
        $data["CALC_TIME"][TEXT]            = $calcTime;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["COMBINED_CLASSCD"][TEXT]  = $combClass;
            $data["COMBINED_SCHOOL_KIND"][TEXT]  = $combSchoolkind;
            $data["COMBINED_CURRICULUM_CD"][TEXT]  = $combCurriculumcd;
        }
        $data["COMBINED_SUBCLASSCD"][TEXT]  = $combSubclass;
        $data["GVAL_CALC"][TEXT]            = $gvalCalc;
        $data["YEAR"][TEXT]                 = $year;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "COMB_GCALC_EXEC_DAT");
        return $query;
    }
    // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
    // 名称区分：D015　名称コード：01　名称予備１：「Y」もしくは「NULL」
    function getNameMstD015() {
        $year = CTRL_YEAR;
        $namecd1 = "D015";
        $namecd2 = "01";

        $query  = "";
        $query .= "SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAMESPARE1, "; // Y:, null:
        $query .= "    NAMESPARE2 "; // Y:, null:
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        $query .= "    NAMECD1 = '{$namecd1}' AND ";
        $query .= "    NAMECD2 = '{$namecd2}' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }
    //①成績データに存在しない生徒を追加
    function getInsertNonStdRec($year, $combSubclass, $testCd, $combClass, $combSchoolkind, $combCurriculumcd, $model) {
        $staffcd = STAFFCD;
        $sem    = substr($testCd, 0, 1);
        $kind   = substr($testCd, 1, 2);
        $item   = substr($testCd, 3, 2);
        $div    = substr($testCd, 5, 2);

        $query  = " INSERT INTO RECORD_SCORE_DAT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO,REGISTERCD,UPDATED) ";
        } else {
            $query .= " (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,SUBCLASSCD,SCHREGNO,REGISTERCD,UPDATED) ";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     '{$sem}' as SEMESTER, ";
        $query .= "     '{$kind}' as TESTKINDCD, ";
        $query .= "     '{$item}' as TESTITEMCD, ";
        $query .= "     '{$div}' as SCORE_DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T2.CLASSCD, ";
            $query .= " T2.SCHOOL_KIND, ";
            $query .= " T2.CURRICULUM_CD, ";
        }
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
        $query .= "     T1.YEAR       = '{$year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T2.CLASSCD = '{$combClass}' AND ";
            $query .= " T2.SCHOOL_KIND = '{$combSchoolkind}' AND ";
            $query .= " T2.CURRICULUM_CD = '{$combCurriculumcd}' AND ";
        }
        $query .= "     T2.SUBCLASSCD = '{$combSubclass}' AND ";
        $query .= "     T1.SCHREGNO NOT IN (SELECT ";
        $query .= "                             R1.SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             RECORD_SCORE_DAT R1 ";
        $query .= "                         WHERE ";
        $query .= "                             R1.YEAR = '{$year}' AND ";
        $query .= "                             R1.SEMESTER || R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV = '{$testCd}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         R1.CLASSCD = '{$combClass}' AND ";
            $query .= "                         R1.SCHOOL_KIND = '{$combSchoolkind}' AND ";
            $query .= "                         R1.CURRICULUM_CD = '{$combCurriculumcd}' AND ";
        }
        $query .= "                             R1.SUBCLASSCD = '{$combSubclass}') ";
        return $query;
    }
    function getNameMstD008() {
        $year = CTRL_YEAR;
        $namecd1 = "D008";

        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS CLASSCD ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        return $query;
    }
    function getClassDetailDat() {
        $query  = " SELECT ";
        $query .= "     CLASSCD ";
        $query .= " FROM ";
        $query .= "     CLASS_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     CLASS_SEQ = '003' ";
        return $query;
    }

    //科目毎・生徒毎に合併先科目の評定および履修・修得単位を自動計算し、成績データに登録する。
    function getUpdateList($repGetCredit, $repValue, $year, $combSubclass, $gvalCalc, $testCd, $combClass, $combSchoolkind, $combCurriculumcd, $model, $gradValueFieldName) {
        //②－Ａ）合併先科目のレコードが存在する生徒
        $query  = " WITH T_SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         R1.YEAR, ";
        $query .= "         R1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     S1.ATTEND_CLASSCD, ";
            $query .= "     S1.ATTEND_SCHOOL_KIND, ";
            $query .= "     S1.ATTEND_CURRICULUM_CD, ";
        }
        $query .= "         S1.ATTEND_SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT R1, ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT S1 ";
        $query .= "     WHERE ";
        $query .= "         R1.YEAR = '{$year}' AND  ";
        $query .= "         R1.SEMESTER || R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV = '{$testCd}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     R1.CLASSCD = '{$combClass}' AND ";
            $query .= "     R1.SCHOOL_KIND = '{$combSchoolkind}' AND ";
            $query .= "     R1.CURRICULUM_CD = '{$combCurriculumcd}' AND ";
        }
        $query .= "         R1.SUBCLASSCD = '{$combSubclass}' AND ";
        $query .= "         S1.YEAR = R1.YEAR AND  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     S1.COMBINED_CLASSCD = R1.CLASSCD AND ";
            $query .= "     S1.COMBINED_SCHOOL_KIND = R1.SCHOOL_KIND AND ";
            $query .= "     S1.COMBINED_CURRICULUM_CD = R1.CURRICULUM_CD AND ";
        }
        $query .= "         S1.COMBINED_SUBCLASSCD = R1.SUBCLASSCD ";
        $query .= "     ) ";
        //②－Ｂ）対応する合併元科目のレコード
        $query .= " , T_REC_MOTO AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     R1.CLASSCD, ";
            $query .= "     R1.SCHOOL_KIND, ";
            $query .= "     R1.CURRICULUM_CD, ";
        }
        $query .= "         R1.SUBCLASSCD, ";
        $query .= "         R1.SCHREGNO, ";
        $query .= "         R1.{$gradValueFieldName} as VALUE, ";
        $query .= "         R1.GET_CREDIT, ";
        $query .= "         R1.COMP_CREDIT, ";
        //「重み付け」計算の際に参照する単位数。
        if ($repGetCredit == "Y") {
            //「評定＝１」のレコードを計算に含む場合、
            //  ・「評定＝１」　のレコードは「履修単位」を参照。
            //  ・「評定２以上」のレコードは「修得単位」を参照。
            $query .= "     CASE WHEN 1 = R1.{$gradValueFieldName} THEN R1.COMP_CREDIT ";
            $query .= "          WHEN 1 < R1.{$gradValueFieldName} THEN R1.GET_CREDIT END as CREDIT ";
        } else {
            //「評定＝１」のレコードを計算に含まない場合、
            //  ・全てのレコードは「修得単位」を参照。
            $query .= "     R1.GET_CREDIT as CREDIT ";
        }
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT R1, ";
        $query .= "         T_SCHNO S1 ";
        $query .= "     WHERE ";
        $query .= "         R1.YEAR = S1.YEAR AND  ";
        $query .= "         R1.SEMESTER || R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV = '{$testCd}' AND ";
        $query .= "         R1.SCHREGNO = S1.SCHREGNO AND  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     R1.CLASSCD = S1.ATTEND_CLASSCD AND ";
            $query .= "     R1.SCHOOL_KIND = S1.ATTEND_SCHOOL_KIND AND ";
            $query .= "     R1.CURRICULUM_CD = S1.ATTEND_CURRICULUM_CD AND ";
        }
        $query .= "         R1.SUBCLASSCD = S1.ATTEND_SUBCLASSCD ";
        $query .= "     ) ";
        //③合併先科目の評定を自動計算し、成績データに登録する。
        $query .= " , T_VALUE AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($gvalCalc == 0) {
            //「0:平均」・・・分母は「評定がNULL／ゼロ以外」の合併元科目のレコード数。
            $query .= "     ROUND(AVG(FLOAT(VALUE)),0) AS VALUE ";
        }
        if ($gvalCalc == 1) {
            //「1:単位による重み付け」・・・分母は「評定がNULL／ゼロ以外」の合併元科目のレコードの単位数の合計。
            $query .= "     ROUND(FLOAT(SUM(VALUE*CREDIT))/SUM(CREDIT),0) AS VALUE ";
        }
        if ($gvalCalc == 2) {
            //「2:最大値」・・・合併元科目の評定の最大値。
            $query .= "     MAX(VALUE) AS VALUE ";
        }
        $query .= "     FROM ";
        $query .= "         T_REC_MOTO ";
        $query .= "     WHERE ";
        //「評定＝１」のレコードを計算に含むかどうかを判断。
        if ($repValue == "Y") {
            $query .= "     0 < VALUE "; //「評定＝１」のレコードを計算に含む。
        } else {
            $query .= "     1 < VALUE "; //「評定＝１」のレコードを計算に含まない。
        }
        //分母をゼロ以上にするため。ＤＢエラーを防ぐため。
        if ($gvalCalc == 1) {
            $query .= "     AND 0 < CREDIT ";
        }
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";
        //③合併先科目の単位を自動計算し、成績データに登録する。
        $query .= " , T_CREDIT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(COMP_CREDIT) AS COMP_CREDIT, ";
        $query .= "         SUM(CREDIT) AS GET_CREDIT ";
        $query .= "     FROM ";
        $query .= "         T_REC_MOTO ";
        $query .= "     WHERE ";
        $query .= "         0 < VALUE "; //評定＝ゼロもしくはNULLのレコードを計算に含まない。
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.VALUE, ";
        $query .= "     L2.COMP_CREDIT, ";
        $query .= "     L2.GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         T_SCHNO ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     ) T1 LEFT JOIN T_VALUE  L1 ON L1.SCHREGNO=T1.SCHREGNO ";
        $query .= "          LEFT JOIN T_CREDIT L2 ON L2.SCHREGNO=T1.SCHREGNO ";
        return $query;
    }
    //科目毎・生徒毎に合併先科目の単位を自動計算し、成績データに登録する。
    function getCreditUpdateList($year, $combSubclass, $testCd, $combClass, $combSchoolkind, $combCurriculumcd, $model) {
        //②－Ａ）合併先科目のレコードが存在する生徒
        $query  = " WITH T_SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         R1.YEAR, ";
        $query .= "         R1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     S1.ATTEND_CLASSCD, ";
            $query .= "     S1.ATTEND_SCHOOL_KIND, ";
            $query .= "     S1.ATTEND_CURRICULUM_CD, ";
        }
        $query .= "         S1.ATTEND_SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT R1, ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT S1 ";
        $query .= "     WHERE ";
        $query .= "         R1.YEAR = '{$year}' AND  ";
        $query .= "         R1.SEMESTER || R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV = '{$testCd}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     R1.CLASSCD = '{$combClass}' AND ";
            $query .= "     R1.SCHOOL_KIND = '{$combSchoolkind}' AND ";
            $query .= "     R1.CURRICULUM_CD = '{$combCurriculumcd}' AND ";
        }
        $query .= "         R1.SUBCLASSCD = '{$combSubclass}' AND ";
        $query .= "         S1.YEAR = R1.YEAR AND  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     S1.COMBINED_CLASSCD = R1.CLASSCD AND ";
            $query .= "     S1.COMBINED_SCHOOL_KIND = R1.SCHOOL_KIND AND ";
            $query .= "     S1.COMBINED_CURRICULUM_CD = R1.CURRICULUM_CD AND ";
        }
        $query .= "         S1.COMBINED_SUBCLASSCD = R1.SUBCLASSCD ";
        $query .= "     ) ";
        //②－Ｂ）対応する合併元科目のレコード
        $query .= " , T_REC_MOTO AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     R1.CLASSCD, ";
            $query .= "     R1.SCHOOL_KIND, ";
            $query .= "     R1.CURRICULUM_CD, ";
        }
        $query .= "         R1.SUBCLASSCD, ";
        $query .= "         R1.SCHREGNO, ";
        $query .= "         R1.GET_CREDIT, ";
        $query .= "         R1.COMP_CREDIT ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT R1, ";
        $query .= "         T_SCHNO S1 ";
        $query .= "     WHERE ";
        $query .= "         R1.YEAR = S1.YEAR AND  ";
        $query .= "         R1.SEMESTER || R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV = '{$testCd}' AND ";
        $query .= "         R1.SCHREGNO = S1.SCHREGNO AND  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     R1.CLASSCD = S1.ATTEND_CLASSCD AND ";
            $query .= "     R1.SCHOOL_KIND = S1.ATTEND_SCHOOL_KIND AND ";
            $query .= "     R1.CURRICULUM_CD = S1.ATTEND_CURRICULUM_CD AND ";
        }
        $query .= "         R1.SUBCLASSCD = S1.ATTEND_SUBCLASSCD ";
        $query .= "     ) ";
        //③合併先科目の単位を自動計算し、成績データに登録する。
        $query .= " , T_CREDIT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(COMP_CREDIT) AS COMP_CREDIT, ";
        $query .= "         SUM(GET_CREDIT) AS GET_CREDIT ";
        $query .= "     FROM ";
        $query .= "         T_REC_MOTO ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L2.COMP_CREDIT, ";
        $query .= "     L2.GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         T_SCHNO ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     ) T1 LEFT JOIN T_CREDIT L2 ON L2.SCHREGNO=T1.SCHREGNO ";
        return $query;
    }
    //更新
    function getUpdateRecordDat($year, $combSubclass, $schno, $gradValue, $compCredit, $getCredit, $testCd, $combClass, $combSchoolkind, $combCurriculumcd, $model, $gradValueFieldName) {
        $data = array();

        $data[$gradValueFieldName][NUMBER]          = $gradValue;
        $data["COMP_CREDIT"][NUMBER]    = $compCredit;
        $data["GET_CREDIT"][NUMBER]     = $getCredit;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "      YEAR = '{$year}' ";
        $where .= " AND  SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testCd}' ";
        $where .= " AND  SUBCLASSCD = '{$combSubclass}' ";
        $where .= " AND  SCHREGNO = '{$schno}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= " AND  CLASSCD = '{$combClass}' ";
            $where .= " AND  SCHOOL_KIND = '{$combSchoolkind}' ";
            $where .= " AND  CURRICULUM_CD = '{$combCurriculumcd}' ";
        }

        $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
        return $query;
    }
}
?>
