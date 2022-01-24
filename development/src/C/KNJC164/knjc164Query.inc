<?php

require_once('for_php7.php');

class knjc164Query extends Query {
    //年組取得（権限チェック）
    function getAuth($model) {

        $selectGradeHrclass = "('".implode("','", explode(",", $model->selectdata) )."')";
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL, ";
            $query .= "        CASE WHEN T1.GRADE || T1.HR_CLASS IN ".$selectGradeHrclass." THEN '1' ELSE '0' END AS SELECTED ";
            $query .= " FROM SCHREG_REGD_HDAT T1 ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "     AND T2.GRADE = T1.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "     AND T2.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            } else {
                $query .= "     AND T2.SCHOOL_KIND IN ('J', 'H') ";
            }
            $query .= " WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL, ";
            $query .= "        CASE WHEN T1.GRADE || T1.HR_CLASS IN ".$selectGradeHrclass." THEN '1' ELSE '0' END AS SELECTED ";
            $query .= " FROM SCHREG_REGD_HDAT T1 ";
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "     AND T2.GRADE = T1.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "     AND T2.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            } else {
                $query .= "     AND T2.SCHOOL_KIND IN ('J', 'H') ";
            }
            $query .= " WHERE ";
            $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
            $query .= "     AND (  T1.TR_CD1  = '".STAFFCD."' ";
            $query .= "         OR T1.TR_CD2  = '".STAFFCD."' ";
            $query .= "         OR T1.TR_CD3  = '".STAFFCD."') ";
            $query .= " ORDER BY VALUE, LABEL ";
        }
        return $query;
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
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //年組取得
    function getHrName($grade, $hrClass) {
        $query  = " SELECT T1.HR_NAME ";
        $query .= " FROM SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE    = '".$grade."' ";
        $query .= "     AND T1.HR_CLASS = '".$hrClass."' ";
        return $query;
    }

    //在籍データ取得
    function getRegd($grade, $hrClass) {
        $query  = " SELECT T1.SCHREGNO, T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, T1.ATTENDNO, T2.NAME_SHOW, T2.NAME_ENG ";
        $query .= " FROM SCHREG_REGD_DAT T1 ";
        $query .= " INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE    = '".$grade."' ";
        $query .= "     AND T1.HR_CLASS = '".$hrClass."' ";
        $query .= " ORDER BY T1.ATTENDNO ";
        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //職員情報取得
    function getStaffInfo() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_SEQ_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' AND ";
        $query .= "     STAFF_SEQ = '001' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($year, $namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . $year . "' ";
        $query .= "     AND NAMECD1 = '" . $namecd1 . "' ";
        $query .= "     AND NAMECD2 = '" . $namecd2 . "' ";
        return $query;
    }

    function getSchoolMstMap($model, $db, $year) {
        $rtnMap = array();
        $query = "SELECT * FROM V_SCHOOL_MST WHERE YEAR = '" . $year . "'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $rtnMap[$key] = $val;
            }
        }
        return $rtnMap;
    }

    function attendSubclass(
                            $defineSchoolCode,
                            $year,
                            $knjSchoolMst,
                            $periodInState,
                            $sdate,
                            $edate,
                            $hasuuMap,
                            $grade,
                            $hrClass,
                            $model
    ) {
        $attendSql = knjc164Query::getAttendSubclassSql(
                                    false, // $hasuuMap["semesFlg"],
                                    $defineSchoolCode,
                                    $knjSchoolMst,
                                    $year,
                                    "1",
                                    "9",
                                    $hasuuMap["attendSemesInState"],
                                    $periodInState,
                                    "", // $hasuuMap["befDayFrom"],
                                    "", // $hasuuMap["befDayTo"],
                                    $sdate, // $hasuuMap["aftDayFrom"],
                                    $edate, // $hasuuMap["aftDayTo"],
                                    $grade,
                                    $hrClass,
                                    '',
                                    $model->Properties["useCurriculumcd"],
                                    $model
                                    );
        return $attendSql;
    }

    /**
     * 科目別出欠データSQLを返す(KNJD154から移行)
     * -- 学期またがり可
     * -- 開始日付の端数可
     * -- 終了日付の端数可
     * 実行結果の SEMESTER が "9" は学期の総合計
     * @param $defineSchoolCode    KNJDefineSchool
     * @param $knjSchoolMst        KNJSchoolMst
     * @param $year          年度
     * @param $sSemester     対象学期範囲From
     * @param $eSemester     対象学期範囲To
     * @param $semesInState  ATTEND_SUBCLASS_DATの対象(学期＋月)
     * @param $periodInState 対象校時
     * @param $befDayFrom    開始日付の端数用From
     * @param $befDayTo      開始日付の端数用To
     * @param $aftDayFrom    終了日付の端数用From
     * @param $aftDayTo      終了日付の端数用To
     * @param $grade         学年：指定しない場合は、Null
     * @param $hrClass       クラス：指定しない場合は、Null
     * @param $schregno      学籍番号：指定しない場合は、Null
     * @param $useCurriculumcd 1=教育課程コードを使用する
     * @return 出欠データSQLを返す
     */
    function getAttendSubclassSql(
            $semesFlg,
            $defineSchoolCode,
            $knjSchoolMst,
            $year,
            $sSemester,
            $eSemester,
            $semesInState,
            $periodInState,
            $befDayFrom,
            $befDayTo,
            $aftDayFrom,
            $aftDayTo,
            $grade,
            $hrClass,
            $schregno,
            $useCurriculumcd,
            $model
    ) {
        $query = "";

        //対象生徒
        $query .= "WITH SCHNO0 AS( ";
        $query .= " SELECT DISTINCT ";
        $query .= "    W1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_DAT W1 ";
        $query .= " WHERE ";
        $query .= "    W1.YEAR = '".$year."' ";
        $query .= "    AND W1.SEMESTER BETWEEN '".$sSemester."' AND '".$eSemester."' ";
        if ($schregno != '') {
            $query .= "    AND W1.SCHREGNO = '".$schregno."' ";
        }
        if ($grade != '') {
            $query .= "    AND W1.GRADE = '".$grade."' ";
        }
        if ($hrClass != '') {
            $query .= "    AND W1.HR_CLASS = '".$hrClass."' ";
        }
        $query .= " ), SCHNO AS( ";
        $query .= " SELECT ";
        $query .= "    W1.SCHREGNO, ";
        $query .= "    W1.GRADE, ";
        $query .= "    W1.SEMESTER, ";
        $query .= "    W1.HR_CLASS, ";
        $query .= "    W1.COURSECD, ";
        $query .= "    W1.MAJORCD, ";
        $query .= "    W1.COURSECODE ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_DAT W1, SCHNO0 W2 ";
        $query .= " WHERE ";
        $query .= "    W1.YEAR = '".$year."' ";
        $query .= "    AND W1.SEMESTER BETWEEN '".$sSemester."' AND '".$eSemester."' ";
        $query .= "    AND W1.SCHREGNO = W2.SCHREGNO ";

        //端数計算有無の判定
        if ($befDayFrom != '' || $aftDayFrom != '') {
            //対象生徒の時間割データ
            $query .= " ), SCHEDULE_SCHREG_R AS( ";
            $query .= " SELECT ";
            $query .= "    T2.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE, ";
            $query .= "    T1.CHAIRCD, ";
            $query .= "    T1.PERIODCD, ";
            $query .= "    T1.DATADIV ";
            $query .= " FROM ";
            $query .= "    SCH_CHR_DAT T1, ";
            $query .= "    CHAIR_STD_DAT T2 ";
            $query .= " WHERE ";
            $query .= "    T1.YEAR = '".$year."' ";
            $query .= "    AND T1.SEMESTER BETWEEN '".$sSemester."' AND '".$eSemester."' ";
            $query .= "    AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
            if ($befDayFrom != '' && $aftDayFrom != '') {
                $query .= "    AND (T1.EXECUTEDATE BETWEEN '".$befDayFrom."' AND '".$befDayTo."' ";
                $query .= "         OR T1.EXECUTEDATE BETWEEN '".$aftDayFrom."' AND '".$aftDayTo."') ";
            } else if ($befDayFrom != '') {
                $query .= "    AND T1.EXECUTEDATE BETWEEN '".$befDayFrom."' AND '".$befDayTo."' ";
            } else if ($aftDayFrom != '') {
                $query .= "    AND T1.EXECUTEDATE BETWEEN '".$aftDayFrom."' AND '".$aftDayTo."' ";
            }
            $query .= "    AND T1.YEAR = T2.YEAR ";
            $query .= "    AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "    AND T1.CHAIRCD = T2.CHAIRCD ";
            if ($defineSchoolCode != '' && $defineSchoolCode["usefromtoperiod"])
                $query .= "    AND T1.PERIODCD IN ".$periodInState." ";
            $query .= "    AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO GROUP BY SCHREGNO) ";
            $query .= "    AND NOT EXISTS(SELECT ";
            $query .= "                       'X' ";
            $query .= "                   FROM ";
            $query .= "                       SCHREG_BASE_MST T3 ";
            $query .= "                   WHERE ";
            $query .= "                       T3.SCHREGNO = T2.SCHREGNO ";
            $query .= "                       AND ((ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE) ";
            $query .= "                             OR (GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE)) ";
            $query .= "                  ) ";
	        $query .= "    AND NOT EXISTS(SELECT ";
	        $query .= "                       'X' ";
	        $query .= "                   FROM ";
	        $query .= "                       ATTEND_DAT L4 ";
            $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
	        $query .= "                   WHERE ";
	        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
	        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
	        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
	        $query .= "                       AND ATT_DI.REP_DI_CD = '27' "; // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
	        $query .= "                  ) ";
            $query .= "    AND NOT EXISTS(SELECT ";
            $query .= "                       'X' ";
            $query .= "                   FROM ";
            $query .= "                       ATTEND_DAT L4 ";
            $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
            $query .= "                   WHERE ";
            $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
            $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
            $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
            $query .= "                       AND ATT_DI.REP_DI_CD = '28' "; // 勤怠コード'28'は時間割にカウントしない
            $query .= "                  ) ";
            $query .= " GROUP BY ";
            $query .= "    T2.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE, ";
            $query .= "    T1.CHAIRCD, ";
            $query .= "    T1.PERIODCD, ";
            $query .= "    T1.DATADIV ";

            $query .= " ), SCHEDULE_SCHREG AS( ";
            $query .= " SELECT ";
            $query .= "    T1.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE, ";
            $query .= "    T1.CHAIRCD, ";
            $query .= "    T1.PERIODCD, ";
            $query .= "    T1.DATADIV ";
            $query .= " FROM ";
            $query .= "    SCHEDULE_SCHREG_R T1 ";
            $query .= "    INNER JOIN (";
            $query .= "     SELECT ";
            $query .= "        T1.SCHREGNO, ";
            $query .= "        T1.SEMESTER, ";
            $query .= "        T1.EXECUTEDATE, ";
            $query .= "        MIN(T1.CHAIRCD) AS CHAIRCD, ";
            $query .= "        T1.PERIODCD ";
            $query .= "     FROM ";
            $query .= "        SCHEDULE_SCHREG_R T1 ";
            $query .= "     WHERE ";
            $query .= "        NOT EXISTS(SELECT ";
            $query .= "                           'X' ";
            $query .= "                       FROM ";
            $query .= "                           SCHREG_TRANSFER_DAT T3 ";
            $query .= "                       WHERE ";
            $query .= "                           T3.SCHREGNO = T1.SCHREGNO ";
            $query .= "                           AND TRANSFERCD IN('1','2') ";
            $query .= "                           AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
            $query .= "                      ) ";
            $query .= "     GROUP BY ";
            $query .= "        T1.SCHREGNO, ";
            $query .= "        T1.SEMESTER, ";
            $query .= "        T1.EXECUTEDATE, ";
            $query .= "        T1.PERIODCD ";
            $query .= "    ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SEMESTER = T1.SEMESTER AND T1.EXECUTEDATE = T2.EXECUTEDATE ";
            $query .= "      AND T2.PERIODCD = T1.PERIODCD ";
        }
        

        //端数計算有無の判定
        if ($befDayFrom != '' || $aftDayFrom != '') {
            // テスト項目マスタの集計フラグ
            $query .= " ),TEST_COUNTFLG AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.EXECUTEDATE, ";
            $query .= "         T1.PERIODCD, ";
            $query .= "         T1.CHAIRCD, ";
            $query .= "         '2' AS DATADIV ";
            $query .= "     FROM ";
            $query .= "         SCH_CHR_TEST T1, ";

            if ("TESTITEM_MST_COUNTFLG" == $model->Properties["useTestCountflg"]) {
                $query .= "         TESTITEM_MST_COUNTFLG T2 ";
                $query .= "     WHERE ";
                $query .= "         T2.YEAR       = T1.YEAR ";
            } else if ("TESTITEM_MST_COUNTFLG_NEW_SDIV" == $model->Properties["useTestCountflg"]) {
                $query .= "         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
                $query .= "     WHERE ";
                $query .= "         T2.YEAR       = T1.YEAR ";
                $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
                $query .= "         AND T2.SCORE_DIV  = '01' ";
            } else {
                $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
                $query .= "     WHERE ";
                $query .= "         T2.YEAR       = T1.YEAR ";
                $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
            }
            $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
            $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
            $query .= "         AND T2.COUNTFLG   = '0' ";
        }

        if ($befDayFrom != '' || $aftDayFrom != '') {
            $query .= " ), T_ATTEND_DAT AS ( ";
            $query .= " SELECT ";
            $query .= "    T1.CHAIRCD, ";
            if ("1" == $useCurriculumcd) {
                $query .= "    T2.CLASSCD, ";
                $query .= "    T2.CURRICULUM_CD, ";
                $query .= "    T2.SCHOOL_KIND, ";
            }
            $query .= "    T2.SUBCLASSCD, ";
            $query .= "    T1.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE AS ATTENDDATE, ";
            $query .= "    T1.PERIODCD, ";
            $query .= "    '000' AS DI_CD, ";
            $query .= "    '' AS MULTIPLY, ";
            $query .= "    T1.DATADIV ";
            $query .= " FROM ";
            $query .= "    SCHEDULE_SCHREG_R T1 ";
            $query .= "    INNER JOIN CHAIR_DAT T2 ON T2.SEMESTER = T1.SEMESTER ";
            $query .= "        AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "        AND T2.YEAR = '".$year."' ";

            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "    T1.CHAIRCD, ";
            if ("1" == $useCurriculumcd) {
                $query .= "    T2.CLASSCD, ";
                $query .= "    T2.CURRICULUM_CD, ";
                $query .= "    T2.SCHOOL_KIND, ";
            }
            $query .= "    T2.SUBCLASSCD, ";
            $query .= "    T1.SCHREGNO, ";
            $query .= "    T1.SEMESTER, ";
            $query .= "    T1.EXECUTEDATE AS ATTENDDATE, ";
            $query .= "    T1.PERIODCD, ";
            $query .= "    CASE WHEN ATT_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATT_DI.ATSUB_REPL_DI_CD ELSE ATT_DI.REP_DI_CD END AS DI_CD, ";
            $query .= "    ATT_DI.MULTIPLY, ";
            $query .= "    T1.DATADIV ";
            $query .= " FROM ";
            $query .= "    SCHEDULE_SCHREG T1 ";
            $query .= "    INNER JOIN CHAIR_DAT T2 ON T2.SEMESTER = T1.SEMESTER ";
            $query .= "        AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "        AND T2.YEAR = '".$year."' ";
            $query .= "    INNER JOIN ATTEND_DAT T0 ON T0.SCHREGNO = T1.SCHREGNO ";
            $query .= "        AND T0.ATTENDDATE = T1.EXECUTEDATE ";
            $query .= "        AND T0.PERIODCD = T1.PERIODCD ";
            $query .= "    LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR = T0.YEAR ";
            $query .= "         AND ATT_DI.DI_CD = T0.DI_CD ";

            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     T1.CHAIRCD, ";
            if ("1" == $useCurriculumcd) {
                $query .= "    T3.CLASSCD, ";
                $query .= "    T3.CURRICULUM_CD, ";
                $query .= "    T3.SCHOOL_KIND, ";
            }
            $query .= "     T3.SUBCLASSCD, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     VALUE(T1.SEMESTER, '9') AS SEMESTER, ";
            $query .= "     T1.EXECUTEDATE AS ATTENDDATE, ";
            $query .= "     T1.PERIODCD, ";
            $query .= "     '01' AS DI_CD, ";
            $query .= "     '' AS MULTIPLY, ";
            $query .= "     T1.DATADIV ";
            $query .= " FROM ";
            $query .= "     SCHEDULE_SCHREG_R T1, ";
            $query .= "     SCHREG_TRANSFER_DAT T2, ";
            $query .= "     CHAIR_DAT T3 ";
            $query .= " WHERE ";
            $query .= "     EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 SCHNO E1 ";
            $query .= "             WHERE ";
            $query .= "                 E1.SCHREGNO = T2.SCHREGNO ";
            $query .= "             ) ";
            $query .= "     AND T2.TRANSFERCD IN('1') ";
            $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "     AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ";
            $query .= "     AND T3.YEAR = '".$year."' ";
            $query .= "     AND T3.SEMESTER = T1.SEMESTER ";
            $query .= "     AND T3.CHAIRCD = T1.CHAIRCD  ";
            $query .= " GROUP BY ";
            $query .= "     GROUPING SETS ";
            if ("1" == $useCurriculumcd) {
                $query .= "      ((T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T3.CLASSCD, T3.CURRICULUM_CD, T3.SCHOOL_KIND, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV), ";
                $query .= "       (T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T3.CLASSCD, T3.CURRICULUM_CD, T3.SCHOOL_KIND,              T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV)) ";
            } else {
                $query .= "      ((T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV), ";
                $query .= "       (T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO,              T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV)) ";
            }

            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     T1.CHAIRCD, ";
            if ("1" == $useCurriculumcd) {
                $query .= "    T3.CLASSCD, ";
                $query .= "    T3.CURRICULUM_CD, ";
                $query .= "    T3.SCHOOL_KIND, ";
            }
            $query .= "     T3.SUBCLASSCD, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     VALUE(T1.SEMESTER, '9') AS SEMESTER, ";
            $query .= "     T1.EXECUTEDATE AS ATTENDDATE, ";
            $query .= "     T1.PERIODCD, ";
            $query .= "     '00' AS DI_CD, ";
            $query .= "     '' AS MULTIPLY, ";
            $query .= "     T1.DATADIV ";
            $query .= " FROM ";
            $query .= "     SCHEDULE_SCHREG_R T1, ";
            $query .= "     SCHREG_TRANSFER_DAT T2, ";
            $query .= "     CHAIR_DAT T3 ";
            $query .= " WHERE ";
            $query .= "     EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 SCHNO E1 ";
            $query .= "             WHERE ";
            $query .= "                 E1.SCHREGNO = T2.SCHREGNO ";
            $query .= "             ) ";
            $query .= "     AND T2.TRANSFERCD IN('2') ";
            $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "     AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ";
            $query .= "     AND T3.YEAR = '".$year."' ";
            $query .= "     AND T3.SEMESTER = T1.SEMESTER ";
            $query .= "     AND T3.CHAIRCD = T1.CHAIRCD  ";
            $query .= " GROUP BY ";
            $query .= "     GROUPING SETS ";
            if ("1" == $useCurriculumcd) {
                $query .= "      ((T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T3.CLASSCD, T3.CURRICULUM_CD, T3.SCHOOL_KIND, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV), ";
                $query .= "       (T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T3.CLASSCD, T3.CURRICULUM_CD, T3.SCHOOL_KIND,              T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV)) ";
            } else {
                $query .= "      ((T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV), ";
                $query .= "       (T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO,              T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV)) ";
            }

        }
        
        $query .= "), SCH_ATTEND_SUM1 AS(";
        $unionAll = "";
        if (!$semesFlg || $befDayFrom != '' || $aftDayFrom != '') {
            $query .= "    SELECT ";
            $query .= "        T1.SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "        T1.CLASSCD, ";
                $query .= "        T1.CURRICULUM_CD, ";
                $query .= "        T1.SCHOOL_KIND, ";
            }
            $query .= "        T1.SUBCLASSCD, ";
            $query .= "        T1.SEMESTER, ";
            $query .= "        SUM(CASE WHEN DI_CD = '000' THEN 1 ELSE 0 END) ";
            $query .= "     - SUM(CASE WHEN DI_CD IN ( ";
            if ("1" != $knjSchoolMst["SUB_OFFDAYS"]) {
                $query .= "    '00' , ";
            }
            $query .= "        '01') THEN 1 ELSE 0 END) AS LESSON, ";
            $query .= "        SUM(CASE WHEN DI_CD = '000' THEN 1 ELSE 0 END) ";
            $query .= "     - SUM(CASE WHEN DI_CD IN ( ";
            if ("1" != $knjSchoolMst["SUB_OFFDAYS"]) {
                $query .= "    '00' , ";
            }
            $query .= "        '01') THEN 1 ELSE 0 END) ";
            $query .= "     - SUM(CASE WHEN DI_CD IN ( ";
            $comma = "";
            if ("1" != $knjSchoolMst["SUB_SUSPEND"]) {
                $query .= "   ".$comma." '2', '9' ";
                $comma = ",";
            }
            if ("1" != $knjSchoolMst["SUB_MOURNING"]) {
                $query .= "   ".$comma." '3', '10' ";
                $comma = ",";
            }
            if ("1" != $knjSchoolMst["SUB_VIRUS"] && $model->Properties["useVirus"] == "true") {
                $query .= "   ".$comma." '19', '20' ";
                $comma = ",";
            }
            if ("1" != $knjSchoolMst["SUB_KOUDOME"] && $model->Properties["useKoudome"] == "true") {
                $query .= "   ".$comma." '25', '26' ";
                $comma = ",";
            }
            if ("" == $comma) {
                $query .= " '' ";
            }
            $query .= "          ) THEN 1 ELSE 0 END) AS MLESSON, ";
            $query .= "        SUM(CASE WHEN DI_CD = '01' THEN 1 ELSE 0 END) AS ABROAD, ";
            $query .= "        SUM(CASE WHEN DI_CD = '00' THEN 1 ELSE 0 END) AS OFFDAYS, ";
            $query .= "        SUM(CASE WHEN DI_CD IN ('1', '8') THEN 1 ELSE 0 END) AS KOUKETSU, ";
            $query .= "        SUM(CASE WHEN DI_CD IN ('2', '9') THEN 1 ELSE 0 END) AS SUSPEND, ";
            $query .= "        SUM(CASE WHEN DI_CD IN ('3', '10') THEN 1 ELSE 0 END) AS MOURNING, ";
            if ($model->Properties["useVirus"] == "true") {
                $query .= "        SUM(CASE WHEN DI_CD IN ('19', '20') THEN 1 ELSE 0 END) AS VIRUS, ";
            }
            if ($model->Properties["useKoudome"] == "true") {
                $query .= "        SUM(CASE WHEN DI_CD IN ('25', '26') THEN 1 ELSE 0 END) AS KOUDOME, ";
            }
            $query .= "        SUM(CASE WHEN DI_CD = '14' THEN 1 ELSE 0 END) AS NURSEOFF, ";
            $query .= "        SUM(CASE WHEN DI_CD IN ('15','23','24') THEN SMALLINT(VALUE(MULTIPLY, '1')) ELSE 0 END) AS LATE, ";
            $query .= "        SUM(CASE WHEN DI_CD = '16' THEN 1 ELSE 0 END) AS EARLY, ";
            $query .= "        SUM(CASE WHEN DI_CD IN( ";
            if ($knjSchoolMst["SUB_OFFDAYS"]&& $knjSchoolMst["SUB_OFFDAYS"]== "1") {
                $query .= "    '00', ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] && $knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "    '2', '9', ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] && $knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "    '19', '20', ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] && $knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "    '25', '26', ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] && $knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "    '3', '10', ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] && $knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "    '1', '8', ";
            }
            $query .= "        '4','5','6','14','11','12','13') THEN 1 ELSE 0 END) ";
            $query .= "        AS ABSENT1, ";
            $query .= "        SUM(CASE WHEN DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ";
            $query .= "    FROM ";
            $query .= "        T_ATTEND_DAT T1 ";
            $query .= "        , SCHNO T0 ";
            $query .= "    WHERE ";
            $query .= "        T1.SCHREGNO = T0.SCHREGNO ";
            $query .= "        AND T1.SEMESTER = T0.SEMESTER ";
            if ($defineSchoolCode["useschchrcountflg"]) {
                $query .= "        AND NOT EXISTS(SELECT ";
                $query .= "                           'X' ";
                $query .= "                       FROM ";
                $query .= "                           SCH_CHR_COUNTFLG T4 ";
                $query .= "                       WHERE ";
                $query .= "                           T4.EXECUTEDATE = T1.ATTENDDATE ";
                $query .= "                           AND T4.PERIODCD = T1.PERIODCD ";
                $query .= "                           AND T4.CHAIRCD = T1.CHAIRCD ";
                $query .= "                           AND T1.DATADIV IN ('0', '1') ";
                $query .= "                           AND T4.GRADE = T0.GRADE ";
                $query .= "                           AND T4.HR_CLASS = T0.HR_CLASS ";
                $query .= "                           AND T4.COUNTFLG = '0') ";
                $query .= "        AND NOT EXISTS(SELECT ";
                $query .= "                           'X' ";
                $query .= "                       FROM ";
                $query .= "                           TEST_COUNTFLG TEST ";
                $query .= "                       WHERE ";
                $query .= "                           TEST.EXECUTEDATE  = T1.ATTENDDATE ";
                $query .= "                           AND TEST.PERIODCD = T1.PERIODCD ";
                $query .= "                           AND TEST.CHAIRCD  = T1.CHAIRCD ";
                $query .= "                           AND TEST.DATADIV  = T1.DATADIV) ";
            }
            $query .= "    GROUP BY ";
            $query .= "        T1.SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "        T1.CLASSCD, ";
                $query .= "        T1.CURRICULUM_CD, ";
                $query .= "        T1.SCHOOL_KIND, ";
            }
            $query .= "        T1.SUBCLASSCD, ";
            $query .= "        T1.SEMESTER ";
            $unionAll = "    UNION ALL "; 
        }
        if ($semesFlg) {
            $query .= $unionAll;
            $query .= "    SELECT ";
            $query .= "        T1.SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "    T1.CLASSCD, ";
                $query .= "    T1.CURRICULUM_CD, ";
                $query .= "    T1.SCHOOL_KIND, ";
            }
            $query .= "        T1.SUBCLASSCD, ";
            $query .= "        SEMESTER, ";
            $query .= "        SUM(VALUE(T1.LESSON,0) ";
            if ("1" != $knjSchoolMst["SUB_OFFDAYS"]) {
                $query .= "            - VALUE(T1.OFFDAYS, 0) ";
            }
            $query .= "            - VALUE(T1.ABROAD, 0)";
            $query .= "        ) AS LESSON, ";
            $query .= "        SUM(VALUE(T1.LESSON,0) ";
            if ("1" != $knjSchoolMst["SUB_OFFDAYS"]) {
                $query .= "            - VALUE(T1.OFFDAYS, 0) ";
            }
            $query .= "            - VALUE(T1.ABROAD, 0)";
            if ("1" != $knjSchoolMst["SUB_SUSPEND"]) {
                $query .= "            - VALUE(T1.SUSPEND, 0) ";
            }
            if ("1" != $knjSchoolMst["SUB_MOURNING"]) {
                $query .= "            - VALUE(T1.MOURNING, 0) ";
            }
            if ("1" != $knjSchoolMst["SUB_VIRUS"] && $model->Properties["useVirus"] == "true") {
                $query .= "            - VALUE(T1.VIRUS, 0) ";
            }
            if ("1" != $knjSchoolMst["SUB_KOUDOME"] && $model->Properties["useKoudome"] == "true") {
                $query .= "            - VALUE(T1.KOUDOME, 0) ";
            }
            $query .= "        ) AS MLESSON, ";
            $query .= "        SUM(VALUE(T1.ABROAD, 0)) AS ABROAD, ";
            $query .= "        SUM(VALUE(T1.OFFDAYS, 0)) AS OFFDAYS, ";
            $query .= "        SUM(VALUE(T1.ABSENT, 0)) AS KOUKETSU, ";
            $query .= "        SUM(VALUE(T1.SUSPEND, 0)) AS SUSPEND, ";
            $query .= "        SUM(VALUE(T1.MOURNING, 0)) AS MOURNING, ";
            if ($model->Properties["useVirus"] == "true") {
                $query .= "        SUM(VALUE(T1.VIRUS, 0)) AS VIRUS, ";
            }
            if ($model->Properties["useKoudome"] == "true") {
                $query .= "        SUM(VALUE(T1.KOUDOME, 0)) AS KOUDOME, ";
            }
            $query .= "        SUM(VALUE(T1.NURSEOFF,0)) AS NURSEOFF, ";
            $query .= "        SUM(VALUE(T1.LATE,0)) AS LATE, ";
            $query .= "        SUM(VALUE(T1.EARLY,0)) AS EARLY, ";
            $query .= "        SUM(VALUE(T1.SICK,0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0) + VALUE(T1.NURSEOFF,0) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] && $knjSchoolMst["SUB_OFFDAYS"]== "1") {
                $query .= "            + VALUE(T1.OFFDAYS, 0) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] && $knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + VALUE(T1.SUSPEND, 0) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] && $knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + VALUE(T1.VIRUS, 0) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] && $knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + VALUE(T1.KOUDOME, 0) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] && $knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + VALUE(T1.MOURNING, 0) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] && $knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + VALUE(T1.ABSENT, 0) ";
            }
            $query .= "        ) AS ABSENT1, ";
            $query .= "        SUM(VALUE(T1.LATE,0) + VALUE(T1.EARLY,0)) AS LATE_EARLY ";
            $query .= "    FROM ";
            $query .= "        ATTEND_SUBCLASS_DAT T1 ";
            $query .= "    WHERE ";
            $query .= "        T1.YEAR = '".$year."' ";
            $query .= "        AND T1.SEMESTER BETWEEN '".$sSemester."' AND '".$eSemester."' ";
            $query .= "        AND T1.SEMESTER || T1.MONTH  IN ".$semesInState." ";
            $query .= "        AND EXISTS(SELECT ";
            $query .= "                       'X' ";
            $query .= "                   FROM ";
            $query .= "                       SCHNO T2 ";
            $query .= "                   WHERE ";
            $query .= "                       T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                   GROUP BY ";
            $query .= "                       SCHREGNO) ";
            $query .= "    GROUP BY ";
            $query .= "        T1.SCHREGNO, ";
            $query .= "        T1.SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "    T1.CLASSCD, ";
                $query .= "    T1.CURRICULUM_CD, ";
                $query .= "    T1.SCHOOL_KIND, ";
            }
            $query .= "        T1.SUBCLASSCD ";
        }

        $query .= "), SCH_ATTEND_SUM AS(";
        $query .= "    SELECT ";
        $query .= "        T1.SCHREGNO, ";
        if ("1" == $useCurriculumcd) {
            $query .= "    T1.CLASSCD, ";
            $query .= "    T1.CURRICULUM_CD, ";
            $query .= "    T1.SCHOOL_KIND, ";
        }
        $query .= "        T1.SUBCLASSCD, ";
        $query .= "        SEMESTER, ";
        $query .= "        SUM(VALUE(T1.LESSON, 0)) AS LESSON, ";
        $query .= "        SUM(VALUE(T1.MLESSON, 0)) AS MLESSON, ";
        $query .= "        SUM(VALUE(T1.ABROAD, 0)) AS ABROAD, ";
        $query .= "        SUM(VALUE(T1.OFFDAYS, 0)) AS OFFDAYS, ";
        $query .= "        SUM(VALUE(T1.SUSPEND, 0)) AS SUSPEND, ";
        $query .= "        SUM(VALUE(T1.MOURNING, 0)) AS MOURNING, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "        SUM(VALUE(T1.VIRUS, 0)) AS VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "        SUM(VALUE(T1.KOUDOME, 0)) AS KOUDOME, ";
        }
        $query .= "        SUM(VALUE(T1.NURSEOFF,0)) AS NURSEOFF, ";
        $query .= "        SUM(VALUE(T1.KOUKETSU,0)) AS KOUKETSU, ";
        $query .= "        SUM(VALUE(T1.LATE,0)) AS LATE, ";
        $query .= "        SUM(VALUE(T1.EARLY,0)) AS EARLY, ";
        $query .= "        SUM(VALUE(T1.ABSENT1,0)) AS ABSENT1, ";
        $query .= "        SUM(VALUE(T1.LATE_EARLY,0)) AS LATE_EARLY ";
        $query .= "    FROM ";
        $query .= "        SCH_ATTEND_SUM1 T1 ";
        $query .= "    GROUP BY ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        T1.SEMESTER, ";
        if ("1" == $useCurriculumcd) {
            $query .= "    T1.CLASSCD, ";
            $query .= "    T1.CURRICULUM_CD, ";
            $query .= "    T1.SCHOOL_KIND, ";
        }
        $query .= "        T1.SUBCLASSCD ";
        
        $query .= "), ATTEND_SPECIAL AS(";
        $query .= "     SELECT W1.SCHREGNO, W1.SEMESTER, W1.SUBCLASSCD, W3.SPECIAL_GROUP_CD,";
        if ("1" == $useCurriculumcd) {
            $query .= "    W1.CLASSCD, ";
            $query .= "    W1.CURRICULUM_CD, ";
            $query .= "    W1.SCHOOL_KIND, ";
        }
        $query .= "            INT(W2.MINUTES) * W1.MLESSON AS LESSON_MINUTES, ";
        $query .= "            INT(W2.MINUTES) * W1.ABSENT1 AS ABSENT_MINUTES, ";
        $query .= "            INT(W2.MINUTES) * (W1.LATE + W1.EARLY) AS LATE_MINUTES ";
        $query .= "     FROM   SCH_ATTEND_SUM W1 ";
        $query .= "     INNER JOIN ATTEND_SUBCLASS_SPECIAL_DAT W2 ON ";
        $query .= "       W2.YEAR = '".$year."' ";
        if ("1" == $useCurriculumcd) {
            $query .= "    AND W1.CLASSCD = W2.CLASSCD ";
            $query .= "    AND W1.CURRICULUM_CD = W2.CURRICULUM_CD ";
            $query .= "    AND W1.SCHOOL_KIND = W2.SCHOOL_KIND ";
        }
        $query .= "       AND W1.SUBCLASSCD = W2.SUBCLASSCD ";
        $query .= "     INNER JOIN ATTEND_SUBCLASS_SPECIAL_MST W3 ON ";
        $query .= "       W3.SPECIAL_GROUP_CD = W2.SPECIAL_GROUP_CD ";

        $query .= " ) ,SCH_ATTEND_UNI AS ( ";
        $query .= " SELECT ";
        $query .= "    TT0.SCHREGNO, ";
        $query .= "    TT0.SEMESTER, ";
        if ("1" == $useCurriculumcd) {
            $query .= "    TT0.CLASSCD, ";
            $query .= "    TT0.CURRICULUM_CD, ";
            $query .= "    TT0.SCHOOL_KIND, ";
        }
        $query .= "    TT0.SUBCLASSCD, ";
        $query .= "    SUM(TT0.LESSON) AS LESSON, ";
        $query .= "    SUM(TT0.MLESSON) AS MLESSON, ";
        $query .= "    SUM(TT0.OFFDAYS) AS OFFDAYS, ";
        $query .= "    SUM(TT0.SUSPEND) AS SUSPEND, ";
        $query .= "    SUM(TT0.MOURNING) AS MOURNING, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "    SUM(TT0.VIRUS) AS VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "    SUM(TT0.KOUDOME) AS KOUDOME, ";
        }
        $query .= "    SUM(TT0.NURSEOFF) AS NURSEOFF, ";
        $query .= "    SUM(TT0.KOUKETSU) AS KOUKETSU, ";
        $query .= "    SUM(TT0.LATE) AS LATE, ";
        $query .= "    SUM(TT0.EARLY) AS EARLY, ";
        $query .= "    SUM(TT0.LATE) + SUM(TT0.EARLY) AS LATE_EARLY, ";
        $query .= "    SUM(TT0.ABSENT1) AS ABSENT1, ";
        $query .= "    L1.SPECIAL_GROUP_CD, ";
        $query .= "    SUM(VALUE(L1.LESSON_MINUTES, 0)) AS SPECIAL_LESSON_MINUTES,";
        $query .= "    SUM(VALUE(L1.ABSENT_MINUTES, 0)) AS SPECIAL_ABSENT_MINUTES,";
        $query .= "    SUM(VALUE(L1.LATE_MINUTES, 0)) AS SPECIAL_LATE_MINUTES";
        $query .= " FROM ";
        $query .= "    SCH_ATTEND_SUM TT0 ";
        $query .= "       LEFT JOIN ATTEND_SPECIAL L1 ON TT0.SCHREGNO = L1.SCHREGNO AND TT0.SEMESTER = L1.SEMESTER AND TT0.SUBCLASSCD = L1.SUBCLASSCD ";
        if ("1" == $useCurriculumcd) {
            $query .= "      AND TT0.CLASSCD = L1.CLASSCD ";
            $query .= "      AND TT0.CURRICULUM_CD = L1.CURRICULUM_CD ";
            $query .= "      AND TT0.SCHOOL_KIND = L1.SCHOOL_KIND ";
        }
        $query .= " GROUP BY ";
        $query .= "    TT0.SCHREGNO, TT0.SEMESTER, TT0.SUBCLASSCD, ";
        if ("1" == $useCurriculumcd) {
            $query .= "    TT0.CLASSCD, ";
            $query .= "    TT0.CURRICULUM_CD, ";
            $query .= "    TT0.SCHOOL_KIND, ";
        }
        $query .= "    L1.SPECIAL_GROUP_CD ";

        if ($knjSchoolMst["ABSENT_COV"] && "0" != $knjSchoolMst["ABSENT_COV"]) {
            $query .= "), ATTEND_B0 AS(";
            $query .= "     SELECT ";
            $query .= "          SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "          SUBCLASSCD, ";
            $query .= "          SEMESTER, ";
            $query .= "          VALUE(SUM(LATE_EARLY),0) ".$knjSchoolMst["ABSENT_COV_LATE"]." AS ABSENT, ";
            $query .= "          DECIMAL(VALUE(SUM(LATE_EARLY),0), 4, 1) ".$knjSchoolMst["ABSENT_COV_LATE"]." AS ABSENT_FLOAT ";
            $query .= "      FROM ";
            $query .= "          SCH_ATTEND_UNI T1 ";
            $query .= "      GROUP BY ";
            $query .= "          SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "          SUBCLASSCD, ";
            $query .= "          SEMESTER ";
        }
        $query .= "), ATTEND_B AS(";
        if ("1" == $knjSchoolMst["ABSENT_COV"] || "3" == $knjSchoolMst["ABSENT_COV"]) {
            $query .= "    SELECT ";
            $query .= "        SCHREGNO, ";
            $query .= "        SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "        SUBCLASSCD, ";
            if ("3" == $knjSchoolMst["ABSENT_COV"]) {
                $query .= "        SUM(ABSENT_FLOAT) AS PENALTY_ABSENT ";
            } else {
                $query .= "        SUM(ABSENT) AS PENALTY_ABSENT ";
            }
            $query .= "    FROM ATTEND_B0 T1 ";
            $query .= "    GROUP BY ";
            $query .= "        SCHREGNO, ";
            $query .= "        SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "        SUBCLASSCD ";
            $query .= " UNION ALL ";
            $query .= "    SELECT ";
            $query .= "        SCHREGNO, ";
            $query .= "        '9' AS SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "        SUBCLASSCD, ";
            if ("3" == $knjSchoolMst["ABSENT_COV"]) {
                $query .= "        SUM(ABSENT_FLOAT) AS PENALTY_ABSENT ";
            } else {
                $query .= "        SUM(ABSENT) AS PENALTY_ABSENT ";
            }
            $query .= "    FROM ATTEND_B0 T1 ";
            $query .= "    GROUP BY ";
            $query .= "        SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "        SUBCLASSCD ";
        } else if ("2" == $knjSchoolMst["ABSENT_COV"] || "4" == $knjSchoolMst["ABSENT_COV"]) {
            $query .= "    SELECT ";
            $query .= "        T1.SCHREGNO, ";
            $query .= "        T1.SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      T1.CLASSCD, ";
                $query .= "      T1.CURRICULUM_CD, ";
                $query .= "      T1.SCHOOL_KIND, ";
            }
            $query .= "        T1.SUBCLASSCD, ";
            $query .= "        T2.ABSENT_SEM AS PENALTY_ABSENT ";
            $query .= "    FROM ";
            $query .= "        (SELECT ";
            $query .= "             SCHREGNO, ";
            $query .= "             SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "             SUBCLASSCD ";
            $query .= "         FROM ";
            $query .= "             SCH_ATTEND_UNI T1 ";
            $query .= "         GROUP BY ";
            $query .= "             SCHREGNO, ";
            $query .= "             SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "             SUBCLASSCD ";
            $query .= "        )T1, ";
            $query .= "        (SELECT ";
            $query .= "             SCHREGNO, ";
            $query .= "             SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "             SUBCLASSCD, ";
            $query .= "             ABSENT AS ABSENT_SEM ";
            $query .= "         FROM ";
            $query .= "             (SELECT ";
            $query .= "                  SCHREGNO, ";
            $query .= "                  SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "                  SUBCLASSCD, ";
            if ("4" == $knjSchoolMst["ABSENT_COV"]) {
                $query .= "                  DECIMAL(VALUE(SUM(LATE_EARLY),0), 4, 1) ".$knjSchoolMst["ABSENT_COV_LATE"]." AS ABSENT ";
            } else {
                $query .= "                  VALUE(SUM(LATE_EARLY),0) ".$knjSchoolMst["ABSENT_COV_LATE"]." AS ABSENT ";
            }
            $query .= "              FROM ";
            $query .= "                  SCH_ATTEND_UNI T1 ";
            $query .= "              GROUP BY ";
            $query .= "                  SCHREGNO, ";
            $query .= "                  SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "                  SUBCLASSCD ";
            $query .= "              ) T1 ";
            $query .= "         GROUP BY ";
            $query .= "             SCHREGNO, ";
            $query .= "             SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "             SUBCLASSCD, ";
            $query .= "             ABSENT ";
            $query .= "        ) T2 ";
            $query .= "    WHERE ";
            $query .= "        T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "        AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "        AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
            if ("1" == $useCurriculumcd) {
                $query .= "        AND T1.CLASSCD = T2.CLASSCD ";
                $query .= "        AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
                $query .= "        AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            }
            $query .= " UNION ALL ";
            $query .= "    SELECT ";
            $query .= "        T1.SCHREGNO, ";
            $query .= "        '9' AS SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "        T1.SUBCLASSCD, ";
            if ("4" == $knjSchoolMst["ABSENT_COV"]) {
                $query .= "        DECIMAL(VALUE(SUM(LATE_EARLY),0), 4, 1) ".$knjSchoolMst["ABSENT_COV_LATE"]." AS PENALTY_ABSENT ";
            } else {
                $query .= "        VALUE(SUM(LATE_EARLY),0) ".$knjSchoolMst["ABSENT_COV_LATE"]." AS PENALTY_ABSENT ";
            }
            $query .= "    FROM ";
            $query .= "        SCH_ATTEND_UNI T1 ";
            $query .= "    GROUP BY  ";
            $query .= "        T1.SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "        T1.SUBCLASSCD ";
        } else if ("5" == $knjSchoolMst["ABSENT_COV"]) {
            $query .= "    SELECT ";
            $query .= "        T1.SCHREGNO, ";
            $query .= "        T1.SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      T1.CLASSCD, ";
                $query .= "      T1.CURRICULUM_CD, ";
                $query .= "      T1.SCHOOL_KIND, ";
            }
            $query .= "        T1.SUBCLASSCD, ";
            $query .= "        T2.ABSENT_SEM AS PENALTY_ABSENT ";
            $query .= "    FROM ";
            $query .= "        (SELECT ";
            $query .= "             SCHREGNO, ";
            $query .= "             SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "             SUBCLASSCD ";
            $query .= "         FROM ";
            $query .= "             SCH_ATTEND_UNI T1 ";
            $query .= "         GROUP BY ";
            $query .= "             SCHREGNO, ";
            $query .= "             SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "             SUBCLASSCD ";
            $query .= "        )T1, ";
            $query .= "        (SELECT ";
            $query .= "             SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "             SUBCLASSCD, ";
            $query .= "             SEMESTER, ";
            $query .= "             ABSENT AS ABSENT_SEM ";
            $query .= "         FROM ";
            $query .= "             (SELECT ";
            $query .= "                  SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "                  SUBCLASSCD, ";
            $query .= "                  SEMESTER, ";
            $query .= "                  VALUE(SUM(LATE_EARLY),0) ".$knjSchoolMst["ABSENT_COV_LATE"];
            $query .= "                   +  (CASE WHEN MOD(VALUE(SUM(LATE_EARLY),0) , ".$knjSchoolMst["ABSENT_COV_LATE"].") >= ";
            $query .= "                     ".$knjSchoolMst["AMARI_KURIAGE"]." THEN 1 ELSE 0 END)AS ABSENT ";
            $query .= "              FROM ";
            $query .= "                  SCH_ATTEND_UNI T1 ";
            $query .= "              GROUP BY ";
            $query .= "                  SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "                  SEMESTER, ";
            $query .= "                  SUBCLASSCD ";
            $query .= "              ) T1 ";
            $query .= "         GROUP BY ";
            $query .= "             SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "             SEMESTER, ";
            $query .= "             SUBCLASSCD, ";
            $query .= "             ABSENT ";
            $query .= "        ) T2 ";
            $query .= "    WHERE ";
            $query .= "        T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "        AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "        AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
            if ("1" == $useCurriculumcd) {
                $query .= "        AND T1.CLASSCD = T2.CLASSCD ";
                $query .= "        AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
                $query .= "        AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            }
            $query .= " UNION ALL ";
            $query .= "    SELECT ";
            $query .= "        T1.SCHREGNO, ";
            $query .= "        '9' AS SEMESTER, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "        T1.SUBCLASSCD, ";
            $query .= "        VALUE(SUM(LATE_EARLY),0) ".$knjSchoolMst["ABSENT_COV_LATE"];
            $query .= "          +  (CASE WHEN MOD(VALUE(SUM(LATE_EARLY),0) , ".$knjSchoolMst["ABSENT_COV_LATE"].") >= ";
            $query .= "            ".$knjSchoolMst["AMARI_KURIAGE"]." THEN 1 ELSE 0 END)AS PENALTY_ABSENT ";
            $query .= "    FROM ";
            $query .= "        SCH_ATTEND_UNI T1 ";
            $query .= "    GROUP BY  ";
            $query .= "        T1.SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "        T1.SUBCLASSCD ";
        } else {
            $query .= "    SELECT ";
            $query .= "        SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "        SUBCLASSCD, ";
            $query .= "        SEMESTER, ";
            $query .= "        0 AS PENALTY_ABSENT ";
            $query .= "    FROM ";
            $query .= "        SCH_ATTEND_UNI T1 ";
            $query .= "    GROUP BY ";
            $query .= "        SCHREGNO, ";
            if ("1" == $useCurriculumcd) {
                $query .= "      CLASSCD, ";
                $query .= "      CURRICULUM_CD, ";
                $query .= "      SCHOOL_KIND, ";
            }
            $query .= "        SUBCLASSCD, ";
            $query .= "        SEMESTER ";
        }

        $query .= " ), ATTEND_C AS(";
        $query .= "    SELECT ";
        $query .= "        T1.SCHREGNO, ";
        if ("1" == $useCurriculumcd) {
            $query .= "      T1.CLASSCD, ";
            $query .= "      T1.CURRICULUM_CD, ";
            $query .= "      T1.SCHOOL_KIND, ";
        }
        $query .= "        T1.SUBCLASSCD, ";
        $query .= "        T1.SEMESTER, ";
        $query .= "        SUM(PENALTY_ABSENT) AS PENALTY_ABSENT, ";
        $query .= "        SUM(PENALTY_ABSENT) * ".($knjSchoolMst["ABSENT_COV_LATE"] ? $knjSchoolMst["ABSENT_COV_LATE"] : "0")." AS PENALTY_LATE_EARLY, ";
        $query .= "        SUM(INT(VALUE(T2.MINUTES, '0')) * INT(PENALTY_ABSENT)) AS PENALTY_SPECIAL_ABSENT_MINUTES ";
        $query .= "    FROM ";
        $query .= "        ATTEND_B T1 ";
        $query .= "        LEFT JOIN ATTEND_SUBCLASS_SPECIAL_DAT T2 ON T2.YEAR = '".$year."' ";
        $query .= "            AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "    GROUP BY ";
        $query .= "        T1.SCHREGNO, ";
        if ("1" == $useCurriculumcd) {
            $query .= "      T1.CLASSCD, ";
            $query .= "      T1.CURRICULUM_CD, ";
            $query .= "      T1.SCHOOL_KIND, ";
        }
        $query .= "        T1.SUBCLASSCD, ";
        $query .= "        T1.SEMESTER ";

        $query .= " ) ,ATTEND_REPLACE AS ( ";
        $query .= " SELECT ";
        $query .= "    TT2.SCHREGNO, TT2.SEMESTER, TT0.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        if ("1" == $useCurriculumcd) {
            $query .= "      TT0.COMBINED_CLASSCD AS CLASSCD, ";
            $query .= "      TT0.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ";
            $query .= "      TT0.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ";
        }
        $query .= "    SUM(VALUE(TT2.LESSON,0)) AS LESSON, ";
        $query .= "    SUM(VALUE(TT2.MLESSON,0)) AS MLESSON, ";
        $query .= "    SUM(VALUE(TT2.OFFDAYS,0)) AS OFFDAYS, ";
        $query .= "    SUM(VALUE(TT2.SUSPEND,0)) AS SUSPEND, ";
        $query .= "    SUM(VALUE(TT2.MOURNING,0)) AS MOURNING, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "    SUM(VALUE(TT2.VIRUS,0)) AS VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "    SUM(VALUE(TT2.KOUDOME,0)) AS KOUDOME, ";
        }
        $query .= "    SUM(VALUE(TT2.NURSEOFF,0)) AS NURSEOFF, ";
        $query .= "    SUM(VALUE(TT2.KOUKETSU,0)) AS KOUKETSU, ";
        $query .= "    SUM(VALUE(TT2.LATE,0)) AS LATE, ";
        $query .= "    SUM(VALUE(TT2.EARLY,0)) AS EARLY, ";
        $query .= "    SUM(VALUE(TT2.ABSENT1, 0)) AS RAW_REPLACED_ABSENT, ";
        $query .= "    SUM(VALUE(TT2.ABSENT1, 0) + VALUE(TT3.PENALTY_ABSENT, 0)) AS REPLACED_ABSENT ";
        $query .= " FROM ";
        $query .= "    SUBCLASS_REPLACE_COMBINED_DAT TT0 ";
        $query .= "    INNER JOIN SCH_ATTEND_UNI TT2 ON ";
        $query .= "       TT0.ATTEND_SUBCLASSCD = TT2.SUBCLASSCD";
        if ("1" == $useCurriculumcd) {
            $query .= "   AND TT0.ATTEND_CLASSCD = TT2.CLASSCD ";
            $query .= "   AND TT0.ATTEND_CURRICULUM_CD = TT2.CURRICULUM_CD ";
            $query .= "   AND TT0.ATTEND_SCHOOL_KIND = TT2.SCHOOL_KIND ";
        }
        $query .= "    LEFT JOIN ATTEND_B TT3 ON ";
        $query .= "       TT2.SCHREGNO = TT3.SCHREGNO ";
        $query .= "       AND TT2.SEMESTER = TT3.SEMESTER ";
        $query .= "       AND TT0.ATTEND_SUBCLASSCD= TT3.SUBCLASSCD ";
        if ("1" == $useCurriculumcd) {
            $query .= "   AND TT0.ATTEND_CLASSCD = TT3.CLASSCD ";
            $query .= "   AND TT0.ATTEND_CURRICULUM_CD = TT3.CURRICULUM_CD ";
            $query .= "   AND TT0.ATTEND_SCHOOL_KIND = TT3.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "    TT0.YEAR = '".$year."' ";
        $query .= " GROUP BY ";
        $query .= "    TT2.SCHREGNO, TT2.SEMESTER, ";
        if ("1" == $useCurriculumcd) {
            $query .= "      TT0.COMBINED_CLASSCD, ";
            $query .= "      TT0.COMBINED_CURRICULUM_CD, ";
            $query .= "      TT0.COMBINED_SCHOOL_KIND, ";
        }
        $query .= "    TT0.COMBINED_SUBCLASSCD ";
        $query .= " ), MAIN AS (   ";

        $query .= " SELECT ";
        $query .= "    TT0.SCHREGNO, ";
        $query .= "    TT0.SEMESTER, ";
        if ("1" == $useCurriculumcd) {
            $query .= "      TT0.CLASSCD, ";
            $query .= "      TT0.CURRICULUM_CD, ";
            $query .= "      TT0.SCHOOL_KIND, ";
        }
        $query .= "    TT0.SUBCLASSCD, ";
        $query .= "    TT0.LESSON, ";
        $query .= "    TT0.MLESSON, ";
        $query .= "    TT0.OFFDAYS, ";
        $query .= "    TT0.SUSPEND, ";
        $query .= "    TT0.MOURNING, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "    TT0.VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "    TT0.KOUDOME, ";
        }
        $query .= "    TT0.NURSEOFF, ";
        $query .= "    TT0.KOUKETSU AS ABSENT, ";
        $query .= "    TT0.LATE, ";
        $query .= "    TT0.EARLY, ";
        $query .= "    VALUE(TT1.PENALTY_LATE_EARLY, 0) AS PENALTY_LATE_EARLY, ";
        $query .= "    (CASE WHEN VALUE(TT1.PENALTY_LATE_EARLY, 0) = 0 OR TT0.LATE >= VALUE(TT1.PENALTY_LATE_EARLY, 0) THEN TT0.LATE ";
        $query .= "          WHEN TT0.LATE <= VALUE(TT1.PENALTY_LATE_EARLY, 0) THEN 0 ";
        $query .= "          ELSE TT0.LATE - VALUE(TT1.PENALTY_LATE_EARLY, 0) END) AS LATE2, ";
        $query .= "    (CASE WHEN VALUE(TT1.PENALTY_LATE_EARLY, 0) = 0 THEN TT0.EARLY ";
        $query .= "          WHEN TT0.LATE + TT0.EARLY <= VALUE(TT1.PENALTY_LATE_EARLY, 0) THEN 0 ";
        $query .= "          ELSE TT0.LATE + TT0.EARLY - VALUE(TT1.PENALTY_LATE_EARLY, 0) END) AS EARLY2, ";
        $query .= "    TT0.ABSENT1 AS SICK1, ";
        $query .= "    TT0.ABSENT1 + VALUE(TT1.PENALTY_ABSENT, 0) AS SICK2, ";
        $query .= "    0 AS RAW_REPLACED_SICK, ";
        $query .= "    0 AS REPLACED_SICK, ";
        $query .= "    TT0.SPECIAL_GROUP_CD, ";
        $query .= "    TT0.SPECIAL_LESSON_MINUTES, ";
        $query .= "    TT0.SPECIAL_ABSENT_MINUTES + VALUE(TT1.PENALTY_SPECIAL_ABSENT_MINUTES,0) AS SPECIAL_SICK_MINUTES1, ";
        $query .= "    TT0.SPECIAL_ABSENT_MINUTES AS SPECIAL_SICK_MINUTES2, ";
        $query .= "    TT0.SPECIAL_ABSENT_MINUTES + TT0.SPECIAL_LATE_MINUTES AS SPECIAL_SICK_MINUTES3 ";
        $query .= " FROM ";
        $query .= "    SCH_ATTEND_UNI TT0 ";
        $query .= "    LEFT JOIN ATTEND_C TT1 ON ";
        $query .= "       TT0.SCHREGNO = TT1.SCHREGNO ";
        $query .= "       AND TT0.SEMESTER = TT1.SEMESTER ";
        if ("1" == $useCurriculumcd) {
            $query .= "   AND TT0.CLASSCD = TT1.CLASSCD ";
            $query .= "   AND TT0.CURRICULUM_CD = TT1.CURRICULUM_CD ";
            $query .= "   AND TT0.SCHOOL_KIND = TT1.SCHOOL_KIND ";
        }
        $query .= "       AND TT0.SUBCLASSCD = TT1.SUBCLASSCD ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "    TT0.SCHREGNO, ";
        $query .= "    TT0.SEMESTER, ";
        if ("1" == $useCurriculumcd) {
            $query .= "      TT0.CLASSCD, ";
            $query .= "      TT0.CURRICULUM_CD, ";
            $query .= "      TT0.SCHOOL_KIND, ";
        }
        $query .= "    TT0.SUBCLASSCD, ";
        $query .= "    VALUE(TT0.LESSON, 0) AS LESSON, ";
        $query .= "    VALUE(TT0.MLESSON, 0) AS MLESSON, ";
        $query .= "    VALUE(TT0.OFFDAYS, 0) AS OFFDAYS, ";
        $query .= "    VALUE(TT0.SUSPEND, 0) AS SUSPEND, ";
        $query .= "    VALUE(TT0.MOURNING, 0) AS MOURNING, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "    VALUE(TT0.VIRUS, 0) AS VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "    VALUE(TT0.KOUDOME, 0) AS KOUDOME, ";
        }
        $query .= "    VALUE(TT0.NURSEOFF, 0) AS NURSEOFF, ";
        $query .= "    VALUE(TT0.KOUKETSU, 0) AS ABSENT, ";
        $query .= "    VALUE(TT0.LATE, 0) AS LATE, ";
        $query .= "    VALUE(TT0.EARLY, 0) AS EARLY, ";
        $query .= "    0 AS PENALTY_LATE_EARLY, ";
        $query .= "    VALUE(TT0.LATE, 0) AS LATE2, ";
        $query .= "    VALUE(TT0.EARLY, 0) AS EARLY2, ";
        $query .= "    0 AS SICK1, ";
        $query .= "    0 AS SICK2, ";
        $query .= "    VALUE(TT0.RAW_REPLACED_ABSENT, 0) AS RAW_REPLACED_SICK, ";
        $query .= "    VALUE(TT0.REPLACED_ABSENT, 0) AS REPLACED_SICK, ";
        $query .= "    CAST(NULL AS VARCHAR(4)) AS SPECIAL_GROUP_CD, ";
        $query .= "    0 AS SPECIAL_LESSON_MINUTES, ";
        $query .= "    0 AS SPECIAL_SICK_MINUTES1, ";
        $query .= "    0 AS SPECIAL_SICK_MINUTES2, ";
        $query .= "    0 AS SPECIAL_SICK_MINUTES3 ";
        $query .= " FROM ";
        $query .= "    ATTEND_REPLACE TT0 ";

        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "    TT0.SCHREGNO, ";
        $query .= "    '9' AS SEMESTER, ";
        if ("1" == $useCurriculumcd) {
            $query .= "      TT0.CLASSCD, ";
            $query .= "      TT0.CURRICULUM_CD, ";
            $query .= "      TT0.SCHOOL_KIND, ";
        }
        $query .= "    TT0.SUBCLASSCD, ";
        $query .= "    SUM(TT0.LESSON) AS LESSON, ";
        $query .= "    SUM(TT0.MLESSON) AS MLESSON, ";
        $query .= "    SUM(TT0.OFFDAYS) AS OFFDAYS, ";
        $query .= "    SUM(TT0.SUSPEND) AS SUSPEND, ";
        $query .= "    SUM(TT0.MOURNING) AS MOURNING, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "    SUM(TT0.VIRUS) AS VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "    SUM(TT0.KOUDOME) AS KOUDOME, ";
        }
        $query .= "    SUM(TT0.NURSEOFF) AS NURSEOFF, ";
        $query .= "    SUM(TT0.KOUKETSU) AS ABSENT, ";
        $query .= "    SUM(TT0.LATE) AS LATE, ";
        $query .= "    SUM(TT0.EARLY) AS EARLY, ";
        $query .= "    VALUE(TT1.PENALTY_LATE_EARLY, 0) AS PENALTY_LATE_EARLY, ";
        $query .= "    (CASE WHEN VALUE(TT1.PENALTY_LATE_EARLY, 0) = 0 THEN SUM(TT0.LATE) ";
        $query .= "          WHEN SUM(TT0.LATE) <= VALUE(TT1.PENALTY_LATE_EARLY, 0) THEN 0 ";
        $query .= "          ELSE SUM(TT0.LATE) - VALUE(TT1.PENALTY_LATE_EARLY, 0) END) AS LATE2, ";
        $query .= "    (CASE WHEN VALUE(TT1.PENALTY_LATE_EARLY, 0) = 0 OR SUM(TT0.LATE) >= VALUE(TT1.PENALTY_LATE_EARLY, 0) THEN SUM(TT0.EARLY) ";
        $query .= "          WHEN SUM(TT0.LATE) + SUM(TT0.EARLY) <= VALUE(TT1.PENALTY_LATE_EARLY, 0) THEN 0 ";
        $query .= "          ELSE SUM(TT0.LATE) + SUM(TT0.EARLY) - VALUE(TT1.PENALTY_LATE_EARLY, 0) END) AS EARLY2, ";
        $query .= "    SUM(TT0.ABSENT1) AS SICK1, ";
        $query .= "    SUM(TT0.ABSENT1) + VALUE(TT1.PENALTY_ABSENT, 0) AS SICK2, ";
        $query .= "    0 AS RAW_REPLACED_SICK, ";
        $query .= "    0 AS REPLACED_SICK, ";
        $query .= "    TT0.SPECIAL_GROUP_CD, ";
        $query .= "    SUM(TT0.SPECIAL_LESSON_MINUTES) AS SPECIAL_LESSON_MINUTES, ";
        $query .= "    SUM(TT0.SPECIAL_ABSENT_MINUTES) + VALUE(TT1.PENALTY_SPECIAL_ABSENT_MINUTES, 0) AS SPECIAL_SICK_MINUTES1, ";
        $query .= "    SUM(TT0.SPECIAL_ABSENT_MINUTES) AS SPECIAL_SICK_MINUTES2, ";
        $query .= "    SUM(TT0.SPECIAL_ABSENT_MINUTES + TT0.SPECIAL_LATE_MINUTES)   AS SPECIAL_SICK_MINUTES3 ";
        $query .= " FROM ";
        $query .= "    SCH_ATTEND_UNI TT0 ";
        $query .= "    LEFT JOIN ATTEND_C TT1 ON ";
        $query .= "       TT0.SCHREGNO = TT1.SCHREGNO ";
        $query .= "       AND '9' = TT1.SEMESTER ";
        if ("1" == $useCurriculumcd) {
            $query .= "   AND TT0.CLASSCD = TT1.CLASSCD ";
            $query .= "   AND TT0.CURRICULUM_CD = TT1.CURRICULUM_CD ";
            $query .= "   AND TT0.SCHOOL_KIND = TT1.SCHOOL_KIND ";
        }
        $query .= "       AND TT0.SUBCLASSCD = TT1.SUBCLASSCD ";
        $query .= " GROUP BY ";
        $query .= "    TT0.SCHREGNO, TT0.SUBCLASSCD, TT0.SPECIAL_GROUP_CD, ";
        if ("1" == $useCurriculumcd) {
            $query .= "      TT0.CLASSCD, ";
            $query .= "      TT0.CURRICULUM_CD, ";
            $query .= "      TT0.SCHOOL_KIND, ";
        }
        $query .= "    TT1.PENALTY_ABSENT, TT1.PENALTY_LATE_EARLY, VALUE(TT1.PENALTY_SPECIAL_ABSENT_MINUTES, 0) ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "    TT0.SCHREGNO, ";
        $query .= "    '9' AS SEMESTER, ";
        if ("1" == $useCurriculumcd) {
            $query .= "      TT0.CLASSCD, ";
            $query .= "      TT0.CURRICULUM_CD, ";
            $query .= "      TT0.SCHOOL_KIND, ";
        }
        $query .= "    TT0.SUBCLASSCD, ";
        $query .= "    SUM(TT0.LESSON) AS LESSON, ";
        $query .= "    SUM(TT0.MLESSON) AS MLESSON, ";
        $query .= "    SUM(TT0.OFFDAYS) AS OFFDAYS, ";
        $query .= "    SUM(TT0.SUSPEND) AS SUSPEND, ";
        $query .= "    SUM(TT0.MOURNING) AS MOURNING, ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "    SUM(TT0.VIRUS) AS VIRUS, ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "    SUM(TT0.KOUDOME) AS KOUDOME, ";
        }
        
        $query .= "    SUM(TT0.NURSEOFF) AS NURSEOFF, ";
        $query .= "    SUM(TT0.KOUKETSU) AS ABSENT, ";
        $query .= "    SUM(TT0.LATE) AS LATE, ";
        $query .= "    SUM(TT0.EARLY) AS EARLY, ";
        $query .= "    0 AS PENALTY_LATE_EARLY, ";
        $query .= "    SUM(TT0.LATE) AS LATE2, ";
        $query .= "    SUM(TT0.EARLY) AS EARLY2, ";
        $query .= "    0 AS SICK1, ";
        $query .= "    0 AS SICK2, ";
        $query .= "    SUM(TT0.RAW_REPLACED_ABSENT) AS RAW_REPLACED_SICK, ";
        $query .= "    SUM(TT0.REPLACED_ABSENT) AS REPLACED_SICK, ";
        $query .= "    CAST(NULL AS VARCHAR(4)) AS SPECIAL_GROUP_CD, ";
        $query .= "    0 AS SPECIAL_LESSON_MINUTES, ";
        $query .= "    0 AS SPECIAL_SICK_MINUTES1, ";
        $query .= "    0 AS SPECIAL_SICK_MINUTES2, ";
        $query .= "    0 AS SPECIAL_SICK_MINUTES3 ";
        $query .= " FROM ";
        $query .= "    ATTEND_REPLACE TT0 ";
        $query .= " GROUP BY ";
        $query .= "    TT0.SCHREGNO, ";
        if ("1" == $useCurriculumcd) {
            $query .= "      TT0.CLASSCD, ";
            $query .= "      TT0.CURRICULUM_CD, ";
            $query .= "      TT0.SCHOOL_KIND, ";
        }
        $query .= "    TT0.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SEMESTER, ";
        if ("1" == $useCurriculumcd) {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ";
        }
        $query .= "     SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     LESSON, ";
        $query .= "     MLESSON, ";
        $query .= "     OFFDAYS, ";
        $query .= "     (SUSPEND ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "         + VIRUS ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "         + KOUDOME ";
        }
        $query .= "     ) AS SUSPEND, ";
        $query .= "     MOURNING, ";
        $query .= "     NURSEOFF, ";
        $query .= "     ABSENT, ";
        $query .= "     LATE, ";
        $query .= "     EARLY, ";
        $query .= "     PENALTY_LATE_EARLY, ";
        $query .= "     LATE2, ";
        $query .= "     EARLY2, ";
        $query .= "     SICK1, ";
        $query .= "     SICK2, ";
        $query .= "     RAW_REPLACED_SICK, ";
        $query .= "     REPLACED_SICK, ";
        $query .= "     SPECIAL_GROUP_CD, ";
        $query .= "     SPECIAL_LESSON_MINUTES, ";
        $query .= "     SPECIAL_SICK_MINUTES1, ";
        $query .= "     SPECIAL_SICK_MINUTES2, ";
        $query .= "     SPECIAL_SICK_MINUTES3 ";
        $query .= " FROM ";
        $query .= "     MAIN ";

        return $query;
    }
}
?>
