<?php

require_once('for_php7.php');


class knjm837wQuery extends Query
{

    //年度取得
    public function getYear()
    {
        $query  = "  SELECT DISTINCT ";
        $query .= "      YEAR || '年度' AS LABEL, ";
        $query .= "      YEAR AS VALUE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_HDAT ";
        $query .= "  ORDER BY  ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //学期マスタ
    public function getSemeMst($year, $sem = "")
    {
        $query  = "  SELECT DISTINCT";
        $query .= "      SEMESTERNAME LABEL, ";
        $query .= "      SEMESTER AS VALUE    ";
        $query .= "  FROM ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '{$year}' ";
        if (strlen($sem)) {
            $query .= "  AND SEMESTER = '{$sem}' ";
        } else {
            $query .= "  AND SEMESTER <> '9' ";
        }

        return $query;
    }

    //テスト種別
    public function getTestcd($model, $year, $semester, $testcd = "")
    {
        $query  = " SELECT ";
        $query .= "     T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS VALUE ";
        $query .= "   , T2.TESTITEMNAME AS LABEL ";
        $query .= " FROM NAME_MST T1 ";
        $query .= " INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$semester."' ";
        $query .= "     AND T2.SEMESTER || '-' || T2.TESTKINDCD || '-' || T2.TESTITEMCD || '-' || T2.SCORE_DIV = T1.NAMESPARE1 ";
        if ($testcd != '') {
            $query .= "     AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '".$testcd."' ";
        }
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'M002' ";
        if ($model->Properties["knjm837wPrintGakkiSeiseki"] == "1") {
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     '990008' AS VALUE ";
            $query .= "   , '学期成績' AS LABEL ";
            $query .= " FROM SYSIBM.SYSDUMMY1 T1 ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //3.成績上位者（４科目以上成績入力された生徒で、平均点８０点以上の生徒）
    //4.欠点保有者（３９点以下の科目が１科目以上ある生徒）
    public function getCsvQuery34($model, $kettenFlg, $entYear, $eYear)
    {
        $izen = ($entYear == $eYear) ? "<=" : "=";
        $ctrl_year = CTRL_YEAR;
        $query  = "";
        //履修科目
        $query .= " WITH SUBCLASS_STD_SELECT AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         TSL.YEAR, ";
        $query .= "         TSL.SEMESTER, ";
        $query .= "         TSL.CLASSCD, ";
        $query .= "         TSL.SCHOOL_KIND, ";
        $query .= "         TSL.CURRICULUM_CD, ";
        $query .= "         TSL.SUBCLASSCD, ";
        $query .= "         TSL.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_STD_SELECT_DAT TSL ";
        $query .= "     WHERE ";
        $query .= "         TSL.YEAR = '{$ctrl_year}' ";
        $query .= "         AND TSL.SEMESTER = '{$model->field["GAKKI"]}' ";
        $query .= " ) ";
        //成績入力された成績
        $query .= " , RECORD AS ( ";
        $query .= "     SELECT ";
        $query .= "         TRC.CLASSCD, ";
        $query .= "         TRC.SCHOOL_KIND, ";
        $query .= "         TRC.CURRICULUM_CD, ";
        $query .= "         TRC.SUBCLASSCD, ";
        $query .= "         TRC.SCHREGNO, ";
        if ("990008" == $model->field["TESTCD"]) {
            $query .= "         TRC.VALUE AS SCORE, ";
            $query .= "         HOJU.VALUE AS SIDOU ";
            $query .= "     FROM ";
            $query .= "         SUBCLASS_STD_SELECT TSL ";
            $query .= "         INNER JOIN RECORD_SCORE_HIST_DAT TRC ON TRC.YEAR = TSL.YEAR ";
            $query .= "             AND TRC.SEMESTER = '".$model->field["GAKKI"]."' ";
            $query .= "             AND TRC.TESTKINDCD || TRC.TESTITEMCD || TRC.SCORE_DIV = '".$model->field["TESTCD"]."' ";
            $query .= "             AND TRC.CLASSCD = TSL.CLASSCD ";
            $query .= "             AND TRC.SCHOOL_KIND = TSL.SCHOOL_KIND ";
            $query .= "             AND TRC.CURRICULUM_CD = TSL.CURRICULUM_CD ";
            $query .= "             AND TRC.SUBCLASSCD = TSL.SUBCLASSCD ";
            $query .= "             AND TRC.SCHREGNO = TSL.SCHREGNO ";
            $query .= "             AND TRC.SEQ = 1 ";
            $query .= "         LEFT JOIN V_RECORD_SCORE_HIST_DAT HOJU ON HOJU.YEAR = TSL.YEAR ";
            $query .= "             AND HOJU.SEMESTER = TRC.SEMESTER ";
            $query .= "             AND HOJU.TESTKINDCD = TRC.TESTKINDCD AND HOJU.TESTITEMCD = TRC.TESTITEMCD AND HOJU.SCORE_DIV = TRC.SCORE_DIV ";
            $query .= "             AND HOJU.CLASSCD = TSL.CLASSCD ";
            $query .= "             AND HOJU.SCHOOL_KIND = TSL.SCHOOL_KIND ";
            $query .= "             AND HOJU.CURRICULUM_CD = TSL.CURRICULUM_CD ";
            $query .= "             AND HOJU.SUBCLASSCD = TSL.SUBCLASSCD ";
            $query .= "             AND HOJU.SCHREGNO = TSL.SCHREGNO ";
            $query .= "     WHERE ";
            $query .= "         TRC.VALUE IS NOT NULL ";
        } else {
            $query .= "         TRC.SCORE AS SCORE, ";
            $query .= "         TRC.VALUE AS SIDOU ";
            $query .= "     FROM ";
            $query .= "         SUBCLASS_STD_SELECT TSL ";
            $query .= "         INNER JOIN V_RECORD_SCORE_HIST_DAT TRC ON TRC.YEAR = TSL.YEAR ";
            $query .= "             AND TRC.SEMESTER = '".$model->field["GAKKI"]."' ";
            $query .= "             AND TRC.TESTKINDCD || TRC.TESTITEMCD || TRC.SCORE_DIV = '".$model->field["TESTCD"]."' ";
            $query .= "             AND TRC.CLASSCD = TSL.CLASSCD ";
            $query .= "             AND TRC.SCHOOL_KIND = TSL.SCHOOL_KIND ";
            $query .= "             AND TRC.CURRICULUM_CD = TSL.CURRICULUM_CD ";
            $query .= "             AND TRC.SUBCLASSCD = TSL.SUBCLASSCD ";
            $query .= "             AND TRC.SCHREGNO = TSL.SCHREGNO ";
            $query .= "     WHERE ";
            $query .= "         TRC.SCORE IS NOT NULL ";
        }
        $query .= " ) ";
        //成績入力された成績の平均点・科目数
        $query .= " , T_AVG_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         TRC.SCHREGNO, ";
        $query .= "         DECIMAL(ROUND(AVG(FLOAT(TRC.SCORE))*10,0)/10,5,1) AS AVG, ";
        $query .= "         COUNT(TRC.SCORE) AS CNT ";
        $query .= "     FROM ";
        $query .= "         RECORD TRC ";
        $query .= "     GROUP BY ";
        $query .= "         TRC.SCHREGNO ";
        $query .= " ) ";

        //メイン
        if ($kettenFlg == "ketten") {
            $query .= " SELECT ";
            $query .= "     TAC.SCHREGNO, ";
            $query .= "     BASE.NAME_SHOW AS NAME, ";
            $query .= "     TAC.AVG, ";
            $query .= "     TAC.CNT, ";
            $query .= "     '' AS REMARK, ";
            $query .= "     TRC.CLASSCD, ";
            $query .= "     TRC.SCHOOL_KIND, ";
            $query .= "     TRC.CURRICULUM_CD, ";
            $query .= "     TRC.SUBCLASSCD, ";
            $query .= "     TSB.SUBCLASSABBV, ";
            $query .= "     TRC.SCORE, ";
            $query .= "     TRC.SIDOU ";
            $query .= " FROM ";
            $query .= "     T_AVG_CNT TAC ";
            $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = TAC.SCHREGNO ";
            $query .= "     INNER JOIN RECORD TRC ON TRC.SCHREGNO = TAC.SCHREGNO ";
            $query .= "     INNER JOIN SUBCLASS_MST TSB ";
            $query .= "          ON TSB.CLASSCD = TRC.CLASSCD ";
            $query .= "         AND TSB.SCHOOL_KIND = TRC.SCHOOL_KIND ";
            $query .= "         AND TSB.CURRICULUM_CD = TRC.CURRICULUM_CD ";
            $query .= "         AND TSB.SUBCLASSCD = TRC.SUBCLASSCD ";
            $query .= " WHERE ";
            $query .= "     VALUE(BASE.INOUTCD, '') NOT IN ('8', '9') ";
            $query .= "     AND FISCALYEAR(BASE.ENT_DATE) {$izen} '{$entYear}' ";
            $query .= "     AND TRC.SCORE <= 39 ";
            $query .= " ORDER BY ";
            $query .= "     SUBSTR(TAC.SCHREGNO, 1, 4) DESC, ";
            $query .= "     SUBSTR(TAC.SCHREGNO, 5, 4) ASC, ";
            $query .= "     TRC.CLASSCD, ";
            $query .= "     TRC.SCHOOL_KIND, ";
            $query .= "     TRC.CURRICULUM_CD, ";
            $query .= "     TRC.SUBCLASSCD ";
        } else {
            $query .= " SELECT ";
            $query .= "     TAC.SCHREGNO, ";
            $query .= "     BASE.NAME_SHOW AS NAME, ";
            $query .= "     TAC.AVG, ";
            $query .= "     TAC.CNT, ";
            $query .= "     '' AS REMARK ";
            $query .= " FROM ";
            $query .= "     T_AVG_CNT TAC ";
            $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = TAC.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     VALUE(BASE.INOUTCD, '') NOT IN ('8', '9') ";
            $query .= "     AND FISCALYEAR(BASE.ENT_DATE) {$izen} '{$entYear}' ";
            $query .= "     AND  4 <= TAC.CNT ";
            $query .= "     AND 80 <= TAC.AVG ";
            $query .= " ORDER BY ";
            $query .= "     SUBSTR(TAC.SCHREGNO, 1, 4) DESC, ";
            $query .= "     SUBSTR(TAC.SCHREGNO, 5, 4) ASC ";
        }
        return $query;
    }

    //I.受験状況
    public function getCsvQueryI($model, $entYear, $eYear)
    {
        $izen = ($entYear == $eYear) ? "<=" : "=";
        $ctrl_year = CTRL_YEAR;
        $ctrl_date = str_replace("/", "-", CTRL_DATE);
        $query  = "";
        //SQL
        $query .= " WITH T_SCHOOL AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         ENTRANCE_DATE ";
        $query .= "     FROM ";
        $query .= "         SCHOOL_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR {$izen} '{$entYear}' ";
        $query .= " ) ";
        $query .= " , T_BASE AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         INOUTCD, ";
        $query .= "         ENT_DATE, ";
        $query .= "         VALUE(ENT_DIV,'1') AS ENT_DIV, ";
        $query .= "         GRD_DATE, ";
        $query .= "         GRD_DIV, ";
        $query .= "         FISCALYEAR(ENT_DATE) AS ENT_YEAR, ";
        $query .= "         FISCALYEAR(GRD_DATE) AS GRD_YEAR ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST ";
        $query .= "     WHERE ";
        $query .= "         VALUE(INOUTCD, '') NOT IN ('8', '9') ";
        $query .= "         AND FISCALYEAR(ENT_DATE) {$izen} '{$entYear}' ";
        $query .= " ) ";
        //履修科目
        $query .= " , SUBCLASS_STD_SELECT AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         TSL.YEAR, ";
        $query .= "         TSL.SEMESTER, ";
        $query .= "         TSL.CLASSCD, ";
        $query .= "         TSL.SCHOOL_KIND, ";
        $query .= "         TSL.CURRICULUM_CD, ";
        $query .= "         TSL.SUBCLASSCD, ";
        $query .= "         TSL.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_STD_SELECT_DAT TSL ";
        $query .= "     WHERE ";
        $query .= "         TSL.YEAR = '{$ctrl_year}' ";
        $query .= "         AND TSL.SEMESTER = '{$model->field["GAKKI"]}' ";
        $query .= " ) ";
        //本年度１科目でも履修登録された生徒
        $query .= " , STD_SELECT AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         TSL.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_STD_SELECT TSL ";
        $query .= " ) ";
        //本年度１科目でも成績入力された生徒
        $query .= " , RECORD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         TSL.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_STD_SELECT TSL ";
        $query .= "         INNER JOIN V_RECORD_SCORE_HIST_DAT TRC ON TRC.YEAR = TSL.YEAR ";
        $query .= "             AND TRC.SEMESTER = '".$model->field["GAKKI"]."' ";
        $query .= "             AND TRC.TESTKINDCD || TRC.TESTITEMCD || TRC.SCORE_DIV = '".$model->field["TESTCD"]."' ";
        $query .= "             AND TRC.CLASSCD = TSL.CLASSCD ";
        $query .= "             AND TRC.SCHOOL_KIND = TSL.SCHOOL_KIND ";
        $query .= "             AND TRC.CURRICULUM_CD = TSL.CURRICULUM_CD ";
        $query .= "             AND TRC.SUBCLASSCD = TSL.SUBCLASSCD ";
        $query .= "             AND TRC.SCHREGNO = TSL.SCHREGNO ";
        $query .= "     WHERE ";
        if ("990008" == $model->field["TESTCD"]) {
            $query .= "         TRC.VALUE IS NOT NULL ";
        } else {
            $query .= "         TRC.SCORE IS NOT NULL ";
        }
        $query .= " ) ";

        //一般生
        $query .= " SELECT ";
        $query .= "     '02' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '04' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '06' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     (( ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3') ";
        $query .= "     ) OR ( ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3') ";
        $query .= "     )) ";
        $query .= "     AND BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND BASE.GRD_YEAR < '{$ctrl_year}' ";
        $query .= "     AND BASE.GRD_DIV IS NOT NULL ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '13' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     BASE.ENT_YEAR = '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND DATE('{$ctrl_date}') >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '16' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     (( ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3') ";
        $query .= "     ) OR ( ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3') ";
        $query .= "     )) ";
        $query .= "     AND BASE.GRD_YEAR = '{$ctrl_year}' ";
        $query .= "     AND DATE('{$ctrl_date}') > BASE.GRD_DATE ";
        $query .= "     AND BASE.GRD_DIV IS NOT NULL ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '19' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= "     INNER JOIN STD_SELECT TSL ON TSL.SCHREGNO = BASE.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     DATE('{$ctrl_date}') >= BASE.ENT_DATE ";
        $query .= "     AND (DATE('{$ctrl_date}') <  BASE.GRD_DATE OR BASE.GRD_DATE IS NULL) ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '22' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= "     INNER JOIN RECORD TRC ON TRC.SCHREGNO = BASE.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     DATE('{$ctrl_date}') >= BASE.ENT_DATE ";
        $query .= "     AND (DATE('{$ctrl_date}') <  BASE.GRD_DATE OR BASE.GRD_DATE IS NULL) ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3') ";
        //転編入生
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '03' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('4','5') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '05' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('4','5') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '07' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     (( ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('4','5') ";
        $query .= "     ) OR ( ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('4','5') ";
        $query .= "     )) ";
        $query .= "     AND BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND BASE.GRD_YEAR < '{$ctrl_year}' ";
        $query .= "     AND BASE.GRD_DIV IS NOT NULL ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '14' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     BASE.ENT_YEAR = '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND DATE('{$ctrl_date}') >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('4','5') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '17' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     (( ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('4','5') ";
        $query .= "     ) OR ( ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('4','5') ";
        $query .= "     )) ";
        $query .= "     AND BASE.GRD_YEAR = '{$ctrl_year}' ";
        $query .= "     AND DATE('{$ctrl_date}') > BASE.GRD_DATE ";
        $query .= "     AND BASE.GRD_DIV IS NOT NULL ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '20' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= "     INNER JOIN STD_SELECT TSL ON TSL.SCHREGNO = BASE.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     DATE('{$ctrl_date}') >= BASE.ENT_DATE ";
        $query .= "     AND (DATE('{$ctrl_date}') <  BASE.GRD_DATE OR BASE.GRD_DATE IS NULL) ";
        $query .= "     AND BASE.ENT_DIV in ('4','5') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '23' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= "     INNER JOIN RECORD TRC ON TRC.SCHREGNO = BASE.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     DATE('{$ctrl_date}') >= BASE.ENT_DATE ";
        $query .= "     AND (DATE('{$ctrl_date}') <  BASE.GRD_DATE OR BASE.GRD_DATE IS NULL) ";
        $query .= "     AND BASE.ENT_DIV in ('4','5') ";
        //備考
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '08-4' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('4') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '08-5' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('5') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '09-3' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     (( ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     ) OR ( ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     )) ";
        $query .= "     AND BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND BASE.GRD_YEAR < '{$ctrl_year}' ";
        $query .= "     AND BASE.GRD_DIV in ('3') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '09-2' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     (( ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     ) OR ( ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     )) ";
        $query .= "     AND BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND BASE.GRD_YEAR < '{$ctrl_year}' ";
        $query .= "     AND BASE.GRD_DIV in ('2') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '09-9' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     (( ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     ) OR ( ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     )) ";
        $query .= "     AND BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND BASE.GRD_YEAR < '{$ctrl_year}' ";
        $query .= "     AND BASE.GRD_DIV in ('9') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '09-1' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     (( ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     ) OR ( ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     )) ";
        $query .= "     AND BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND BASE.GRD_YEAR < '{$ctrl_year}' ";
        $query .= "     AND BASE.GRD_DIV in ('1') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '15-4' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     BASE.ENT_YEAR = '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND DATE('{$ctrl_date}') >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('4') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '15-5' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     BASE.ENT_YEAR = '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND DATE('{$ctrl_date}') >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('5') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '18-3' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     (( ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     ) OR ( ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     )) ";
        $query .= "     AND BASE.GRD_YEAR = '{$ctrl_year}' ";
        $query .= "     AND DATE('{$ctrl_date}') > BASE.GRD_DATE ";
        $query .= "     AND BASE.GRD_DIV in ('3') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '18-2' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     (( ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     ) OR ( ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     )) ";
        $query .= "     AND BASE.GRD_YEAR = '{$ctrl_year}' ";
        $query .= "     AND DATE('{$ctrl_date}') > BASE.GRD_DATE ";
        $query .= "     AND BASE.GRD_DIV in ('2') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '18-9' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     (( ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     ) OR ( ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     )) ";
        $query .= "     AND BASE.GRD_YEAR = '{$ctrl_year}' ";
        $query .= "     AND DATE('{$ctrl_date}') > BASE.GRD_DATE ";
        $query .= "     AND BASE.GRD_DIV in ('9') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '18-1' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= " WHERE ";
        $query .= "     (( ";
        $query .= "     SCL.ENTRANCE_DATE >= BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     ) OR ( ";
        $query .= "     BASE.ENT_YEAR < '{$ctrl_year}' ";
        $query .= "     AND SCL.ENTRANCE_DATE < BASE.ENT_DATE ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= "     )) ";
        $query .= "     AND BASE.GRD_YEAR = '{$ctrl_year}' ";
        $query .= "     AND DATE('{$ctrl_date}') > BASE.GRD_DATE ";
        $query .= "     AND BASE.GRD_DIV in ('1') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '21-1' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= "     INNER JOIN STD_SELECT TSL ON TSL.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_BASE_YEAR_DETAIL_MST BSYR ";
        $query .= "         ON  BSYR.SCHREGNO = BASE.SCHREGNO ";
        $query .= "         AND BSYR.YEAR = '{$ctrl_year}' ";
        $query .= "         AND BSYR.BASE_SEQ = '001' ";
        $query .= "         AND BSYR.BASE_REMARK1 = '1' ";
        $query .= " WHERE ";
        $query .= "     DATE('{$ctrl_date}') >= BASE.ENT_DATE ";
        $query .= "     AND (DATE('{$ctrl_date}') <  BASE.GRD_DATE OR BASE.GRD_DATE IS NULL) ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '28-1' AS CNT_DIV, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     T_SCHOOL SCL ";
        $query .= "     INNER JOIN T_BASE BASE ON BASE.ENT_YEAR = SCL.YEAR ";
        $query .= "     INNER JOIN RECORD TRC ON TRC.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_BASE_YEAR_DETAIL_MST BSYR ";
        $query .= "         ON  BSYR.SCHREGNO = BASE.SCHREGNO ";
        $query .= "         AND BSYR.YEAR = '{$ctrl_year}' ";
        $query .= "         AND BSYR.BASE_SEQ = '001' ";
        $query .= "         AND BSYR.BASE_REMARK1 = '1' ";
        $query .= " WHERE ";
        $query .= "     DATE('{$ctrl_date}') >= BASE.ENT_DATE ";
        $query .= "     AND (DATE('{$ctrl_date}') <  BASE.GRD_DATE OR BASE.GRD_DATE IS NULL) ";
        $query .= "     AND BASE.ENT_DIV in ('1','2','3','4','5') ";
        $query .= " ORDER BY ";
        $query .= "     CNT_DIV ";
        return $query;
    }
}
