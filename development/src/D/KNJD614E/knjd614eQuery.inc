<?php
class knjd614eQuery extends Query {

    //校種
    function getSchoolKind($model) {
        $query .= " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ABBV2 ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GDAT.GRADE AS VALUE, ";
        $query .= "     GDAT.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT GDAT ";
        $query .= " WHERE ";
        $query .= "     GDAT.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND GDAT.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期詳細マスタ(日付の取得)
    function getSemesterDetailMst($semesterDetail) {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTER_DETAIL, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     VALUE(SDATE, '9999-12-31') AS SDATE, ";
        $query .= "     VALUE(EDATE, '9999-12-31') AS EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER_DETAIL = '{$semesterDetail}' ";

        return $query;
    }

    function getGradeInfo($model, $elmName) {
        $query  = " SELECT ";
        $query .= "   ".$elmName." ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND GRADE = '".$model->grade."' ";
        return $query;
    }

    function getHrClsDat($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.HR_NAME, ";
        $query .= "   T1.HR_NAMEABBV, ";
        $query .= "   T1.GRADE_NAME, ";
        $query .= "   T1.HR_CLASS_NAME1, ";
        $query .= "   T1.HR_CLASS_NAME2, ";
        $query .= "   T1.HR_FACCD, ";
        $query .= "   T1.TR_CD1, ";
        $query .= "   T1.TR_CD2, ";
        $query .= "   T1.TR_CD3, ";
        $query .= "   T1.SUBTR_CD1, ";
        $query .= "   T1.SUBTR_CD2, ";
        $query .= "   T1.SUBTR_CD3, ";
        $query .= "   T1.CLASSWEEKS, ";
        $query .= "   T1.CLASSDAYS ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.GRADE = '".$model->grade."' ";
        $query .= "   AND T1.SEMESTER = (SELECT MAX(TH.SEMESTER) FROM SCHREG_REGD_HDAT TH WHERE TH.YEAR = T1.YEAR AND TH.GRADE = T1.GRADE AND TH.HR_CLASS = T1.HR_CLASS) ";  //通年が想定されるので、全クラスの最終学期で登録されている名称を引っ張る
        return $query;
    }

    //授業日数を取得
    function getJyugyouNissuu($model) {
        $sDateYM = substr(str_replace('/', '', $model->field["SDATE"]), 0, 6);
        $eDateYM = substr(str_replace('/', '', $model->field["EDATE"]), 0, 6);
        $query  = " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     SUM(LESSON) AS LESSON ";
        $query .= " FROM ";
        $query .= "   ATTEND_LESSON_MST ";
        $query .= " WHERE ";
        $query .= "   '".$sDateYM."' <= YEAR || MONTH AND YEAR || MONTH <= '".$eDateYM."' ";
        $query .= "   AND COURSECD = '0' ";
        $query .= "   AND MAJORCD = '000' ";
        $query .= "   AND GRADE = '".$model->grade."' ";
        $query .= " GROUP BY ";
        $query .= "   GRADE ";
        $query .= " ORDER BY ";
        $query .= "   GRADE ";
        return $query;
    }

    function getCourseCodeMst() {
        return " SELECT * FROM COURSECODE_MST ";
    }

    //在籍者数/休学者数/復学者数/留学者数を取得
    function getSchCntInfo($model, $sum_param="") {
        $sDate = str_replace('/', '-', $model->field["SDATE"]);
        $eDate = str_replace('/', '-', $model->field["EDATE"]);

        $query  = " WITH SCHNO_A AS( ";  // 在籍者数
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE  ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_HDAT TH ";
        $query .= "     ON TH.YEAR = T1.YEAR ";
        $query .= "    AND TH.SEMESTER = T1.SEMESTER ";
        $query .= "    AND TH.GRADE = T1.GRADE ";
        $query .= "    AND TH.HR_CLASS = T1.HR_CLASS ";
        $query .= "   LEFT JOIN SEMESTER_MST T2  ";
        $query .= "     ON T1.YEAR = T2.YEAR  ";
        $query .= "    AND T1.SEMESTER = T2.SEMESTER  ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '". CTRL_YEAR ."' ";
        $query .= "   AND T1.GRADE = '".$model->grade."' ";
        $query .= "   AND '".$eDate."' BETWEEN T2.SDATE AND T2.EDATE ";
        $query .= "   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ";
        $query .= "                  WHERE S1.SCHREGNO = T1.SCHREGNO  ";
        $query .= "                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '".$eDate."' THEN T2.EDATE ELSE '".$sDate."' END)  ";
        $query .= "                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '".$sDate."' THEN T2.EDATE ELSE '".$sDate."' END)) )  ";
        $query .= "   AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1  ";
        $query .= "                  WHERE S1.SCHREGNO = T1.SCHREGNO  ";
        $query .= "                    AND S1.TRANSFERCD IN ('1','2') ";
        $query .= "                    AND CASE WHEN T2.EDATE < '".$sDate."' THEN T2.EDATE ELSE '".$sDate."' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)  ";
        $query .= " ), SCHNO_B AS( ";  // 休学者数
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE  ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_HDAT TH ";
        $query .= "     ON TH.YEAR = T1.YEAR ";
        $query .= "    AND TH.SEMESTER = T1.SEMESTER ";
        $query .= "    AND TH.GRADE = T1.GRADE ";
        $query .= "    AND TH.HR_CLASS = T1.HR_CLASS ";
        $query .= "   LEFT JOIN SEMESTER_MST T2  ";
        $query .= "     ON T1.YEAR = T2.YEAR  ";
        $query .= "    AND T1.SEMESTER = T2.SEMESTER  ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '". CTRL_YEAR ."' ";
        $query .= "   AND T1.GRADE = '".$model->grade."' ";
        $query .= "   AND '".$eDate."' BETWEEN T2.SDATE AND T2.EDATE ";
        $query .= "   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ";
        $query .= "                  WHERE S1.SCHREGNO = T1.SCHREGNO  ";
        $query .= "                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '".$eDate."' THEN T2.EDATE ELSE '".$sDate."' END)  ";
        $query .= "                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '".$sDate."' THEN T2.EDATE ELSE '".$sDate."' END)) )  ";
        $query .= "   AND EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1  ";
        $query .= "                  WHERE S1.SCHREGNO = T1.SCHREGNO  ";
        $query .= "                    AND S1.TRANSFERCD IN ('1') ";
        $query .= "                    AND CASE WHEN T2.EDATE < '".$sDate."' THEN T2.EDATE ELSE '".$sDate."' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)  ";
        $query .= " ), SCHNO_C AS( ";  // 復学者数
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE  ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_HDAT TH ";
        $query .= "     ON TH.YEAR = T1.YEAR ";
        $query .= "    AND TH.SEMESTER = T1.SEMESTER ";
        $query .= "    AND TH.GRADE = T1.GRADE ";
        $query .= "    AND TH.HR_CLASS = T1.HR_CLASS ";
        $query .= "   LEFT JOIN SEMESTER_MST T2  ";
        $query .= "     ON T1.YEAR = T2.YEAR  ";
        $query .= "    AND T1.SEMESTER = T2.SEMESTER  ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '". CTRL_YEAR ."' ";
        $query .= "   AND T1.GRADE = '".$model->grade."' ";
        $query .= "   AND '".$eDate."' BETWEEN T2.SDATE AND T2.EDATE ";
        $query .= "   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ";
        $query .= "                  WHERE S1.SCHREGNO = T1.SCHREGNO  ";
        $query .= "                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '".$eDate."' THEN T2.EDATE ELSE '".$sDate."' END)  ";
        $query .= "                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '".$sDate."' THEN T2.EDATE ELSE '".$sDate."' END)) )  ";
        $query .= "   AND EXISTS(SELECT  'X' FROM ( ";
        $query .= "                                SELECT T1.SCHREGNO, MIN(T1.COMEBACK_DATE) AS COMEBACK_DATE ";
        $query .= "                                FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 ";
        $query .= "                                     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                      AND T2.YEAR = '". CTRL_YEAR ."' ";
        $query .= "                                      AND T2.SEMESTER = (SELECT TW.SEMESTER FROM SEMESTER_MST TW WHERE T1.COMEBACK_DATE BETWEEN TW.SDATE AND TW.EDATE) ";
        $query .= "                                      AND T2.GRADE = '".$model->grade."' ";
        $query .= "                                WHERE ";
        $query .= "                                  T1.COMEBACK_DATE < '".$eDate."' ";
        $query .= "                                GROUP BY ";
        $query .= "                                  T1.SCHREGNO ";
        $query .= "                               ) S1 ";
        $query .= "              WHERE S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "              ) ";
        $query .= " ), SCHNO_D AS( ";  // 留学者数
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE  ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_HDAT TH ";
        $query .= "     ON TH.YEAR = T1.YEAR ";
        $query .= "    AND TH.SEMESTER = T1.SEMESTER ";
        $query .= "    AND TH.GRADE = T1.GRADE ";
        $query .= "    AND TH.HR_CLASS = T1.HR_CLASS ";
        $query .= "   LEFT JOIN SEMESTER_MST T2  ";
        $query .= "     ON T1.YEAR = T2.YEAR  ";
        $query .= "    AND T1.SEMESTER = T2.SEMESTER  ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '". CTRL_YEAR ."' ";
        $query .= "   AND T1.GRADE = '".$model->grade."' ";
        $query .= "   AND '".$eDate."' BETWEEN T2.SDATE AND T2.EDATE ";
        $query .= "   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ";
        $query .= "                  WHERE S1.SCHREGNO = T1.SCHREGNO  ";
        $query .= "                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '".$eDate."' THEN T2.EDATE ELSE '".$sDate."' END)  ";
        $query .= "                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '".$sDate."' THEN T2.EDATE ELSE '".$sDate."' END)) )  ";
        $query .= "   AND EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1  ";
        $query .= "                  WHERE S1.SCHREGNO = T1.SCHREGNO  ";
        $query .= "                    AND S1.TRANSFERCD IN ('2') ";
        $query .= "                    AND CASE WHEN T2.EDATE < '".$sDate."' THEN T2.EDATE ELSE '".$sDate."' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)  ";
        $query .= " ),KEY_MARGE_WK1 AS ( ";
        $query .= " SELECT * FROM SCHNO_A ";
        $query .= " UNION ";
        $query .= " SELECT * FROM SCHNO_B ";
        $query .= " UNION ";
        $query .= " SELECT * FROM SCHNO_C ";
        $query .= " UNION ";
        $query .= " SELECT * FROM SCHNO_D ";
        $query .= " ),KEY_MARGE_WK2 AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= " T0.GRADE, T0.HR_CLASS, T0.ATTENDNO, T0.COURSECD, T0.MAJORCD, T0.COURSECODE, ";
        $query .= " (CASE WHEN VALUE(T1.ATTENDNO, '') = '' THEN 0 ELSE 1 END) AS T1ATTNO, ";
        $query .= " (CASE WHEN VALUE(T2.ATTENDNO, '') = '' THEN 0 ELSE 1 END) AS T2ATTNO, ";
        $query .= " (CASE WHEN VALUE(T3.ATTENDNO, '') = '' THEN 0 ELSE 1 END) AS T3ATTNO, ";
        $query .= " (CASE WHEN VALUE(T4.ATTENDNO, '') = '' THEN 0 ELSE 1 END) AS T4ATTNO ";
        $query .= " FROM KEY_MARGE_WK1 T0 ";
        $query .= "   LEFT JOIN SCHNO_A T1 ";
        $query .= "     ON T1.GRADE = T0.GRADE ";
        $query .= "    AND T1.HR_CLASS = T0.HR_CLASS ";
        $query .= "    AND T1.ATTENDNO = T0.ATTENDNO ";
        $query .= "    AND T1.COURSECD = T0.COURSECD ";
        $query .= "    AND T1.MAJORCD = T0.MAJORCD ";
        $query .= "    AND T1.COURSECODE = T0.COURSECODE ";
        $query .= "   LEFT JOIN SCHNO_B T2 ";
        $query .= "     ON T2.GRADE = T0.GRADE ";
        $query .= "    AND T2.HR_CLASS = T0.HR_CLASS ";
        $query .= "    AND T2.ATTENDNO = T0.ATTENDNO ";
        $query .= "    AND T2.COURSECD = T0.COURSECD ";
        $query .= "    AND T2.MAJORCD = T0.MAJORCD ";
        $query .= "    AND T2.COURSECODE = T0.COURSECODE ";
        $query .= "   LEFT JOIN SCHNO_C T3 ";
        $query .= "     ON T3.GRADE = T0.GRADE ";
        $query .= "    AND T3.HR_CLASS = T0.HR_CLASS ";
        $query .= "    AND T3.ATTENDNO = T0.ATTENDNO ";
        $query .= "    AND T3.COURSECD = T0.COURSECD ";
        $query .= "    AND T3.MAJORCD = T0.MAJORCD ";
        $query .= "    AND T3.COURSECODE = T0.COURSECODE ";
        $query .= "   LEFT JOIN SCHNO_D T4 ";
        $query .= "     ON T4.GRADE = T0.GRADE ";
        $query .= "    AND T4.HR_CLASS = T0.HR_CLASS ";
        $query .= "    AND T4.ATTENDNO = T0.ATTENDNO ";
        $query .= "    AND T4.COURSECD = T0.COURSECD ";
        $query .= "    AND T4.MAJORCD = T0.MAJORCD ";
        $query .= "    AND T4.COURSECODE = T0.COURSECODE ";
        $query .= " ) ";
        $query .= " SELECT ";
        if ($sum_param != "GRADE_SUM") {
            $query .= "   T0.COURSECD, ";
            $query .= "   T0.MAJORCD, ";
            $query .= "   T0.COURSECODE, ";
            $query .= "   T0.HR_CLASS, ";
        }
        $query .= "   T0.GRADE, ";
        $query .= "   SUM(T0.T1ATTNO) AS ZAISEKI_CNT, ";
        $query .= "   SUM(T0.T2ATTNO) AS KYUUGAKU_CNT, ";
        $query .= "   SUM(T0.T3ATTNO) AS FUKUGAKU_CNT, ";
        $query .= "   SUM(T0.T4ATTNO) AS RYUUGAKU_CNT ";
        $query .= " FROM ";
        $query .= "   KEY_MARGE_WK2 T0 ";
        $query .= " GROUP BY ";
        if ($sum_param != "GRADE_SUM") {
            $query .= "   T0.COURSECD, ";
            $query .= "   T0.MAJORCD, ";
            $query .= "   T0.COURSECODE, ";
            $query .= "   T0.HR_CLASS, ";
        }
        $query .= "   T0.GRADE ";
        $query .= " ORDER BY ";
        if ($sum_param != "GRADE_SUM") {
            $query .= "   T0.COURSECD, ";
            $query .= "   T0.MAJORCD, ";
            $query .= "   T0.COURSECODE, ";
        }
        $query .= "   T0.GRADE ";
        if ($sum_param != "GRADE_SUM") {
            $query .= "   ,T0.HR_CLASS ";
        }
        return $query;
    }

    //出席日数不足者を取得
    function getNotEnoughAttend($model) {
        //SQLが長いので、何をやっているかを記載。
        //1.換算で利用するデータがKAIKIN_MSTの1ヵ年皆勤の条件で計算するため、それを取得。
        //2.KAIKIN_MSTに1ヵ年皆勤の複数データ登録可能なので、優先度の高いもので、コードの若い物の最初の1件を採用する。
        //3.生徒の出欠情報と、遅刻早退の換算値を算出
        //4.クラス毎に出席すべき日数を算出
        //5.出席日数不足か、判定(生徒毎に出す処理もあるので、出席日数不足の集計はPRG側)。
        $query  = " WITH KANSAN_PATTERN AS ( ";
        $query .= " SELECT ";
        $query .= "   ROW_NUMBER() OVER(ORDER BY PRIORITY,KAIKIN_CD) AS ROWNUMBER, ";
        $query .= "   CASE WHEN TIKOKU_CONDITION IS NOT NULL THEN 2 ELSE 1 END AS CALC_PATTERN, ";
        $query .= "   KESSEKI_KANSAN, ";
        $query .= "   TIKOKU_CONDITION, ";
        $query .= "   SOUTAI_CONDITION ";
        $query .= " FROM ";
        $query .= "   KAIKIN_MST ";
        $query .= " WHERE ";
        $query .= "   KAIKIN_DIV = '1' ";
        $query .= "   AND REF_YEAR = '1' ";
        $query .= " ), KANSAN_1PATTERN AS ( ";  // 複数レコードある可能性を排除する(先頭だけ取得)。
        $query .= " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   KANSAN_PATTERN  ";
        $query .= " WHERE ";
        $query .= "   ROWNUMBER = 1 ";
        $query .= " ), SCH_RESULT AS ( ";  // 生徒の出欠情報と、遅刻早退の換算値を算出
        $query .= " SELECT ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLASSDAYS, ";
        $query .= "     T1.SUSPEND, ";
        $query .= "     T1.MOURNING, ";
        $query .= "     VALUE(T1.CLASSDAYS,0)-VALUE(T1.SUSPEND,0)-VALUE(T1.MOURNING,0) AS PRESENT1, ";
        $query .= "     VALUE(T1.SICK,0)+VALUE(T1.NOTICE,0)+VALUE(T1.NONOTICE,0) AS ABSENT, ";
        $query .= "     VALUE(T1.CLASSDAYS,0)-VALUE(T1.SUSPEND,0)-VALUE(T1.MOURNING,0) ";
        $query .= "           -VALUE(T1.SICK,0)-VALUE(T1.NOTICE,0)-VALUE(T1.NONOTICE,0) AS PRESENT2, ";
        $query .= "     T1.LATE, ";
        $query .= "     T1.EARLY, ";
        $query .= "     CASE WHEN T2.CALC_PATTERN IS NULL ";
        $query .= "               OR (T2.CALC_PATTERN <> 2 AND VALUE(T2.KESSEKI_KANSAN, 0) = 0) ";
        $query .= "               OR (T2.CALC_PATTERN = 2 AND (VALUE(T2.TIKOKU_CONDITION, 0) = 0 OR VALUE(T2.SOUTAI_CONDITION, 0) = 0)) ";
        $query .= "               THEN 0 ";  // 0割り対策
        $query .= "          WHEN T2.CALC_PATTERN = 2 THEN INT(T1.LATE / T2.TIKOKU_CONDITION) + INT(T1.EARLY / T2.SOUTAI_CONDITION) ";
        $query .= "          ELSE INT((T1.LATE + T1.EARLY) / T2.KESSEKI_KANSAN) ";
        $query .= "          END AS KANSAN_VAL ";
        $query .= " FROM ";
        $query .= " ( ";
        $query .= "  SELECT ";
        $query .= "      T3.COURSECD, ";
        $query .= "      T3.MAJORCD, ";
        $query .= "      T3.COURSECODE, ";
        $query .= "      T3.GRADE, ";
        $query .= "      T3.HR_CLASS, ";
        $query .= "      T3.ATTENDNO, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T4.NAME, ";
        $query .= "      SUM(VALUE(T1.LESSON, 0) - VALUE(OFFDAYS, 0) - VALUE(ABROAD, 0)) AS CLASSDAYS, ";
        $query .= "      SUM(T1.SUSPEND) AS SUSPEND, ";
        $query .= "      SUM(T1.MOURNING) AS MOURNING, ";
        $query .= "      SUM(T1.SICK) AS SICK, ";
        $query .= "      SUM(T1.NOTICE) AS NOTICE, ";
        $query .= "      SUM(T1.NONOTICE) AS NONOTICE, ";
        $query .= "      SUM(T1.LATE) AS LATE, ";
        $query .= "      SUM(T1.EARLY) AS EARLY ";
        $query .= "  FROM ";
        $query .= "      ATTEND_SEMES_DAT T1 ";
        $query .= "      INNER JOIN SCHREG_REGD_DAT T3 ";
        $query .= "        ON T3.YEAR = T1.YEAR ";
        $query .= "       AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "      INNER JOIN SCHREG_BASE_MST T4 ";
        $query .= "        ON T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T1.SEMESTER <= (SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER <> '9') ";
        $query .= "      AND T3.GRADE = '".$model->grade."' ";
        $query .= "  GROUP BY ";
        $query .= "      T3.COURSECD, ";
        $query .= "      T3.MAJORCD, ";
        $query .= "      T3.COURSECODE, ";
        $query .= "      T3.GRADE, ";
        $query .= "      T3.HR_CLASS, ";
        $query .= "      T3.ATTENDNO, ";
        $query .= "      T4.NAME, ";
        $query .= "      T1.SCHREGNO ";
        $query .= " )T1,KANSAN_1PATTERN T2 ";
        $query .= " ORDER BY ";
        $query .= "   HR_CLASS ";
        $query .= "   ,T1.ATTENDNO ";
        $query .= " ), CLS_MUSTDAY AS ( ";  // クラス内での出席すべき日数を算出
        $query .= " SELECT ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     MAX(T1.PRESENT1) AS MX_PRESENT ";
        $query .= " FROM ";
        $query .= "   SCH_RESULT T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     CASE WHEN T1.ABSENT + T1.KANSAN_VAL > INT(T2.MX_PRESENT / 4) THEN 1 ELSE 0 END AS FUSOKU_FLG, ";
        $query .= "     T1.CLASSDAYS, ";
        $query .= "     T1.SUSPEND, ";
        $query .= "     T1.MOURNING, ";
        $query .= "     T1.PRESENT1, ";
        $query .= "     T1.ABSENT, ";
        $query .= "     T1.PRESENT2, ";
        $query .= "     T1.LATE, ";
        $query .= "     T1.EARLY, ";
        $query .= "     T1.KANSAN_VAL ";
        $query .= " FROM ";
        $query .= "   SCH_RESULT T1 ";
        $query .= "   LEFT JOIN CLS_MUSTDAY T2 ";
        $query .= "     ON T2.GRADE = T1.GRADE ";
        $query .= "    AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "    AND T2.COURSECD = T1.COURSECD ";
        $query .= "    AND T2.MAJORCD = T1.MAJORCD ";
        $query .= "    AND T2.COURSECODE = T1.COURSECODE ";
        $query .= "   LEFT JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "     ON T3.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T3.SEMESTER = (SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER <> '9') ";
        $query .= "    AND T3.GRADE = T1.GRADE ";
        $query .= "    AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO ";
        return $query;
    }

    //成績不良者を取得
    function notYetStudent($model, $detailFlg=false) {
        $query  = " WITH CREDIT_BASE AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.CLASSCD, ";
        $query .= "   T1.SCHOOL_KIND, ";
        $query .= "   T1.CURRICULUM_CD, ";
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.COMP_CREDIT, ";
        $query .= "   CASE WHEN T1.COMP_CREDIT IS NULL OR T1.COMP_CREDIT = 0 THEN 1 ELSE 0 END AS CHK_NL ";
        $query .= " FROM ";
        $query .= "   RECORD_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = '9' ";
        $query .= "   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990008' ";
        $query .= " ), CREDIT_SUMMARY AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T2.COURSECD, ";
        $query .= "   T2.MAJORCD, ";
        $query .= "   T2.COURSECODE, ";
        $query .= "   T2.GRADE, ";
        $query .= "   T2.HR_CLASS, ";
        $query .= "   T2.ATTENDNO, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T3.NAME, ";
        $query .= "   SUM(T1.COMP_CREDIT) AS COMP_CREDITS, ";
        $query .= "   SUM(T1.CHK_NL) AS CHK_NLS ";
        $query .= " FROM ";
        $query .= "   CREDIT_BASE T1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST T3 ";
        $query .= "     ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "    T2.GRADE = '".$model->grade."' ";
        $query .= " GROUP BY ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T2.COURSECD, ";
        $query .= "   T2.MAJORCD, ";
        $query .= "   T2.COURSECODE, ";
        $query .= "   T2.GRADE, ";
        $query .= "   T2.HR_CLASS, ";
        $query .= "   T2.ATTENDNO, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T3.NAME ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.COURSECD, ";
        $query .= "   T1.MAJORCD, ";
        $query .= "   T1.COURSECODE, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        if ($detailFlg) {
            $query .= "   T1.ATTENDNO, ";
            $query .= "   T1.SCHREGNO, ";
            $query .= "   T1.NAME, ";
            $query .= "   T2.SCORE, ";
            $query .= "   T2.AVG, ";
            $query .= "   CASE WHEN T1.COMP_CREDITS >= ".$model->field["CREDIT_LINE"]." AND T1.CHK_NLS > 0 THEN 1 ELSE 0 END AS PAT1, ";
            $query .= "   CASE WHEN T1.COMP_CREDITS < ".$model->field["CREDIT_LINE"]." THEN 1 ELSE 0 END AS PAT2 ";
        } else {
            $query .= "   SUM(CASE WHEN T1.COMP_CREDITS >= ".$model->field["CREDIT_LINE"]." AND T1.CHK_NLS > 0 THEN 1 ELSE 0 END) AS PAT1, ";
            $query .= "   SUM(CASE WHEN T1.COMP_CREDITS < ".$model->field["CREDIT_LINE"]." THEN 1 ELSE 0 END) AS PAT2 ";
        }
        $query .= " FROM ";
        $query .= "   CREDIT_SUMMARY T1 ";
        if ($detailFlg) {
            $query .= "   LEFT JOIN RECORD_RANK_SDIV_DAT T2 ";
            $query .= "     ON T2.YEAR = T1.YEAR ";
            $query .= "    AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "    AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '990008' ";
            $query .= "    AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '99-".$model->schoolKind."-99-999999' ";
            $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        } else {
            $query .= " GROUP BY ";
            $query .= "   T1.COURSECD, ";
            $query .= "   T1.MAJORCD, ";
            $query .= "   T1.COURSECODE, ";
            $query .= "   T1.GRADE, ";
            $query .= "   T1.HR_CLASS ";
        }
        $query .= "  ORDER BY ";
        $query .= "    T1.COURSECD, ";
        $query .= "    T1.MAJORCD, ";
        $query .= "    T1.COURSECODE, ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.HR_CLASS ";
        if ($detailFlg) {
            $query .= "  ,T1.ATTENDNO, ";
            $query .= "T1.SCHREGNO, ";
            $query .= "T1.NAME ";
        }
        return $query;
    }

    //成績優秀者を取得(在籍者数の5%)
    function getExStudent($model) {
        $sDate = str_replace('/', '-', $model->field["SDATE"]);
        $eDate = str_replace('/', '-', $model->field["EDATE"]);

        $query  = " WITH SCHNO_A AS( ";  // 在籍者数
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE  ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN SEMESTER_MST T2  ";
        $query .= "     ON T1.YEAR = T2.YEAR  ";
        $query .= "    AND T1.SEMESTER = T2.SEMESTER  ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '". CTRL_YEAR ."' ";
        $query .= "   AND T1.GRADE = '".$model->grade."' ";
        $query .= "   AND '".$eDate."' BETWEEN T2.SDATE AND T2.EDATE ";
        $query .= "   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1  ";
        $query .= "                  WHERE S1.SCHREGNO = T1.SCHREGNO  ";
        $query .= "                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '".$eDate."' THEN T2.EDATE ELSE '".$sDate."' END)  ";
        $query .= "                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '".$sDate."' THEN T2.EDATE ELSE '".$sDate."' END)) )  ";
        $query .= "   AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1  ";
        $query .= "                  WHERE S1.SCHREGNO = T1.SCHREGNO  ";
        $query .= "                    AND S1.TRANSFERCD IN ('1','2') ";
        $query .= "                    AND CASE WHEN T2.EDATE < '".$sDate."' THEN T2.EDATE ELSE '".$sDate."' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)  ";
        $query .= " ), Z_CNT AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.COURSECD, ";
        $query .= "   T1.MAJORCD, ";
        $query .= "   T5.MAJORNAME, ";
        $query .= "   T1.COURSECODE, ";
        $query .= "   T6.COURSECODENAME, ";
        $query .= "   T1.GRADE, ";
        $query .= "   COUNT(T1.schregno) AS ZAISEKI_CNT ";
        $query .= " FROM ";
        $query .= "   SCHNO_A T1 ";
        $query .= "   LEFT JOIN MAJOR_MST T5 ";
        $query .= "     ON T5.COURSECD = T1.COURSECD ";
        $query .= "    AND T5.MAJORCD = T1.MAJORCD ";
        $query .= "   LEFT JOIN COURSECODE_MST T6 ";
        $query .= "     ON T6.COURSECODE = T1.COURSECODE ";
        $query .= " GROUP BY ";
        $query .= "   T1.COURSECD, ";
        $query .= "   T1.MAJORCD, ";
        $query .= "   T5.MAJORNAME, ";
        $query .= "   T1.COURSECODE, ";
        $query .= "   T6.COURSECODENAME, ";
        $query .= "   T1.GRADE ";
        $query .= " ORDER BY ";
        $query .= "   T1.COURSECD, ";
        $query .= "   T1.MAJORCD, ";
        $query .= "   T1.COURSECODE, ";
        $query .= "   T1.GRADE ";
        $query .= " ), DEV_RANK AS ( ";
        $query .= " SELECT ";
        $query .= "   row_number() over(partition by (T2.GRADE) order by GRADE_DEVIATION_RANK ) as R_NUM, ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.TESTKINDCD, ";
        $query .= "   T1.TESTITEMCD, ";
        $query .= "   T1.SCORE_DIV, ";
        $query .= "   T1.CLASSCD, ";
        $query .= "   T1.SCHOOL_KIND, ";
        $query .= "   T1.CURRICULUM_CD, ";
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T2.GRADE, ";
        $query .= "   T2.HR_CLASS, ";
        $query .= "   T2.ATTENDNO, ";
        $query .= "   T2.COURSECD, ";
        $query .= "   T2.MAJORCD, ";
        $query .= "   T2.COURSECODE, ";
        $query .= "   T3.NAME, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.SCORE, ";
        $query .= "   T1.AVG, ";
        $query .= "   T1.GRADE_DEVIATION_RANK ";
        $query .= " FROM ";
        $query .= "   RECORD_RANK_SDIV_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST T3 ";
        $query .= "     ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '". CTRL_YEAR ."' ";
        $query .= "   AND T1.SEMESTER = '1' ";
        $query .= "   AND T1.TESTKINDCD = '99' ";
        $query .= "   AND T1.TESTITEMCD = '00' ";
        $query .= "   AND T1.SCORE_DIV = '08' ";
        $query .= "   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '99-".$model->schoolKind."-99-999999' ";
        $query .= "   AND T2.GRADE = '".$model->grade."' ";
        $query .= " order by ";
        $query .= "   T2.GRADE, ";
        $query .= "   T1.GRADE_DEVIATION_RANK, ";
        $query .= "   T2.HR_CLASS, ";
        $query .= "   T2.ATTENDNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.* ";
        $query .= " FROM ";
        $query .= "   DEV_RANK T1 ";
        $query .= "   LEFT JOIN Z_CNT T2 ";
        $query .= "     ON T2.COURSECD = T1.COURSECD ";
        $query .= "    AND T2.MAJORCD = T1.MAJORCD ";
        $query .= "    AND T2.COURSECODE = T1.COURSECODE ";
        $query .= "    AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "   T1.GRADE_DEVIATION_RANK <= (SELECT GRADE_DEVIATION_RANK FROM DEV_RANK WHERE R_NUM = CEIL(T2.ZAISEKI_CNT * 5 / 100.0)) ";
        return $query;
    }

    function getKaikinType($model) {
        $query  = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   KAIKIN_MST ";
        $query .= " ORDER BY ";
        $query .= "   REF_YEAR DESC, ";
        $query .= "   KAIKIN_DIV ";
        return $query;
    }

    //皆勤・精勤者を取得
    function getKaikinSeikin($model) {
        $query  = " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.KAIKIN_CD, ";
        $query .= "   T2.KAIKIN_NAME, ";
        $query .= "   T2.KAIKIN_DIV, ";
        $query .= "   T2.REF_YEAR, ";
        $query .= "   T3.COURSECD, ";
        $query .= "   T3.MAJORCD, ";
        $query .= "   T3.COURSECODE, ";
        $query .= "   T3.GRADE, ";
        $query .= "   T4.GRADE_CD, ";
        $query .= "   T4.GRADE_NAME1, ";
        $query .= "   T3.HR_CLASS, ";
        $query .= "   T5.HR_NAME, ";
        $query .= "   T5.HR_NAMEABBV, ";
        $query .= "   T3.ATTENDNO, ";
        $query .= "   T6.NAME ";
        $query .= " FROM ";
        $query .= "   KAIKIN_DAT T1 ";
        $query .= "   INNER JOIN KAIKIN_MST T2 ";
        $query .= "     ON T2.KAIKIN_CD = T1.KAIKIN_CD ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "    AND T3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T4 ";
        $query .= "     ON T4.YEAR = T3.YEAR ";
        $query .= "    AND T4.GRADE = T3.GRADE ";
        $query .= "   LEFT JOIN SCHREG_REGD_HDAT T5 ";
        $query .= "     ON T5.YEAR = T3.YEAR ";
        $query .= "    AND T5.SEMESTER = T3.SEMESTER ";
        $query .= "    AND T5.GRADE = T3.GRADE ";
        $query .= "    AND T5.HR_CLASS = T3.HR_CLASS ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST T6 ";
        $query .= "     ON T6.SCHREGNO = T3.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T3.GRADE = '".$model->grade."' ";
        $query .= "   AND T1.KAIKIN_FLG = '1' ";
        $query .= "   AND VALUE(T1.INVALID_FLG, '0') = '0' ";
        $query .= " ORDER BY ";
        $query .= "   HR_CLASS, ";
        $query .= "   ATTENDNO ";
        return $query;
    }
}
?>
