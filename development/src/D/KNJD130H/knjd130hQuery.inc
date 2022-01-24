<?php

require_once('for_php7.php');

class knjd130hQuery extends Query {

    //学期コンボ取得
    function getSemesterQuery()
    {
        $query  = "SELECT SEMESTER, SEMESTERNAME ";
        $query .= "  FROM SEMESTER_MST ";
        $query .= " WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER <> '9' ";
        $query .= "ORDER BY SEMESTER ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($schregno, $semester)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT schregno, totalstudytime, communication ";
        $query .= "   FROM hreportremark_dat ";
        $query .= "  WHERE year     = '".CTRL_YEAR."'";
        $query .= "    AND semester = '".$semester."' ";
        $query .= "    AND schregno = '".$schregno."'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields,$schregno, $semester)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]                = CTRL_YEAR;
        $data["SEMESTER"][TEXT]            = $semester;
        $data["SCHREGNO"][TEXT]            = $schregno;
        $data["COMMUNICATION"][TEXT]       = $fields["COMMUNICATION"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";

        $db->query(Query::insertSQL($data, "hreportremark_dat"));

        Query::dbCheckIn($db);
        return ;
    }

   //DELETE
   function &getDeleteQuery($schregno, $semester)
   {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM hreportremark_dat ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".$semester."' ";
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
   }

    //学籍データ参照---SubForm1
    function getGrade($model)
    {
        $query  = "SELECT GRADE, HR_CLASS ";
        $query .= "  FROM SCHREG_REGD_DAT ";
        $query .= " WHERE YEAR   = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."' AND SCHREGNO = '".$model->schregno."'";

        return $query;
    }

    //遅刻何回で欠課とするかの指数取得---SubForm1
    function getScAbsentCov()
    {
        return "SELECT absent_cov,absent_cov_late FROM school_mst WHERE year = '".CTRL_YEAR."'";
    }

    //成績データ参照---SubForm1
    function getRecord($model, $isodate, $isomonth, $cntmonth, $grade, $absent_cov, $absent_cov_late,$knjSchoolMst)
    {
        //学籍の表（生徒）
        $query  = "WITH SCHNO AS(";
        $query .=     "SELECT  T2.SCHREGNO, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, ";
        $query .=             "T2.COURSECD, T2.MAJORCD, T2.COURSECODE ";
        $query .=     "FROM    SCHREG_REGD_DAT T2 ";
        $query .=     "WHERE   T2.YEAR = '".CTRL_YEAR."' ";
        $query .=         "AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .=         "AND T2.SCHREGNO = '".$model->schregno."' ";
        $query .= ") ";

        //講座の表（生徒別・学期別）
        $query .= ", HR_CHAIR_A AS(";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=     "SELECT  S1.SCHREGNO, S2.CLASSCD, S2.SCHOOL_KIND, S2.CURRICULUM_CD, S2.SUBCLASSCD ";
        } else {
            $query .=     "SELECT  S1.SCHREGNO,S2.SUBCLASSCD ";
        }
        $query .=     "FROM    CHAIR_STD_DAT S1, ";
        $query .=             "CHAIR_DAT S2 ";
        $query .=     "WHERE   S1.YEAR = '".CTRL_YEAR."' ";
        $query .=          "AND S1.SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .=         "AND S2.YEAR  = '".CTRL_YEAR."' ";
        $query .=         "AND S2.SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .=         "AND S2.SEMESTER = S1.SEMESTER ";
        $query .=         "AND S2.CHAIRCD = S1.CHAIRCD ";
        $query .=         "AND EXISTS( SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=         "AND (SUBSTR(S2.SUBCLASSCD,1,2) BETWEEN '01' AND '89' OR SUBSTR(S2.SUBCLASSCD,1,2) = '90') ";
            $query .=     "GROUP BY S1.SCHREGNO, S2.CLASSCD, S2.SCHOOL_KIND, S2.CURRICULUM_CD, S2.SUBCLASSCD ";
        } else {
            $query .=         "AND (SUBSTR(SUBCLASSCD,1,2) BETWEEN '01' AND '89' OR SUBSTR(SUBCLASSCD,1,2) = '90') ";
            $query .=     "GROUP BY S1.SCHREGNO,S2.SUBCLASSCD ";
        }
        $query .= ") ";

        //講座の表（生徒）
        $query .= ", CHAIR_A AS(";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=     "SELECT  SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ";
        } else {
            $query .=     "SELECT  SCHREGNO,SUBCLASSCD ";
        }
        $query .=     "FROM    HR_CHAIR_A S1 ";
        $query .=     "WHERE   EXISTS( SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ";
        $query .= ") ";

        //読替先科目の表（名簿無し）
        $query .= ", NOT_CHAIR_REPLACE AS(";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=     "SELECT  S1.GRADING_CLASSCD, S1.GRADING_SCHOOL_KIND, S1.GRADING_CURRICULUM_CD, S1.GRADING_SUBCLASSCD ";
        } else {
            $query .=     "SELECT  S1.GRADING_SUBCLASSCD ";
        }
        $query .=     "FROM    SUBCLASS_REPLACE_DAT S1 ";
        $query .=     "INNER JOIN CHAIR_DAT S2 ON S2.YEAR = '".CTRL_YEAR."' ";
        $query .=                            "AND S2.SEMESTER <= '".CTRL_SEMESTER."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=                            "AND S2.CLASSCD        = S1.ATTEND_CLASSCD ";
            $query .=                            "AND S2.SCHOOL_KIND    = S1.ATTEND_SCHOOL_KIND ";
            $query .=                            "AND S2.CURRICULUM_CD  = S1.ATTEND_CURRICULUM_CD ";
        }
        $query .=                            "AND S2.SUBCLASSCD = S1.ATTEND_SUBCLASSCD ";
        $query .=     "WHERE   S1.YEAR = '".CTRL_YEAR."' ";
        $query .=         "AND S1.ANNUAL = '".$grade."' ";
        $query .=         "AND NOT EXISTS(SELECT  'X' FROM CHAIR_STD_DAT S3 ";
        $query .=                        "WHERE   S3.YEAR = '".CTRL_YEAR."' ";
        $query .=                            "AND S3.SEMESTER = S2.SEMESTER ";
        $query .=                            "AND S3.CHAIRCD = S2.CHAIRCD) ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=     "GROUP BY S1.GRADING_CLASSCD, S1.GRADING_SCHOOL_KIND, S1.GRADING_CURRICULUM_CD, S1.GRADING_SUBCLASSCD ";
        } else {
            $query .=     "GROUP BY S1.GRADING_SUBCLASSCD ";
        }
        $query .= ") ";

        //評価読替科目（生徒）
        $query .= ", SCHREG_SUBCLASS_REPLACE AS(";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=     "SELECT  S1.GRADING_CLASSCD, S1.GRADING_SCHOOL_KIND, S1.GRADING_CURRICULUM_CD, S1.GRADING_SUBCLASSCD, ";
            $query .=             "S1.ATTEND_CLASSCD, S1.ATTEND_SCHOOL_KIND, S1.ATTEND_CURRICULUM_CD, S1.ATTEND_SUBCLASSCD ";
        } else {
            $query .=     "SELECT  S1.GRADING_SUBCLASSCD, S1.ATTEND_SUBCLASSCD ";
        }
        $query .=     "FROM    SUBCLASS_REPLACE_DAT S1 ";
        $query .=     "WHERE   S1.YEAR = '".CTRL_YEAR."' ";
        $query .=         "AND S1.ANNUAL = '".$grade."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=         "AND (EXISTS(SELECT  'X' FROM CHAIR_A S2 ";
            $query .=                     "WHERE S2.CLASSCD = S1.GRADING_CLASSCD AND S2.SCHOOL_KIND = S1.GRADING_SCHOOL_KIND AND ";
            $query .=                           "S2.CURRICULUM_CD = S1.GRADING_CURRICULUM_CD AND S2.SUBCLASSCD = S1.GRADING_SUBCLASSCD) ";
            $query .=           "OR EXISTS(SELECT  'X' FROM NOT_CHAIR_REPLACE S2 ";
            $query .=                     "WHERE S2.GRADING_CLASSCD = S1.GRADING_CLASSCD AND S2.GRADING_SCHOOL_KIND = S1.GRADING_SCHOOL_KIND AND ";
            $query .=                           "S2.GRADING_CURRICULUM_CD = S1.GRADING_CURRICULUM_CD AND S2.GRADING_SUBCLASSCD = S1.GRADING_SUBCLASSCD)) ";
        } else {
            $query .=         "AND (EXISTS(SELECT  'X' FROM CHAIR_A S2 WHERE S2.SUBCLASSCD = S1.GRADING_SUBCLASSCD) ";
            $query .=           "OR EXISTS(SELECT  'X' FROM NOT_CHAIR_REPLACE S2 WHERE S2.GRADING_SUBCLASSCD = S1.GRADING_SUBCLASSCD)) ";
        }
        $query .= ") ";

        //成績明細データの表
        $query .= ",RECORD AS(";
        $query .=      "SELECT  SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=             "CLASSCD, ";
            $query .=             "SCHOOL_KIND, ";
            $query .=             "CURRICULUM_CD, ";
        }
        $query .=             "SUBCLASSCD, ";
        $query .=             "CHAIRCD, ";
        $query .=             "COMP_CREDIT, GET_CREDIT, ADD_CREDIT, ";
        $query .=              "SEM1_INTR_SCORE, ";
        $query .=             "SEM1_INTR_VALUE, ";
        $query .=              "SEM1_TERM_SCORE, ";
        $query .=             "SEM1_TERM_VALUE, ";
        $query .=              "CASE WHEN SEM1_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM1_VALUE)) ELSE NULL END AS SEM1_VALUE, ";  
        $query .=              "SEM2_INTR_SCORE, ";
        $query .=             "SEM2_INTR_VALUE, ";
        $query .=              "SEM2_TERM_SCORE, ";
        $query .=             "SEM2_TERM_VALUE, ";
        $query .=              "CASE WHEN SEM2_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM2_VALUE)) ELSE NULL END AS SEM2_VALUE, ";  
        $query .=              "SEM3_INTR_SCORE, ";
        $query .=             "SEM3_INTR_VALUE, ";
        $query .=              "SEM3_TERM_SCORE, ";
        $query .=             "SEM3_TERM_VALUE, ";
        $query .=              "CASE WHEN SEM3_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM3_VALUE)) ELSE NULL END AS SEM3_VALUE, ";  
        $query .=              "CASE WHEN GRAD_VALUE IS NOT NULL THEN RTRIM(CHAR(GRAD_VALUE)) ELSE NULL END AS GRAD_VALUE ";  
        $query .=      "FROM    RECORD_DAT T1 ";
        $query .=      "WHERE   YEAR = '".CTRL_YEAR."' ";
        $query .=         "AND EXISTS(SELECT  'X' FROM SCHNO T2 WHERE T2.SCHREGNO = T1.SCHREGNO) ";
        $query .=         "AND (SUBSTR(SUBCLASSCD,1,2) BETWEEN '01' AND '89' OR SUBSTR(SUBCLASSCD,1,2) = '90') ";
        $query .= ") ";

        // 時間割データの表
        $query .= ", T_SCH_CHR_DAT AS(";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=     "SELECT  T0.SCHREGNO, ";
            $query .=             "T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD, T2.SEMESTER, ";
            $query .=             "T1.EXECUTEDATE, T1.PERIODCD, T1.CHAIRCD, T1.DATADIV, ";
            $query .=             "(CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END) AS DI_CD, ";
            $query .=             "L1.MULTIPLY, ";
            $query .=             "(CASE WHEN T4.SCHREGNO IS NOT NULL THEN '1' ELSE '0' END) AS IS_OFFDAYS ";
        } else {
            $query .=     "SELECT  T0.SCHREGNO, ";
            $query .=     "T2.SUBCLASSCD, T2.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.CHAIRCD, T1.DATADIV, ";
            $query .=     "(CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END) AS DI_CD, ";
            $query .=     "L1.MULTIPLY, (CASE WHEN T4.SCHREGNO IS NOT NULL THEN '1' ELSE '0' END) AS IS_OFFDAYS ";
        }
        $query .=     "FROM    SCH_CHR_DAT T1 ";
        $query .=     "INNER JOIN SEMESTER_MST T3 ON T3.YEAR = T1.YEAR ";
        $query .=         "AND T3.SEMESTER <> '9' ";
        $query .=         "AND T1.EXECUTEDATE BETWEEN T3.SDATE AND T3.EDATE ";
        $query .=     "INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .=         "AND T2.SEMESTER = T3.SEMESTER ";
        $query .=         "AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .=     "INNER JOIN CHAIR_STD_DAT T0 ON T0.YEAR = T1.YEAR ";
        $query .=         "AND T0.SEMESTER = T2.SEMESTER ";
        $query .=         "AND T0.CHAIRCD = T2.CHAIRCD ";
        $query .=         "AND T1.EXECUTEDATE BETWEEN T0.APPDATE AND T0.APPENDDATE ";
        $query .=     "LEFT JOIN ATTEND_DAT T5 ON T5.SCHREGNO = T0.SCHREGNO ";
        $query .=         "AND T1.EXECUTEDATE = T5.ATTENDDATE ";
        $query .=         "AND T1.PERIODCD = T5.PERIODCD ";
        $query .=         "AND T1.CHAIRCD = T5.CHAIRCD ";
        $query .=     "LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T5.YEAR AND L1.DI_CD = T5.DI_CD ";
        $query .=     "LEFT JOIN SCHREG_TRANSFER_DAT T4 ON T4.SCHREGNO = T0.SCHREGNO ";
        $query .=         "AND T1.EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ";
        $query .=         "AND T4.TRANSFERCD = '2' ";
        $query .=     "WHERE   T1.YEAR = '".CTRL_YEAR."' ";
        $query .=         "AND T1.EXECUTEDATE > '".$isodate."' ";
        $query .=         "AND T1.EXECUTEDATE <= '".CTRL_DATE."' ";
        $query .=         "AND T3.SEMESTER <= '".CTRL_SEMESTER."' ";
        //                      学籍不在日を除外
        $query .=         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .=                        "WHERE   T6.SCHREGNO = T0.SCHREGNO ";
        $query .=                            "AND (( T6.ENT_DIV IN('4','5') AND T1.EXECUTEDATE < T6.ENT_DATE ) ";
        $query .=                              "OR ( T6.GRD_DIV IN('2','3') AND T1.EXECUTEDATE > T6.GRD_DATE )) ) ";
        //                      留学日、休学日を除外
        $query .=         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ";
        $query .=                        "WHERE   T7.SCHREGNO = T0.SCHREGNO ";
        $query .=                            "AND T7.TRANSFERCD <> '2' AND T1.EXECUTEDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T0.SCHREGNO ";
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
        $query .= "                       L4.SCHREGNO = T0.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '28' "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "                  ) ";
        $query .= ") ";

        //テスト項目マスタの集計フラグの表
        $query .= " , TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         '2' AS DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        $query .= "     WHERE ";
        $query .= "             T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
        $query .= "     ) ";

        //生徒・科目・学期別欠課集計の表（出欠データと集計テーブルを合算）
        $query .= ", SCH_ATTEND_SUM AS(";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=     "SELECT  T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SEMESTER ";
        } else {
            $query .=     "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ";
        }
        $query .=            ",SUM(CASE WHEN DI_CD IN('4','5','6','14','11','12','13'";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .=           ",'1','8'";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .=           ",'2','9'";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .=           ",'3','10'";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .=           ",'19','20'";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .=           ",'25','26'";
        }
        $query .=                 ") ";
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .=             "OR (IS_OFFDAYS = '1')";
        }
        $query .=             " THEN 1 ELSE 0 END)AS ABSENT1 ";            
        $query .=            ",SUM(CASE WHEN DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ";
        $query .=     "FROM T_SCH_CHR_DAT T1, SCHNO T0 ";
        $query .=     "WHERE   T1.SCHREGNO = T0.SCHREGNO ";
//        if( definecode.useschchrcountflg ){
            $query .=     "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ";
            $query .=                     "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE ";
            $query .=                         "AND T4.PERIODCD = T1.PERIODCD ";
            $query .=                         "AND T4.CHAIRCD = T1.CHAIRCD ";
            $query .=                         "AND T4.GRADE = '".$grade."' ";
            $query .=                         "AND T4.HR_CLASS = T0.HR_CLASS ";
            $query .=                         "AND T1.DATADIV IN ('0','1') "; //テスト(DATADIV=2)以外
            $query .=                         "AND T4.COUNTFLG = '0') ";
            $query .= "    AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
            $query .= "                    WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
            $query .= "                      AND TEST.PERIODCD    = T1.PERIODCD ";
            $query .= "                      AND TEST.CHAIRCD     = T1.CHAIRCD ";
            $query .= "                      AND TEST.DATADIV     = T1.DATADIV) "; //テスト(DATADIV=2)
//        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=     "GROUP BY T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SEMESTER ";
            $query .=     "UNION ALL ";
            $query .=     "SELECT  T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, SEMESTER ";
        } else {
            $query .=     "GROUP BY T1.SCHREGNO, T1.SUBCLASSCD, T1.SEMESTER ";
            $query .=     "UNION ALL ";
            $query .=     "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, SEMESTER ";
        }
        $query .=            ",SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0)";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .=           "+ VALUE(ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .=           "+ VALUE(SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .=           "+ VALUE(MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .=           "+ VALUE(OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .=           "+ VALUE(VIRUS,0)";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .=           "+ VALUE(KOUDOME,0)";
        }
        $query .=                ") AS ABSENT1 ";            
        $query .=            ",SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ";
        $query .=     "FROM    ATTEND_SUBCLASS_DAT T1 ";
        $query .=     "WHERE   YEAR = '".CTRL_YEAR."' ";
        $query .=         "AND SEMESTER <= '".CTRL_SEMESTER."' ";
        if ($cntmonth > 0) {
            $query .=     "AND T1.MONTH IN ('".$isomonth."') ";
        } else {
            /********************************************/
            /* 集計するべきデータがない場合             */
            /* レコードが取れるはずがない条件を追加する */
            /* セレクト分に if 文を入れると             */
            /* ごちゃごちゃしそうなので、こうします。   */
            /********************************************/
            $query .=     "AND SEMESTER < '1' ";
        }
        $query .=         "AND EXISTS(SELECT  'X' FROM SCHNO T2 ";
        $query .=                    "WHERE   T2.SCHREGNO = T1.SCHREGNO ";
        $query .=                    "GROUP BY SCHREGNO) ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=     "GROUP BY T1.SCHREGNO, T1.SEMESTER, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ";
        } else {
            $query .=     "GROUP BY T1.SCHREGNO, T1.SEMESTER, T1.SUBCLASSCD ";
        }
        $query .= ") ";

        //ペナルティー欠課を加味した生徒欠課集計の表（出欠データと集計テーブルを合算）
        if( $absent_cov == 1 ){
            //学期でペナルティ欠課を算出する場合
            $query .= ", ATTEND_B AS(";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .=        "SELECT  SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ";
            } else {
                $query .=        "SELECT  SCHREGNO, SUBCLASSCD ";
            }
            $query .=                ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM1 ";
            $query .=                ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM2 ";
            $query .=                ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM3 ";
            $query .=                ",VALUE(SUM(ABSENT),0) AS ABSENT_SEM9 ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .=        "FROM(   SELECT  SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SEMESTER ";
                $query .=                        ",VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " . $absent_cov_late . " AS ABSENT ";
                $query .=                 "FROM    SCH_ATTEND_SUM T1 ";
                $query .=                "GROUP BY SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SEMESTER ";
                $query .=            ")T1 ";
                $query .=        "GROUP BY SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ";
            } else {
                $query .=        "FROM(   SELECT  SCHREGNO, SUBCLASSCD, SEMESTER ";
                $query .=                        ",VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " . $absent_cov_late . " AS ABSENT ";
                $query .=                 "FROM    SCH_ATTEND_SUM T1 ";
                $query .=                "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ";
                $query .=            ")T1 ";
                $query .=        "GROUP BY SCHREGNO, SUBCLASSCD ";
            }
            $query .= ") ";
        } else if( $absent_cov == 2 ){
            //通年でペナルティ欠課を算出する場合 
            //学期の欠課時数は学期別で換算したペナルティ欠課を加算、学年の欠課時数は年間で換算する
            $query .= ", ATTEND_B AS(";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .=        "SELECT  T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.ABSENT_SEM9 ";
                $query .=               ",T2.ABSENT_SEM1, T2.ABSENT_SEM2, T2.ABSENT_SEM3 ";
                $query .=        "FROM (";
                $query .=             "SELECT  SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ";
                $query .=                     ",VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " . $absent_cov_late . " AS ABSENT_SEM9 ";
                $query .=              "FROM    SCH_ATTEND_SUM T1 ";
                $query .=             "GROUP BY SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ";
                $query .=        ")T1, (";
                $query .=             "SELECT  SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ";
                $query .=                     ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM1 ";
                $query .=                     ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM2 ";
                $query .=                     ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM3 ";
                $query .=             "FROM(   SELECT  SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SEMESTER ";
                $query .=                             ",VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " . $absent_cov_late . " AS ABSENT ";
                $query .=                      "FROM    SCH_ATTEND_SUM T1 ";
                $query .=                     "GROUP BY SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SEMESTER ";
                $query .=                 ")T1 ";
                $query .=             "GROUP BY SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ";
                $query .=        ")T2 ";
                $query .=        "WHERE T1.SCHREGNO = T2.SCHREGNO AND T1.CLASSCD = T2.CLASSCD AND T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
                $query .=              "T1.CURRICULUM_CD = T2.CURRICULUM_CD AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
            } else {
                $query .=        "SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.ABSENT_SEM9 ";
                $query .=               ",T2.ABSENT_SEM1, T2.ABSENT_SEM2, T2.ABSENT_SEM3 ";
                $query .=        "FROM (";
                $query .=             "SELECT  SCHREGNO, SUBCLASSCD ";
                $query .=                     ",VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " . $absent_cov_late . " AS ABSENT_SEM9 ";
                $query .=              "FROM    SCH_ATTEND_SUM T1 ";
                $query .=             "GROUP BY SCHREGNO, SUBCLASSCD ";
                $query .=        ")T1, (";
                $query .=             "SELECT  SCHREGNO, SUBCLASSCD ";
                $query .=                     ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM1 ";
                $query .=                     ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM2 ";
                $query .=                     ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT ELSE NULL END),0) AS ABSENT_SEM3 ";
                $query .=             "FROM(   SELECT  SCHREGNO, SUBCLASSCD, SEMESTER ";
                $query .=                             ",VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " . $absent_cov_late . " AS ABSENT ";
                $query .=                      "FROM    SCH_ATTEND_SUM T1 ";
                $query .=                     "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ";
                $query .=                 ")T1 ";
                $query .=             "GROUP BY SCHREGNO, SUBCLASSCD ";
                $query .=        ")T2 ";
                $query .=        "WHERE T1.SCHREGNO = T2.SCHREGNO AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
            }
            $query .= ") ";
        } else{
            //ペナルティ欠課なしの場合
            $query .= ", ATTEND_B AS(";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .=        "SELECT  SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ";
            } else {
                $query .=        "SELECT  SCHREGNO, SUBCLASSCD ";
            }
            $query .=                ",VALUE(SUM(CASE WHEN SEMESTER = '1' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM1 ";
            $query .=                ",VALUE(SUM(CASE WHEN SEMESTER = '2' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM2 ";
            $query .=                ",VALUE(SUM(CASE WHEN SEMESTER = '3' THEN ABSENT1 ELSE NULL END),0) AS ABSENT_SEM3 ";
            $query .=                ",VALUE(SUM(ABSENT1),0) AS ABSENT_SEM9 ";
            $query .=         "FROM    SCH_ATTEND_SUM T1 ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .=        "GROUP BY SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ";
            } else {
                $query .=        "GROUP BY SCHREGNO, SUBCLASSCD ";
            }
            $query .= ") ";
        }

        //読替先のペナルティー欠課を加味した生徒欠課集計の表
        $query .= ", ATTEND_B_REPLACE AS(";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=      "SELECT  S2.SCHREGNO, ";
            $query .=              "S1.GRADING_CLASSCD AS CLASSCD, S1.GRADING_SCHOOL_KIND AS SCHOOL_KIND, S1.GRADING_CURRICULUM_CD AS CURRICULUM_CD, S1.GRADING_SUBCLASSCD AS SUBCLASSCD, ";
            $query .=              "SUM(S2.ABSENT_SEM9) AS ABSENT_SEM9 ";
            $query .=      "FROM    SUBCLASS_REPLACE_DAT S1, ATTEND_B S2 ";
            $query .=     "WHERE   S1.ATTEND_CLASSCD = S2.CLASSCD ";
            $query .=         "AND S1.ATTEND_SCHOOL_KIND = S2.SCHOOL_KIND ";
            $query .=         "AND S1.ATTEND_CURRICULUM_CD = S2.CURRICULUM_CD ";
            $query .=         "AND S1.ATTEND_SUBCLASSCD = S2.SUBCLASSCD ";
            $query .=         "AND S1.YEAR = '".CTRL_YEAR."' ";
            $query .=         "AND S1.ANNUAL = '".$grade."' ";  
            $query .=      "GROUP BY S2.SCHREGNO, S1.GRADING_CLASSCD, S1.GRADING_SCHOOL_KIND, S1.GRADING_CURRICULUM_CD, S1.GRADING_SUBCLASSCD ";
        } else {
            $query .=      "SELECT  S2.SCHREGNO, S1.GRADING_SUBCLASSCD AS SUBCLASSCD ";
            $query .=            ",SUM(S2.ABSENT_SEM9) AS ABSENT_SEM9 ";
            $query .=      "FROM    SUBCLASS_REPLACE_DAT S1, ATTEND_B S2 ";
            $query .=     "WHERE   S1.ATTEND_SUBCLASSCD = S2.SUBCLASSCD ";
            $query .=         "AND S1.YEAR = '".CTRL_YEAR."' ";
            $query .=         "AND S1.ANNUAL = '".$grade."' ";  
            $query .=      "GROUP BY S2.SCHREGNO, S1.GRADING_SUBCLASSCD ";
        }
        $query .= ") ";

        //無条件履修修得フラグがオンの科目の表
        $query .= ",CREDITS_UNCONDITION AS(";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=     "SELECT  SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, CREDITS ";
        } else {
            $query .=     "SELECT  SCHREGNO, SUBCLASSCD, CREDITS ";
        }
        $query .=     "FROM    CREDIT_MST T1, SCHNO T2 ";
        $query .=     "WHERE   T1.YEAR = '".CTRL_YEAR."' ";
        $query .=         "AND T1.GRADE = T2.GRADE ";
        $query .=         "AND T1.COURSECD = T2.COURSECD ";
        $query .=         "AND T1.MAJORCD = T2.MAJORCD ";
        $query .=         "AND T1.COURSECODE = T2.COURSECODE ";
        $query .=         "AND VALUE(T1.COMP_UNCONDITION_FLG,'0') = '1' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=         "AND NOT EXISTS(SELECT 'X' FROM RECORD T3 ";
            $query .=                        "WHERE T3.SCHREGNO = T2.SCHREGNO AND ";
            $query .=                              "T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD AND T3.SUBCLASSCD = T1.SUBCLASSCD) ";
        } else {
            $query .=         "AND NOT EXISTS(SELECT 'X' FROM RECORD T3 WHERE T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO) ";
        }
        $query .= ") ";

        //科目数カウント
        $query .= ",SUBCLASSNUM AS(";
        $query .=     "SELECT  SCHREGNO ";
        $query .=            ",SUM(CASE WHEN SUBSTR(SUBCLASSCD,1,2) = '90' THEN 1 ELSE NULL END) AS NUM90 ";
        $query .=            ",SUM(CASE WHEN SUBSTR(SUBCLASSCD,1,2) != '90' THEN 1 ELSE NULL END) AS NUMTOTAL ";
        $query .=     "FROM (";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=           "SELECT  S1.SCHREGNO, ";
            $query .=                   "S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || S1.SUBCLASSCD AS SUBCLASSCD ";
            $query .=           "FROM    CHAIR_A S1 ";
            $query .=           "WHERE   EXISTS(SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ";
            $query .=               "AND EXISTS(SELECT 'X' FROM RECORD S2 ";
            $query .=                          "WHERE S2.SCHREGNO = S1.SCHREGNO AND S2.CLASSCD = S1.CLASSCD AND S2.SCHOOL_KIND = S1.SCHOOL_KIND AND ";
            $query .=                                "S2.CURRICULUM_CD = S1.CURRICULUM_CD AND S2.SUBCLASSCD = S1.SUBCLASSCD) ";
            $query .=           "UNION ";
                                  //受講名簿が１件もない場合の評価読替科目
            $query .=           "SELECT  SCHREGNO, ";
            $query .=                   "GRADING_CLASSCD || '-' || GRADING_SCHOOL_KIND || '-' || GRADING_CURRICULUM_CD || '-' || GRADING_SUBCLASSCD AS SUBCLASSCD ";
            $query .=           "FROM    NOT_CHAIR_REPLACE S1, SCHNO S2 ";
                                  //無条件履修取得の科目
            $query .=           "UNION ";
            $query .=           "SELECT  SCHREGNO, ";
            $query .=                   "CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .=           "SELECT  S1.SCHREGNO, S1.SUBCLASSCD ";
            $query .=           "FROM    CHAIR_A S1 ";
            $query .=           "WHERE   EXISTS(SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ";
            $query .=               "AND EXISTS(SELECT 'X' FROM RECORD S2 WHERE S2.SCHREGNO = S1.SCHREGNO AND S2.SUBCLASSCD = S1.SUBCLASSCD) ";
            $query .=           "UNION ";
                                  //受講名簿が１件もない場合の評価読替科目
            $query .=           "SELECT  SCHREGNO, GRADING_SUBCLASSCD AS SUBCLASSCD ";
            $query .=           "FROM    NOT_CHAIR_REPLACE S1, SCHNO S2 ";
                                  //無条件履修取得の科目
            $query .=           "UNION ";
            $query .=           "SELECT  SCHREGNO, SUBCLASSCD ";
        }
        $query .=           "FROM    CREDITS_UNCONDITION S1 ";
        $query .=           ")T1 ";
        $query .=     "GROUP BY SCHREGNO ";
        $query .= ") ";

        //定期考査の出欠の表１
        $query .= ",TEST_ATTEND_A AS ( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=     "SELECT  T1.SEMESTER AS SEMES, T1.TESTKINDCD AS TESTKINDCD, ";
            $query .=             "T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, T3.SUBCLASSCD, ";
        } else {
            $query .=     "SELECT  T1.SEMESTER AS SEMES, T1.TESTKINDCD AS TESTKINDCD, T3.SUBCLASSCD,  ";
        }
        $query .=             "T2.SCHREGNO, (CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END) AS DI_CD  ";
        $query .=     "FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .=             "LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T2.YEAR AND L1.DI_CD = T2.DI_CD, ";
        $query .=             "CHAIR_DAT T3  ";
        $query .=     "WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .=             "T1.SEMESTER <= '".CTRL_SEMESTER."' AND  ";
        $query .=             "T1.CHAIRCD = T2.CHAIRCD AND  ";
        $query .=             "T3.YEAR = '".CTRL_YEAR."' AND  ";
        $query .=             "T3.SEMESTER = T1.SEMESTER AND  ";
        $query .=             "T3.CHAIRCD = T2.CHAIRCD AND  ";
        $query .=             "T2.SCHREGNO = '".$model->schregno."' AND  ";
        $query .=             "T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .=             "T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .=             "(CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END) IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .=     ") ";

        //定期考査の出欠の表２
        $query .= ",TEST_ATTEND_B AS ( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=     "SELECT  SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, ";
        } else {
            $query .=     "SELECT  SCHREGNO, SUBCLASSCD,  ";
        }
        $query .=             "MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM1_INTR_ATTEND,  ";
        $query .=             "MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM1_TERM_ATTEND,  ";
        $query .=             "MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM2_INTR_ATTEND,  ";
        $query .=             "MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM2_TERM_ATTEND,  ";
        $query .=             "MIN(CASE WHEN SEMES = '3' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM3_INTR_ATTEND,  ";
        $query .=             "MIN(CASE WHEN SEMES = '3' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM3_TERM_ATTEND  ";
        $query .=     "FROM    TEST_ATTEND_A  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=     "GROUP BY SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ";
        } else {
            $query .=     "GROUP BY SCHREGNO, SUBCLASSCD  ";
        }
        $query .=     ") ";


        //メイン表
        $query .= "SELECT  T2.ATTENDNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=         "T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD AS SUBCLASSCD, ";  
            $query .=         "T4.SUBCLASSNAME, T7.CLASSNAME AS CHAIRNAME, ";  
        } else {
            $query .=         "T5.SUBCLASSCD, T4.SUBCLASSNAME, T7.CLASSNAME AS CHAIRNAME, ";  
        }
        $query .=          "T6.CREDITS, T6.ABSENCE_HIGH, ";
        $query .=         "CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T1.COMP_CREDIT IS NULL THEN T6.CREDITS ELSE T1.COMP_CREDIT END AS COMP_CREDIT, ";  
        $query .=         "CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T1.GET_CREDIT IS NULL THEN T6.CREDITS ELSE T1.GET_CREDIT END AS GET_CREDIT, ";  
        $query .=         "T1.ADD_CREDIT, ";
        $query .=         "T1.COMP_CREDIT AS ON_RECORD_COMP, "; 
        $query .=         "T1.GET_CREDIT AS ON_RECORD_GET, "; 
        $query .=         "VALUE(T6.COMP_UNCONDITION_FLG,'0')AS COMP_UNCONDITION_FLG, ";  //値が'1'の場合無条件に単位を与える
        $query .=          "SEM1_INTR_SCORE, SEM1_INTR_VALUE, ";
        $query .=          "SEM1_TERM_SCORE, SEM1_TERM_VALUE, ";
        $query .=          "SEM1_VALUE, ";//
        $query .=          "SEM2_INTR_SCORE, SEM2_INTR_VALUE, ";
        $query .=          "SEM2_TERM_SCORE, SEM2_TERM_VALUE, ";
        $query .=          "SEM2_VALUE, ";//
        $query .=          "SEM3_INTR_SCORE, SEM3_INTR_VALUE, ";
        $query .=          "SEM3_TERM_SCORE, SEM3_TERM_VALUE, ";
        $query .=          "SEM3_VALUE, ";

        $query .=          "CASE WHEN SEM1_INTR_ATTEND IS NULL AND SEM1_TERM_ATTEND IS NULL THEN '' ELSE '/' END AS SEM1_ATTEND, ";
        $query .=          "CASE WHEN SEM2_INTR_ATTEND IS NULL AND SEM2_TERM_ATTEND IS NULL THEN '' ELSE '/' END AS SEM2_ATTEND, ";
        $query .=          "CASE WHEN SEM1_INTR_ATTEND IS NULL AND SEM1_TERM_ATTEND IS NULL AND SEM2_INTR_ATTEND IS NULL AND SEM2_TERM_ATTEND IS NULL AND SEM3_TERM_ATTEND IS NULL THEN '' ELSE '/' END AS GRAD_ATTEND, ";

        $query .=          "T1.GRAD_VALUE, ";//
        $query .=          "ABSENT_SEM1, ABSENT_SEM2, ABSENT_SEM3, ";
        $query .=         "T3.ABSENT_SEM9 AS ABSENT_TOTAL ";
        $query .=        ",T5.REPLACEFLG, T8.ABSENT_SEM9 AS REPLACE_ABSENT_TOTAL "; 
        $query .=        ",VALUE(T9.NUM90,0) AS NUM90, VALUE(T9.NUMTOTAL,0) AS NUMTOTAL ";  

        $query .= "FROM   SCHNO T2 ";

        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "LEFT JOIN (";
                            //任意の生徒が受講している科目
            $query .=     "SELECT  S1.SCHREGNO, S1.CLASSCD, S1.SCHOOL_KIND, S1.CURRICULUM_CD, S1.SUBCLASSCD ";
            $query .=            ",CASE WHEN S2.GRADING_CLASSCD || S2.GRADING_SCHOOL_KIND || S2.GRADING_CURRICULUM_CD || S2.GRADING_SUBCLASSCD IS NOT NULL THEN 9 ";
            $query .=                  "WHEN S4.ATTEND_CLASSCD || S4.ATTEND_SCHOOL_KIND || S4.ATTEND_CURRICULUM_CD || S4.ATTEND_SUBCLASSCD IS NOT NULL THEN 2 ";
            $query .=                  "WHEN S3.ATTEND_CLASSCD || S3.ATTEND_SCHOOL_KIND || S3.ATTEND_CURRICULUM_CD || S3.ATTEND_SUBCLASSCD IS NOT NULL THEN 1 ";
            $query .=                  "ELSE 0 END AS REPLACEFLG ";
            $query .=     "FROM    CHAIR_A S1 ";
            $query .=     "LEFT JOIN(";
                                //評価読替科目
            $query .=         "SELECT  S1.GRADING_CLASSCD, S1.GRADING_SCHOOL_KIND, S1.GRADING_CURRICULUM_CD, S1.GRADING_SUBCLASSCD ";
            $query .=         "FROM    SCHREG_SUBCLASS_REPLACE S1 ";
            $query .=         "GROUP BY S1.GRADING_CLASSCD, S1.GRADING_SCHOOL_KIND, S1.GRADING_CURRICULUM_CD, S1.GRADING_SUBCLASSCD ";
            $query .=     ")S2 ON S2.GRADING_CLASSCD = S1.CLASSCD AND S2.GRADING_SCHOOL_KIND = S1.SCHOOL_KIND AND S2.GRADING_CURRICULUM_CD = S1.CURRICULUM_CD AND S2.GRADING_SUBCLASSCD = S1.SUBCLASSCD ";
            $query .=     "LEFT JOIN(";
                                //評価読替元の科目
            $query .=         "SELECT  S1.ATTEND_CLASSCD, S1.ATTEND_SCHOOL_KIND, S1.ATTEND_CURRICULUM_CD, S1.ATTEND_SUBCLASSCD ";
            $query .=         "FROM    SCHREG_SUBCLASS_REPLACE S1 ";
            $query .=         "GROUP BY S1.ATTEND_CLASSCD, S1.ATTEND_SCHOOL_KIND, S1.ATTEND_CURRICULUM_CD, S1.ATTEND_SUBCLASSCD ";
            $query .=     ")S3 ON S3.ATTEND_CLASSCD = S1.CLASSCD AND S3.ATTEND_SCHOOL_KIND = S1.SCHOOL_KIND AND S3.ATTEND_CURRICULUM_CD = S1.CURRICULUM_CD AND S3.ATTEND_SUBCLASSCD = S1.SUBCLASSCD ";
            $query .=     "LEFT JOIN(";
                                //評価読替科目に学年評定がある評価読替元の科目 => 学年の値は非表示のため抽出
            $query .=         "SELECT  S1.ATTEND_CLASSCD, S1.ATTEND_SCHOOL_KIND, S1.ATTEND_CURRICULUM_CD, S1.ATTEND_SUBCLASSCD ";
            $query .=         "FROM    SCHREG_SUBCLASS_REPLACE S1 ";
            $query .=         "WHERE   EXISTS(SELECT  'X' FROM RECORD_DAT S2 ";
            $query .=                        "WHERE   S2.CLASSCD = S1.GRADING_CLASSCD AND S2.SCHOOL_KIND = S1.GRADING_SCHOOL_KIND AND S2.CURRICULUM_CD = S1.GRADING_CURRICULUM_CD AND S2.SUBCLASSCD = S1.GRADING_SUBCLASSCD AND ";
            $query .=                                "S2.GRAD_VALUE IS NOT NULL ";
            $query .=                        "GROUP BY S2.CLASSCD, S2.SCHOOL_KIND, S2.CURRICULUM_CD, S2.SUBCLASSCD) ";
            $query .=         "GROUP BY S1.ATTEND_CLASSCD, S1.ATTEND_SCHOOL_KIND, S1.ATTEND_CURRICULUM_CD, S1.ATTEND_SUBCLASSCD ";
            $query .=     ")S4 ON S4.ATTEND_CLASSCD = S1.CLASSCD AND S4.ATTEND_SCHOOL_KIND = S1.SCHOOL_KIND AND S4.ATTEND_CURRICULUM_CD = S1.CURRICULUM_CD AND S4.ATTEND_SUBCLASSCD = S1.SUBCLASSCD ";
            $query .=     "WHERE   EXISTS(SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ";
            $query .=         "AND EXISTS(SELECT 'X' FROM RECORD S2 WHERE S2.SCHREGNO = S1.SCHREGNO AND S2.CLASSCD = S1.CLASSCD AND S2.SCHOOL_KIND = S1.SCHOOL_KIND AND S2.CURRICULUM_CD = S1.CURRICULUM_CD AND S2.SUBCLASSCD = S1.SUBCLASSCD) ";
            $query .=     "UNION ";
                            //受講名簿が１件もない場合の評価読替科目
            $query .=     "SELECT  SCHREGNO, GRADING_CLASSCD AS CLASSCD, GRADING_SCHOOL_KIND AS SCHOOL_KIND, GRADING_CURRICULUM_CD AS CURRICULUM_CD, GRADING_SUBCLASSCD AS SUBCLASSCD, 9 AS REPLACEFLG ";
            $query .=     "FROM    NOT_CHAIR_REPLACE S1, SCHNO S2 ";

                            //無条件履修取得の科目
            $query .=     "UNION ";
            $query .=     "SELECT  SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, 0 AS REPLACEFLG ";
            $query .=     "FROM    CREDITS_UNCONDITION S1 ";

            $query .= ")T5 ON T5.SCHREGNO = T2.SCHREGNO ";

            $query .= "LEFT JOIN RECORD T1 ON T1.SCHREGNO = T5.SCHREGNO AND T1.CLASSCD = T5.CLASSCD AND T1.SCHOOL_KIND = T5.SCHOOL_KIND AND T1.CURRICULUM_CD = T5.CURRICULUM_CD AND T1.SUBCLASSCD = T5.SUBCLASSCD "; 
            $query .= "LEFT JOIN ATTEND_B T3 ON T3.SCHREGNO = T5.SCHREGNO AND T3.CLASSCD = T5.CLASSCD AND T3.SCHOOL_KIND = T5.SCHOOL_KIND AND T3.CURRICULUM_CD = T5.CURRICULUM_CD AND T3.SUBCLASSCD = T5.SUBCLASSCD ";
            $query .= "LEFT JOIN ATTEND_B_REPLACE T8 ON T8.SCHREGNO = T5.SCHREGNO AND T8.CLASSCD = T5.CLASSCD AND T8.SCHOOL_KIND = T5.SCHOOL_KIND AND T8.CURRICULUM_CD = T5.CURRICULUM_CD AND T8.SUBCLASSCD = T5.SUBCLASSCD "; 

            $query .= "LEFT JOIN TEST_ATTEND_B T10 ON T10.SCHREGNO = T5.SCHREGNO AND T10.CLASSCD = T5.CLASSCD AND T10.SCHOOL_KIND = T5.SCHOOL_KIND AND T10.CURRICULUM_CD = T5.CURRICULUM_CD AND T10.SUBCLASSCD = T5.SUBCLASSCD "; 

            $query .= "LEFT JOIN SUBCLASS_MST T4 ON T4.CLASSCD = T5.CLASSCD AND T4.SCHOOL_KIND = T5.SCHOOL_KIND AND T4.CURRICULUM_CD = T5.CURRICULUM_CD AND T4.SUBCLASSCD = T5.SUBCLASSCD ";
            $query .= "LEFT JOIN CLASS_MST T7 ON T7.CLASSCD = SUBSTR(T5.SUBCLASSCD,1,2) AND T7.SCHOOL_KIND = T5.SCHOOL_KIND ";  
            $query .= "LEFT JOIN CREDIT_MST T6 ON T6.YEAR = '".CTRL_YEAR."' ";
            $query .=                        "AND T6.CLASSCD = T5.CLASSCD ";
            $query .=                        "AND T6.SCHOOL_KIND = T5.SCHOOL_KIND ";
            $query .=                        "AND T6.CURRICULUM_CD = T5.CURRICULUM_CD ";
            $query .=                        "AND T6.SUBCLASSCD = T5.SUBCLASSCD ";
            $query .=                        "AND T6.GRADE = T2.GRADE ";
            $query .=                        "AND T6.COURSECD = T2.COURSECD ";
            $query .=                        "AND T6.MAJORCD = T2.MAJORCD ";
            $query .=                        "AND T6.COURSECODE = T2.COURSECODE ";

            $query .= "LEFT JOIN SUBCLASSNUM T9 ON T9.SCHREGNO = T5.SCHREGNO ";  

            $query .= "ORDER BY ATTENDNO, SUBCLASSCD ";  
        } else {
            $query .= "LEFT JOIN (";
                            //任意の生徒が受講している科目
            $query .=     "SELECT  S1.SCHREGNO, S1.SUBCLASSCD ";
            $query .=            ",CASE WHEN S2.SUBCLASSCD IS NOT NULL THEN 9 ";
            $query .=                  "WHEN S4.SUBCLASSCD IS NOT NULL THEN 2 ";
            $query .=                  "WHEN S3.SUBCLASSCD IS NOT NULL THEN 1 ";
            $query .=                  "ELSE 0 END AS REPLACEFLG ";
            $query .=     "FROM    CHAIR_A S1 ";
            $query .=     "LEFT JOIN(";
                                //評価読替科目
            $query .=         "SELECT  S1.GRADING_SUBCLASSCD AS SUBCLASSCD ";
            $query .=         "FROM    SCHREG_SUBCLASS_REPLACE S1 ";
            $query .=         "GROUP BY S1.GRADING_SUBCLASSCD ";
            $query .=     ")S2 ON S2.SUBCLASSCD = S1.SUBCLASSCD ";
            $query .=     "LEFT JOIN(";
                                //評価読替元の科目
            $query .=         "SELECT  S1.ATTEND_SUBCLASSCD AS SUBCLASSCD ";
            $query .=         "FROM    SCHREG_SUBCLASS_REPLACE S1 ";
            $query .=         "GROUP BY S1.ATTEND_SUBCLASSCD ";
            $query .=     ")S3 ON S3.SUBCLASSCD = S1.SUBCLASSCD ";
            $query .=     "LEFT JOIN(";
                                //評価読替科目に学年評定がある評価読替元の科目 => 学年の値は非表示のため抽出
            $query .=         "SELECT  S1.ATTEND_SUBCLASSCD AS SUBCLASSCD ";
            $query .=         "FROM    SCHREG_SUBCLASS_REPLACE S1 ";
            $query .=         "WHERE   EXISTS(SELECT  'X' FROM RECORD_DAT S2 ";
            $query .=                        "WHERE   S2.SUBCLASSCD = S1.GRADING_SUBCLASSCD AND S2.GRAD_VALUE IS NOT NULL ";
            $query .=                        "GROUP BY S2.SUBCLASSCD) ";
            $query .=         "GROUP BY S1.ATTEND_SUBCLASSCD ";
            $query .=     ")S4 ON S4.SUBCLASSCD = S1.SUBCLASSCD ";
            $query .=     "WHERE   EXISTS(SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO) ";
            $query .=         "AND EXISTS(SELECT 'X' FROM RECORD S2 WHERE S2.SCHREGNO = S1.SCHREGNO AND S2.SUBCLASSCD = S1.SUBCLASSCD) ";
            $query .=     "UNION ";
                            //受講名簿が１件もない場合の評価読替科目
            $query .=     "SELECT  SCHREGNO, GRADING_SUBCLASSCD AS SUBCLASSCD, 9 AS REPLACEFLG ";
            $query .=     "FROM    NOT_CHAIR_REPLACE S1, SCHNO S2 ";

                            //無条件履修取得の科目
            $query .=     "UNION ";
            $query .=     "SELECT  SCHREGNO, SUBCLASSCD, 0 AS REPLACEFLG ";
            $query .=     "FROM    CREDITS_UNCONDITION S1 ";

            $query .= ")T5 ON T5.SCHREGNO = T2.SCHREGNO ";

            $query .= "LEFT JOIN RECORD T1 ON T1.SCHREGNO = T5.SCHREGNO AND T1.SUBCLASSCD = T5.SUBCLASSCD "; 
            $query .= "LEFT JOIN ATTEND_B T3 ON T3.SCHREGNO = T5.SCHREGNO AND T3.SUBCLASSCD = T5.SUBCLASSCD ";
            $query .= "LEFT JOIN ATTEND_B_REPLACE T8 ON T8.SCHREGNO = T5.SCHREGNO AND T8.SUBCLASSCD = T5.SUBCLASSCD "; 

            $query .= "LEFT JOIN TEST_ATTEND_B T10 ON T10.SCHREGNO = T5.SCHREGNO AND T10.SUBCLASSCD = T5.SUBCLASSCD "; 

            $query .= "LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T5.SUBCLASSCD ";
            $query .= "LEFT JOIN CLASS_MST T7 ON T7.CLASSCD = SUBSTR(T5.SUBCLASSCD,1,2) ";  
            $query .= "LEFT JOIN CREDIT_MST T6 ON T6.YEAR = '".CTRL_YEAR."' ";
            $query .=                        "AND T6.SUBCLASSCD = T5.SUBCLASSCD ";
            $query .=                         "AND T6.GRADE = T2.GRADE ";
            $query .=                         "AND T6.COURSECD = T2.COURSECD ";
            $query .=                         "AND T6.MAJORCD = T2.MAJORCD ";
            $query .=                         "AND T6.COURSECODE = T2.COURSECODE ";

            $query .= "LEFT JOIN SUBCLASSNUM T9 ON T9.SCHREGNO = T5.SCHREGNO ";  

            $query .= "ORDER BY ATTENDNO, T5.SUBCLASSCD ";  
        }

        return $query;
    }

    //成績データ(順位)---SubForm1
    function getRecordRank($model, $grade, $hr_class)
    {
        //学籍の表（クラス）
        $query  = "WITH SCHNO AS( ";
        $query .=     "SELECT  SCHREGNO ";
        $query .=     "FROM    SCHREG_REGD_DAT ";
        $query .=     "WHERE   YEAR = '".CTRL_YEAR."'  ";
        $query .=         "AND SEMESTER = '".CTRL_SEMESTER."'  ";
        $query .=         "AND GRADE = '".$grade."'  ";
        $query .=         "AND HR_CLASS = '".$hr_class."'  ";
        $query .=     "GROUP BY SCHREGNO ";
        $query .=     ") ";

        //成績明細データ(順位)の表
        $query .= ",RECORD AS ( ";
        $query .=     "SELECT  T1.SCHREGNO ";
        $query .=            ",SUM(SEM1_VALUE) AS SEM1_SUM ";
        $query .=            ",SUM(SEM2_VALUE) AS SEM2_SUM ";
        $query .=            ",SUM(GRAD_VALUE) AS GRAD_SUM ";
        $query .=            ",RANK() OVER (ORDER BY VALUE(SUM(SEM1_VALUE),0) DESC) AS SEM1_RNK ";
        $query .=            ",RANK() OVER (ORDER BY VALUE(SUM(SEM2_VALUE),0) DESC) AS SEM2_RNK ";
        $query .=            ",RANK() OVER (ORDER BY VALUE(SUM(GRAD_VALUE),0) DESC) AS GRAD_RNK ";
        $query .=     "FROM    RECORD_DAT T1  ";
        $query .=     "WHERE   YEAR = '".CTRL_YEAR."'  ";
        $query .=         "AND EXISTS(SELECT  'X' FROM SCHNO T2 WHERE T2.SCHREGNO = T1.SCHREGNO)  ";
        $query .=         "AND (SUBSTR(SUBCLASSCD,1,2) BETWEEN '01' AND '89' OR SUBSTR(SUBCLASSCD,1,2) = '90') ";
        $query .=     "GROUP BY T1.SCHREGNO ";
        $query .=     ") ";

        //成績明細データ(人数)の表
        $query .= ",RECORD_CNT AS ( ";
        $query .=     "SELECT  COUNT(SEM1_SUM) AS SEM1_CNT ";
        $query .=            ",COUNT(SEM2_SUM) AS SEM2_CNT ";
        $query .=            ",COUNT(GRAD_SUM) AS GRAD_CNT ";
        $query .=     "FROM    RECORD ";
        $query .=     ") ";

        //メインの表
        $query .= "SELECT  SCHREGNO ";
        $query .=        ",(SELECT SEM1_CNT FROM RECORD_CNT) AS SEM1_CNT ";
        $query .=        ",(SELECT SEM2_CNT FROM RECORD_CNT) AS SEM2_CNT ";
        $query .=        ",(SELECT GRAD_CNT FROM RECORD_CNT) AS GRAD_CNT ";
        $query .=        ",CASE WHEN SEM1_SUM IS NULL THEN NULL ELSE SEM1_RNK END AS SEM1_RNK ";
        $query .=        ",CASE WHEN SEM2_SUM IS NULL THEN NULL ELSE SEM2_RNK END AS SEM2_RNK ";
        $query .=        ",CASE WHEN GRAD_SUM IS NULL THEN NULL ELSE GRAD_RNK END AS GRAD_RNK ";
        $query .= "FROM    RECORD ";
        $query .= "WHERE   SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //学校マスタの校種有無チェック
    function checkSchoolMst() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME  = 'SCHOOL_MST' AND ";
        $query .= "     NAME    = 'SCHOOL_KIND' ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //出欠データ参照---SubForm1
    function getAppointedDay($model)
    {
        $next_year = CTRL_YEAR + 1;

        $query  = "SELECT CASE WHEN MONTH < '04' THEN '".$next_year."' ELSE YEAR END AS YEAR, ";
        $query .= "       SEMESTER, ";
        $query .= "       MONTH, ";
        $query .= "       APPOINTED_DAY ";
        $query .= "  FROM ATTEND_SEMES_DAT ";
        $query .= " WHERE COPYCD = '0' AND ";
        $query .= "       YEAR   = '".CTRL_YEAR."' AND SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .= " GROUP BY YEAR, SEMESTER, MONTH,APPOINTED_DAY ";
        $query .= " ORDER BY YEAR, SEMESTER, MONTH,APPOINTED_DAY ";

        return $query;
    }

    //出欠データ参照---SubForm1
    function getAttend($model, $isodate, $isomonth, $cntmonth, $knjSchoolMst)
    {
        //対象生徒
        $query  = "WITH SCHNO (SCHREGNO, SEMESTER) AS(";
        $query .=      "SELECT  T1.SCHREGNO, T1.SEMESTER ";
        $query .=     "FROM    SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .=     "WHERE   T1.YEAR = '".CTRL_YEAR."' ";
        $query .=          "AND T1.SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .=         "AND T1.YEAR = T2.YEAR ";
        $query .=         "AND T1.SEMESTER = T2.SEMESTER ";
        $query .=         "AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .=      "GROUP BY SCHREGNO,T1.SEMESTER ";
        $query .=      "UNION ";
        $query .=      "VALUES( cast('".$model->schregno."' as varchar(8) ), '9') ";
        $query .=      ")";

        //対象生徒の時間割データ
        $query .= ",SCHEDULE_SCHREG AS(";
        $query .=      "SELECT  T2.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD ";
        $query .=      "FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2 ";
        $query .=      "WHERE   T1.YEAR = '".CTRL_YEAR."' ";
        $query .=          "AND T1.SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .=          "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .=          "AND T1.EXECUTEDATE > '".$isodate."' ";
        $query .=          "AND T1.EXECUTEDATE <= '".CTRL_DATE."' ";
        $query .=          "AND T1.YEAR = T2.YEAR ";
        $query .=          "AND T1.SEMESTER = T2.SEMESTER ";
        $query .=          "AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .=          "AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) ";
                            //学籍不在日を除外
        $query .=          "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .=                         "WHERE   T6.SCHREGNO = T2.SCHREGNO ";
        $query .=                             "AND (( T6.ENT_DIV IN('4','5') AND T1.EXECUTEDATE < T6.ENT_DATE ) ";
        $query .=                               "OR ( T6.GRD_DIV IN('2','3') AND T1.EXECUTEDATE > T6.GRD_DATE )) ) ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
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
        $query .=      "GROUP BY T2.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD ";
        $query .=      ") ";

        //対象生徒の出欠データ
        $query .= ",T_ATTEND_DAT AS(";
        $query .=      "SELECT  T0.SCHREGNO, T1.SEMESTER, T0.ATTENDDATE, T0.PERIODCD, ";
        $query .=              "CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END AS DI_CD ";
        $query .=      "FROM    ATTEND_DAT T0 ";
        $query .=              "LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T0.YEAR AND L1.DI_CD = T0.DI_CD, ";
        $query .=              "SCHEDULE_SCHREG T1 ";
        $query .=      "WHERE   T0.YEAR = '".CTRL_YEAR."' AND ";
        $query .=              "T0.ATTENDDATE > '".$isodate."' AND ";
        $query .=              "T0.ATTENDDATE <= '".CTRL_DATE."' AND ";
        $query .=              "T0.SCHREGNO = T1.SCHREGNO AND ";
        $query .=              "T0.ATTENDDATE = T1.EXECUTEDATE AND ";
        $query .=              "T0.PERIODCD = T1.PERIODCD ";
                            //留学日、休学日を除外
        $query .=          "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ";
        $query .=                         "WHERE   T7.SCHREGNO = T1.SCHREGNO ";
        $query .=                             "AND T7.TRANSFERCD IN('1','2') ";
        $query .=                             "AND T1.EXECUTEDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        $query .=      ") ";

        //対象生徒の出欠データ（忌引・出停した日）
        $query .= ",T_ATTEND_DAT_B AS(";
        $query .=      "SELECT  T1.SCHREGNO, T1.SEMESTER, T1.ATTENDDATE ";
        $query .=      "FROM    T_ATTEND_DAT T1 ";
        $query .=      "WHERE   DI_CD IN('2','9','3','10' ";
        if ($model->virus) {
            $query .= "                 ,'19','20' ";
        }
        if ($model->koudome) {
            $query .= "                 ,'25','26' ";
        }
        $query .=                       ") ";
        $query .=      "GROUP BY T1.SCHREGNO, T1.SEMESTER, T1.ATTENDDATE ";
        $query .=      ") ";

        //対象生徒の日単位の最小校時・最大校時・校時数
        $query .= ",T_PERIOD_CNT AS(";
        $query .=      "SELECT  T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, ";
        $query .=              "MIN(T1.PERIODCD) AS FIRST_PERIOD, ";
        $query .=              "MAX(T1.PERIODCD) AS LAST_PERIOD, ";
        $query .=              "COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ";
        $query .=      "FROM    SCHEDULE_SCHREG T1 ";
        $query .=      "GROUP BY T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE ";
        $query .=      ") ";

        //メイン表
        $query .=    "SELECT  TT0.SCHREGNO, TT0.SEMESTER, ";
                                     //授業日数
        $query .=            "VALUE(TT1.LESSON,0) + VALUE(TT7.LESSON,0) ";
        $query .=            " - VALUE(TT12.ABROAD,0) - VALUE(TT7.ABROAD,0) ";
        $query .=            " - VALUE(TT13.OFFDAYS,0) - VALUE(TT7.OFFDAYS,0) ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .=        " + VALUE(TT13.OFFDAYS,0) + VALUE(TT7.OFFDAYS,0) ";
        }
        $query .=            " AS LESSON, ";
                                     //出席すべき日数
        $query .=            "VALUE(TT1.LESSON,0) + VALUE(TT7.LESSON,0) ";
        $query .=            " - VALUE(TT3.SUSPEND,0) - VALUE(TT7.SUSPEND,0) ";
        if ($model->virus) {
            $query .=            " - VALUE(TT3_V.VIRUS,0) - VALUE(TT7.VIRUS,0) ";
        }
        if ($model->koudome) {
            $query .=            " - VALUE(TT3_K.KOUDOME,0) - VALUE(TT7.KOUDOME,0) ";
        }
        $query .=            " - VALUE(TT4.MOURNING,0) - VALUE(TT7.MOURNING,0) ";
        $query .=            " - VALUE(TT12.ABROAD,0) - VALUE(TT7.ABROAD,0) ";
        $query .=            " - VALUE(TT13.OFFDAYS,0) - VALUE(TT7.OFFDAYS,0) ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .=        " + VALUE(TT13.OFFDAYS,0) + VALUE(TT7.OFFDAYS,0) ";
        }
        $query .=            " AS MLESSON, ";
                                     //出停・忌引日数
        $query .=            "VALUE(TT3.SUSPEND,0) + VALUE(TT4.MOURNING,0) ";
        if ($model->virus) {
            $query .=            " + VALUE(TT3_V.VIRUS,0) + VALUE(TT7.VIRUS,0) ";
        }
        if ($model->koudome) {
            $query .=            " + VALUE(TT3_K.KOUDOME,0) + VALUE(TT7.KOUDOME,0) ";
        }
        $query .=            " + VALUE(TT7.SUSPEND,0) + VALUE(TT7.MOURNING,0) AS MOURNING_SUSPEND, ";
                                     //欠席日数
        $query .=            "VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) + VALUE(TT7.ABSENT,0) ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .=        " + VALUE(TT13.OFFDAYS,0) + VALUE(TT7.OFFDAYS,0) ";
        }
        $query .=            " AS ABSENT, ";
                                     //早退回数
        $query .=            "VALUE(TT6.EARLY,0) + VALUE(TT11.EARLY,0) + VALUE(TT7.EARLY,0) AS EARLY ";
        $query .=    "FROM    SCHNO TT0 ";
        //個人別授業日数
        $query .=    "LEFT OUTER JOIN(";
        $query .=       "SELECT  SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, COUNT(DISTINCT EXECUTEDATE) AS LESSON ";
        $query .=       "FROM    T_PERIOD_CNT ";
        $query .=       "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ";
        $query .=       ") TT1 ON TT0.SCHREGNO = TT1.SCHREGNO AND TT0.SEMESTER = TT1.SEMESTER ";
        //個人別出停日数
        $query .=    "LEFT OUTER JOIN(";
        $query .=       "SELECT SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, COUNT(DISTINCT ATTENDDATE) AS SUSPEND ";
        $query .=       "FROM   T_ATTEND_DAT ";
        $query .=       "WHERE  DI_CD IN ('2','9') ";
        $query .=       "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ";
        $query .=       ") TT3 ON TT0.SCHREGNO = TT3.SCHREGNO AND TT0.SEMESTER = TT3.SEMESTER ";
        //伝染病
        if ($model->virus) {
            $query .=    "LEFT OUTER JOIN(";
            $query .=       "SELECT SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, COUNT(DISTINCT ATTENDDATE) AS VIRUS ";
            $query .=       "FROM   T_ATTEND_DAT ";
            $query .=       "WHERE  DI_CD IN ('19','20') ";
            $query .=       "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ";
            $query .=       ") TT3_V ON TT0.SCHREGNO = TT3_V.SCHREGNO AND TT0.SEMESTER = TT3_V.SEMESTER ";
        }
        //出停（交止）
        if ($model->koudome) {
            $query .=    "LEFT OUTER JOIN(";
            $query .=       "SELECT SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, COUNT(DISTINCT ATTENDDATE) AS KOUDOME ";
            $query .=       "FROM   T_ATTEND_DAT ";
            $query .=       "WHERE  DI_CD IN ('25','26') ";
            $query .=       "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ";
            $query .=       ") TT3_K ON TT0.SCHREGNO = TT3_K.SCHREGNO AND TT0.SEMESTER = TT3_K.SEMESTER ";
        }
        //個人別忌引日数
        $query .=    "LEFT OUTER JOIN(";
        $query .=       "SELECT SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, COUNT(DISTINCT ATTENDDATE) AS MOURNING ";
        $query .=       "FROM   T_ATTEND_DAT ";
        $query .=       "WHERE  DI_CD IN ('3','10') ";
        $query .=       "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ";
        $query .=       ") TT4 ON TT0.SCHREGNO = TT4.SCHREGNO AND TT0.SEMESTER = TT4.SEMESTER ";
        //個人別欠席日数
        $query .=    "LEFT OUTER JOIN(";
        $query .=       "SELECT  W1.SCHREGNO, VALUE(W1.SEMESTER,'9')AS SEMESTER, ";
        $query .=               "SUM(CASE L1.REP_DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ";
        $query .=               "SUM(CASE L1.REP_DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ";
        $query .=               "SUM(CASE L1.REP_DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ";
        $query .=       "FROM    ATTEND_DAT W0 ";
        $query .=               "LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = W0.YEAR AND L1.DI_CD = W0.DI_CD, ";
        $query .=          "(";
        $query .=          "SELECT  T0.SCHREGNO, T1.SEMESTER, T0.EXECUTEDATE, T0.FIRST_PERIOD ";
        $query .=          "FROM    T_PERIOD_CNT T0, ";
        $query .=             "(";
        $query .=             "SELECT  W1.SCHREGNO, SEMESTER, W1.ATTENDDATE, ";
        $query .=                     "MIN(W1.PERIODCD) AS FIRST_PERIOD, ";
        $query .=                     "COUNT(W1.PERIODCD) AS PERIOD_CNT ";
        $query .=             "FROM    T_ATTEND_DAT W1 ";
        $query .=             "WHERE   W1.DI_CD IN ('4','5','6','11','12','13') ";
        $query .=             "GROUP BY W1.SCHREGNO, SEMESTER, W1.ATTENDDATE ";
        $query .=             ") T1 ";
        $query .=          "WHERE   T0.SCHREGNO = T1.SCHREGNO AND ";
        $query .=                  "T0.EXECUTEDATE = T1.ATTENDDATE AND ";
        $query .=                  "T0.FIRST_PERIOD = T1.FIRST_PERIOD AND ";
        $query .=                  "T0.PERIOD_CNT = T1.PERIOD_CNT ";
        $query .=          ") W1 ";
        $query .=       "WHERE   W0.SCHREGNO = W1.SCHREGNO AND ";
        $query .=               "W0.ATTENDDATE = W1.EXECUTEDATE AND ";
        $query .=               "W0.PERIODCD = W1.FIRST_PERIOD ";
        $query .=       "GROUP BY GROUPING SETS((W1.SCHREGNO,W1.SEMESTER),W1.SCHREGNO) ";
        $query .=       ")TT5 ON TT0.SCHREGNO = TT5.SCHREGNO AND TT0.SEMESTER = TT5.SEMESTER ";

        //個人別遅刻・早退回数
        $query .=    "LEFT OUTER JOIN(";
        $query .=       "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS LATE, COUNT(T3.ATTENDDATE) AS EARLY ";
        $query .=              ",VALUE(T0.SEMESTER,'9')AS SEMESTER ";
        $query .=       "FROM    T_PERIOD_CNT T0 ";
        $query .=       "INNER JOIN(";
        $query .=          "SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT ";
        $query .=          "FROM    T_ATTEND_DAT W1 ";
        $query .=          "WHERE   W1.DI_CD NOT IN ('0','14','15','16','23','24','29','30','31','32') ";
        $query .=          "AND NOT EXISTS (SELECT 'X' ";
        $query .=                          "FROM   T_ATTEND_DAT_B W2 ";
        $query .=                          "WHERE  W2.SCHREGNO = W1.SCHREGNO ";
        $query .=                            "AND  W2.ATTENDDATE = W1.ATTENDDATE) ";
        $query .=          "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ";
        $query .=          ")T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.EXECUTEDATE = T1.ATTENDDATE AND ";
        $query .=                                               "T0.PERIOD_CNT != T1.PERIOD_CNT ";
        $query .=       "LEFT OUTER JOIN(";
        $query .=          "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ";
        $query .=          "FROM    T_ATTEND_DAT ";
        $query .=          "WHERE   DI_CD IN ('4','5','6','11','12','13') ";
        $query .=          ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ";
        $query .=                                               "T0.FIRST_PERIOD = T2.PERIODCD ";
        $query .=       "LEFT OUTER JOIN(";
        $query .=          "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ";
        $query .=          "FROM    T_ATTEND_DAT ";
        $query .=          "WHERE   DI_CD IN ('4','5','6') ";
        $query .=          ")T3 ON T0.SCHREGNO= T3.SCHREGNO AND T0.EXECUTEDATE = T3.ATTENDDATE AND ";
        $query .=                                              "T0.LAST_PERIOD = T3.PERIODCD ";
        $query .=       "GROUP BY GROUPING SETS((T0.SCHREGNO,T0.SEMESTER),T0.SCHREGNO) ";
        $query .=       ")TT6 ON TT0.SCHREGNO = TT6.SCHREGNO AND TT0.SEMESTER = TT6.SEMESTER ";

        //個人別遅刻回数
        $query .=    "LEFT OUTER JOIN(";
        $query .=       "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS LATE ";
        $query .=              ",VALUE(T0.SEMESTER,'9')AS SEMESTER ";
        $query .=       "FROM    T_PERIOD_CNT T0 ";
        $query .=       "INNER JOIN(";
        $query .=          "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ";
        $query .=          "FROM    T_ATTEND_DAT W1 ";
        $query .=          "WHERE   DI_CD IN ('15','23','24','29','31','32') ";
        $query .=          "AND NOT EXISTS (SELECT 'X' ";
        $query .=                          "FROM   T_ATTEND_DAT_B W2 ";
        $query .=                          "WHERE  W2.SCHREGNO = W1.SCHREGNO ";
        $query .=                            "AND  W2.ATTENDDATE = W1.ATTENDDATE) ";
        $query .=          ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ";
        $query .=                                               "T0.FIRST_PERIOD = T2.PERIODCD ";
        $query .=       "GROUP BY GROUPING SETS((T0.SCHREGNO,T0.SEMESTER),T0.SCHREGNO) ";
        $query .=       ")TT10 ON TT0.SCHREGNO = TT10.SCHREGNO AND TT0.SEMESTER = TT10.SEMESTER ";

        //個人別早退回数
        $query .=    "LEFT OUTER JOIN(";
        $query .=       "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS EARLY ";
        $query .=              ",VALUE(T0.SEMESTER,'9')AS SEMESTER ";
        $query .=       "FROM    T_PERIOD_CNT T0 ";
        $query .=       "INNER JOIN(";
        $query .=          "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ";
        $query .=          "FROM    T_ATTEND_DAT W1 ";
        $query .=          "WHERE   DI_CD IN ('16','30','31','32') ";
        $query .=          "AND NOT EXISTS (SELECT 'X' ";
        $query .=                          "FROM   T_ATTEND_DAT_B W2 ";
        $query .=                          "WHERE  W2.SCHREGNO = W1.SCHREGNO ";
        $query .=                            "AND  W2.ATTENDDATE = W1.ATTENDDATE) ";
        $query .=          ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ";
        $query .=                                               "T0.LAST_PERIOD = T2.PERIODCD ";
        $query .=       "GROUP BY GROUPING SETS((T0.SCHREGNO,T0.SEMESTER),T0.SCHREGNO) ";
        $query .=       ")TT11 ON TT0.SCHREGNO = TT11.SCHREGNO AND TT0.SEMESTER = TT11.SEMESTER ";

        //月別集計データから集計した表
        $query .=    "LEFT JOIN(";
        $query .=       "SELECT  SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, ";
        $query .=               "SUM(LESSON) AS LESSON, ";
        $query .=               "SUM(ABROAD) AS ABROAD, ";
        $query .=               "SUM(OFFDAYS) AS OFFDAYS, ";
        $query .=               "SUM(MOURNING) AS MOURNING, ";
        $query .=               "SUM(SUSPEND) AS SUSPEND, ";
        if ($model->virus) {
            $query .=               "SUM(VIRUS) AS VIRUS, ";
        }
        if ($model->koudome) {
            $query .=               "SUM(KOUDOME) AS KOUDOME, ";
        }
        $query .=               "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ) AS ABSENT, ";
        $query .=               "SUM(LATE) AS LATE, ";
        $query .=               "SUM(EARLY) AS EARLY ";
        $query .=       "FROM    ATTEND_SEMES_DAT W1 ";
        $query .=       "WHERE   YEAR = '".CTRL_YEAR."' AND ";
        $query .=               "SEMESTER <= '".CTRL_SEMESTER."' AND ";

        if ($cntmonth > 0) {
            $query .=           "W1.MONTH IN ('".$isomonth."') AND ";
        } else {
            /********************************************/
            /* 集計するべきデータがない場合             */
            /* レコードが取れるはずがない条件を追加する */
            /* セレクト分に if 文を入れると             */
            /* ごちゃごちゃしそうなので、こうします。   */
            /********************************************/
            $query .=           "SEMESTER < '1' AND ";
        }
        $query .=               "EXISTS(SELECT  'X' ";
        $query .=                      "FROM    SCHNO W2 ";
        $query .=                      "WHERE   W1.SCHREGNO = W2.SCHREGNO)";
        $query .=       "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ";
        $query .=       ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO AND TT0.SEMESTER = TT7.SEMESTER ";

        //留学中の授業日数
        $query .=    "LEFT OUTER JOIN(";
        $query .=       "SELECT  T1.SCHREGNO, VALUE(T1.SEMESTER,'9')AS SEMESTER, COUNT(DISTINCT T1.EXECUTEDATE) AS ABROAD ";
        $query .=       "FROM    T_PERIOD_CNT T1, SCHREG_TRANSFER_DAT T7 ";
        $query .=       "WHERE   T7.SCHREGNO = T1.SCHREGNO ";
        $query .=           "AND T7.TRANSFERCD IN('1') ";
        $query .=           "AND T1.EXECUTEDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ";
        $query .=       "GROUP BY GROUPING SETS((T1.SCHREGNO,T1.SEMESTER),T1.SCHREGNO) ";
        $query .=       ") TT12 ON TT0.SCHREGNO = TT12.SCHREGNO AND TT0.SEMESTER = TT12.SEMESTER ";

        //休学中の授業日数
        $query .=    "LEFT OUTER JOIN(";
        $query .=       "SELECT  T1.SCHREGNO, VALUE(T1.SEMESTER,'9')AS SEMESTER, COUNT(DISTINCT T1.EXECUTEDATE) AS OFFDAYS ";
        $query .=       "FROM    T_PERIOD_CNT T1, SCHREG_TRANSFER_DAT T7 ";
        $query .=       "WHERE   T7.SCHREGNO = T1.SCHREGNO ";
        $query .=           "AND T7.TRANSFERCD IN('2') ";
        $query .=           "AND T1.EXECUTEDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ";
        $query .=       "GROUP BY GROUPING SETS((T1.SCHREGNO,T1.SEMESTER),T1.SCHREGNO) ";
        $query .=       ") TT13 ON TT0.SCHREGNO = TT13.SCHREGNO AND TT0.SEMESTER = TT13.SEMESTER ";

        $query .= "ORDER BY SCHREGNO ,TT0.SEMESTER";

        return $query;
    }

    //出欠データ参照---SubForm1
    function getAttendSHR($model, $isodate, $isomonth, $cntmonth)
    {
        //対象生徒
        $query  = "WITH SCHNO (SCHREGNO, SEMESTER) AS(";
        $query .=      "SELECT  T1.SCHREGNO, T1.SEMESTER ";
        $query .=     "FROM    SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .=     "WHERE   T1.YEAR = '".CTRL_YEAR."' ";
        $query .=          "AND T1.SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .=         "AND T1.YEAR = T2.YEAR ";
        $query .=         "AND T1.SEMESTER = T2.SEMESTER ";
        $query .=         "AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .=      "GROUP BY SCHREGNO,T1.SEMESTER ";
        $query .=      "UNION ";
        $query .=      "VALUES( cast('".$model->schregno."' as varchar(8) ), '9') ";
        $query .=      ")";

        //対象生徒の出欠データ(授業の遅刻) 
        $query .= ",shr_ATTEND_DATb AS( ";
        $query .= "    SELECT  T1.SCHREGNO, T1.ATTENDDATE, T1.PERIODCD, L1.REP_DI_CD AS DI_CD, L1.MULTIPLY, T2.SEMESTER ";
        $query .= "    FROM    ATTEND_DAT T1 ";
        $query .= "            LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T1.YEAR AND L1.DI_CD = T1.DI_CD ";
        $query .= "           ,semester_mst t2 ";
        $query .= "           ,chair_dat t3 ";
        $query .= "    WHERE   T1.YEAR = '".CTRL_YEAR."'  ";
        $query .= "        and T1.ATTENDDATE > '".$isodate."'  ";
        $query .= "        and T1.ATTENDDATE <= '".CTRL_DATE."'  ";
        $query .= "        and t2.year = '".CTRL_YEAR."' ";
        $query .= "        and t2.semester <= '".CTRL_SEMESTER."' ";
        $query .= "        and t3.year = '".CTRL_YEAR."' ";
        $query .= "        and t1.attenddate between t2.sdate and t2.edate ";
        $query .= "        and t2.semester = t3.semester ";
        $query .= "        and t3.chaircd = t1.chaircd ";
        $query .= "        and substr(t3.subclasscd,1,2) <> '92' ";
        $query .= "        and L1.REP_DI_CD in('15','23','24','29','31','32') ";
        $query .= "        and exists (SELECT SCHREGNO FROM SCHNO t3 where t3.schregno = T1.SCHREGNO)    ";
        $query .= "    ) ";

        //対象生徒の出欠データ(遅刻度数) 
        $query .= ",shr_ATTEND_DATa AS( ";
        $query .= "    SELECT  T1.SCHREGNO, T1.ATTENDDATE, T1.PERIODCD, L1.REP_DI_CD AS DI_CD, L1.MULTIPLY, T2.SEMESTER ";
        $query .= "    FROM    ATTEND_DAT T1 ";
        $query .= "            LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T1.YEAR AND L1.DI_CD = T1.DI_CD ";
        $query .= "           ,semester_mst t2 ";
        $query .= "           ,chair_dat t3 ";
        $query .= "    WHERE   T1.YEAR = '".CTRL_YEAR."'  ";
        $query .= "        and T1.ATTENDDATE BETWEEN '".CTRL_YEAR."-04-01"."' AND '".CTRL_DATE."'  ";
        $query .= "        and t2.year = '".CTRL_YEAR."' ";
        $query .= "        and t2.semester <= '".CTRL_SEMESTER."' ";
        $query .= "        and t3.year = '".CTRL_YEAR."' ";
        $query .= "        and t1.attenddate between t2.sdate and t2.edate ";
        $query .= "        and t2.semester = t3.semester ";
        $query .= "        and t3.chaircd = t1.chaircd ";
        $query .= "        and substr(t3.subclasscd,1,2) = '92' ";
        $query .= "        and L1.REP_DI_CD in('4','5','6','11','12','13','15','23','24','29','31','32') ";
        $query .= "        and exists (SELECT SCHREGNO FROM SCHNO t3 where t3.schregno = T1.SCHREGNO)    ";
        $query .= "    ) ";

        //対象生徒の時間割データ
        $query .= ",SCHEDULE_SCHREG AS( ";
        $query .= "    SELECT  T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD  ";
        $query .= "    FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2  ";
        $query .= "    WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "            T1.EXECUTEDATE BETWEEN '".CTRL_YEAR."-04-01"."' AND '".CTRL_DATE."' AND  ";
        $query .= "            T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "            T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND  ";
        $query .= "            T1.SEMESTER = T2.SEMESTER AND  ";
        $query .= "            T1.CHAIRCD = T2.CHAIRCD AND  ";
        $query .= "            exists (SELECT 'x' FROM shr_ATTEND_DATa t3  ";
        $query .= "                     where t3.schregno = T2.SCHREGNO  ";
        $query .= "                       and t3.attenddate = t1.executedate)    ";
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
        $query .= "             ";
        $query .= "    GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD  ";
        $query .= "    ) ";

        //対象生徒の出欠データ
        $query .= ",T_ATTEND_DAT AS( ";
        $query .= "    SELECT  T0.SCHREGNO, T0.ATTENDDATE, T0.PERIODCD, L1.REP_DI_CD AS DI_CD  ";
        $query .= "    FROM    ATTEND_DAT T0  ";
        $query .= "            LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T0.YEAR AND L1.DI_CD = T0.DI_CD, ";
        $query .= "            SCHEDULE_SCHREG T1  ";
        $query .= "    WHERE   T0.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "            T0.ATTENDDATE BETWEEN '".CTRL_YEAR."-04-01"."' AND '".CTRL_DATE."' AND  ";
        $query .= "            T0.SCHREGNO = T1.SCHREGNO AND  ";
        $query .= "            T0.ATTENDDATE = T1.EXECUTEDATE AND  ";
        $query .= "            T0.PERIODCD = T1.PERIODCD  ";
        $query .= "    ) ";

        //対象生徒の日単位の最小校時・最大校時・校時数
        $query .= ",T_PERIOD_CNT AS( ";
        $query .= "    SELECT  T1.SCHREGNO, T1.EXECUTEDATE,  ";
        $query .= "            MIN(T1.PERIODCD) AS FIRST_PERIOD,  ";
        $query .= "            MAX(T1.PERIODCD) AS LAST_PERIOD,  ";
        $query .= "            COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT  ";
        $query .= "    FROM    SCHEDULE_SCHREG T1  ";
        $query .= "    GROUP BY T1.SCHREGNO, T1.EXECUTEDATE  ";
        $query .= "    ) ";

        //個人別出停・忌引
        $query .= ",suspend_MOURNING_data as( ";
        $query .= "    SELECT SCHREGNO, attenddate ";
        $query .= "    FROM   T_ATTEND_DAT  ";
        $query .= "    WHERE  DI_CD IN ('2','9','3','10' ";
        if ($model->virus) {
            $query .= "                 ,'19','20' ";
        }
        if ($model->koudome) {
            $query .= "                 ,'25','26' ";
        }
        $query .= "                     )  ";
        $query .= "    ) ";

        //個人別欠席
        $query .= ",absent_data as( ";
        $query .= "    SELECT  W0.SCHREGNO, w0.attenddate ";
        $query .= "    FROM    ATTEND_DAT W0,  ";
        $query .= "        ( ";
        $query .= "        SELECT  T0.SCHREGNO, T0.EXECUTEDATE, T0.FIRST_PERIOD  ";
        $query .= "        FROM    T_PERIOD_CNT T0,  ";
        $query .= "            ( ";
        $query .= "            SELECT  W1.SCHREGNO, W1.ATTENDDATE,  ";
        $query .= "                    MIN(W1.PERIODCD) AS FIRST_PERIOD,  ";
        $query .= "                    COUNT(W1.PERIODCD) AS PERIOD_CNT  ";
        $query .= "            FROM    T_ATTEND_DAT W1  ";
        $query .= "            WHERE   W1.DI_CD IN ('4','5','6','11','12','13')  ";
        $query .= "            GROUP BY W1.SCHREGNO, W1.ATTENDDATE  ";
        $query .= "            ) T1  ";
        $query .= "        WHERE   T0.SCHREGNO = T1.SCHREGNO AND  ";
        $query .= "                T0.EXECUTEDATE = T1.ATTENDDATE AND  ";
        $query .= "                T0.FIRST_PERIOD = T1.FIRST_PERIOD AND  ";
        $query .= "                T0.PERIOD_CNT = T1.PERIOD_CNT  ";
        $query .= "        ) W1  ";
        $query .= "    WHERE   W0.SCHREGNO = W1.SCHREGNO AND  ";
        $query .= "            W0.ATTENDDATE = W1.EXECUTEDATE AND  ";
        $query .= "            W0.PERIODCD = W1.FIRST_PERIOD  ";
        $query .= "    ) ";

        //個人別早退１
        $query .= ",early_data1 as( ";
        $query .= "    SELECT  T0.SCHREGNO, T1.ATTENDDATE ";
        $query .= "    FROM    T_PERIOD_CNT T0  ";
        $query .= "        INNER JOIN( ";
        $query .= "            SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT  ";
        $query .= "            FROM    T_ATTEND_DAT W1  ";
        $query .= "            WHERE   W1.DI_CD NOT IN ('0','14','15','16','23','24','29','30','31','32')      ";
        $query .= "            GROUP BY W1.SCHREGNO, W1.ATTENDDATE  ";
        $query .= "            )T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.EXECUTEDATE = T1.ATTENDDATE AND  ";
        $query .= "                                                 T0.PERIOD_CNT != T1.PERIOD_CNT  ";
        $query .= "        INNER JOIN( ";
        $query .= "            SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD  ";
        $query .= "            FROM    T_ATTEND_DAT  ";
        $query .= "            WHERE   DI_CD IN ('4','5','6')                            ";
        $query .= "            )T3 ON T0.SCHREGNO= T3.SCHREGNO AND T0.EXECUTEDATE = T3.ATTENDDATE AND  ";
        $query .= "                                                T0.LAST_PERIOD = T3.PERIODCD  ";
        $query .= "    ) ";

        //個人別早退２
        $query .= ",early_data2 as( ";
        $query .= "    SELECT  T0.SCHREGNO, T2.ATTENDDATE ";
        $query .= "    FROM    T_PERIOD_CNT T0  ";
        $query .= "        INNER JOIN( ";
        $query .= "            SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD  ";
        $query .= "            FROM    T_ATTEND_DAT  ";
        $query .= "            WHERE   DI_CD IN ('16','30','31','32')  ";
        $query .= "            )T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND  ";
        $query .= "                                                 T0.LAST_PERIOD = T2.PERIODCD  ";
        $query .= "    ) ";


        //メインの表
        $query .=    "SELECT  TT0.SCHREGNO, TT0.SEMESTER, ";
        $query .=            "VALUE(TT1.count,0) AS SHR ";
        $query .=           ",VALUE(TT2.NOT_SHR,0) + VALUE(TT7.NOT_SHR,0) AS NOT_SHR ";
        $query .=    "FROM    SCHNO TT0 ";

        //個人別遅刻回数ＳＨＲ(遅刻度数)
        $query .=    "LEFT OUTER JOIN(";
        $query .=          "select t1.schregno, value(t1.semester,'9') as semester, SUM(SMALLINT(VALUE(MULTIPLY, '1'))) as count ";
        $query .=          "from   shr_ATTEND_DATa t1 ";
        $query .=          "where not exists(select 'x' from suspend_MOURNING_data t2 where t2.schregno = t1.schregno and t2.attenddate = t1.attenddate) ";
        $query .=          "  and not exists(select 'x' from absent_data t2 where t2.schregno = t1.schregno and t2.attenddate = t1.attenddate) ";
        $query .=          "  and not exists(select 'x' from early_data1 t2 where t2.schregno = t1.schregno and t2.attenddate = t1.attenddate) ";
        $query .=          "  and not exists(select 'x' from early_data2 t2 where t2.schregno = t1.schregno and t2.attenddate = t1.attenddate) ";
        $query .=          "group by grouping sets((t1.schregno,t1.semester),t1.schregno) ";
        $query .=       ") TT1 ON TT0.SCHREGNO = TT1.SCHREGNO AND TT0.SEMESTER = TT1.SEMESTER ";

        //個人別遅刻回数授業
        $query .=    "LEFT OUTER JOIN(";
        $query .=          "select t1.schregno, value(t1.semester,'9') as semester, SUM(SMALLINT(VALUE(MULTIPLY, '1'))) as NOT_SHR ";
        $query .=          "from   shr_ATTEND_DATb t1 ";
        $query .=          "group by grouping sets((t1.schregno,t1.semester),t1.schregno) ";
        $query .=       ") TT2 ON TT0.SCHREGNO = TT2.SCHREGNO AND TT0.SEMESTER = TT2.SEMESTER ";

        //科目別集計データから集計した表
        $query .=    "LEFT JOIN(";
        $query .=       "SELECT  SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, ";
        $query .=               "SUM(LATE) AS NOT_SHR ";
        $query .=       "FROM    ATTEND_SUBCLASS_DAT W1 ";
        $query .=       "WHERE   YEAR = '".CTRL_YEAR."' AND ";
        $query .=               "SEMESTER <= '".CTRL_SEMESTER."' AND ";
        $query .=               "CLASSCD <> '92' AND ";
        if ($cntmonth > 0) {
            $query .=           "W1.MONTH IN ('".$isomonth."') AND ";
        } else {
            /********************************************/
            /* 集計するべきデータがない場合             */
            /* レコードが取れるはずがない条件を追加する */
            /* セレクト分に if 文を入れると             */
            /* ごちゃごちゃしそうなので、こうします。   */
            /********************************************/
            $query .=           "SEMESTER < '1' AND ";
        }
        $query .=               "EXISTS(SELECT  'X' ";
        $query .=                      "FROM    SCHNO W2 ";
        $query .=                      "WHERE   W1.SCHREGNO = W2.SCHREGNO)";
        $query .=       "GROUP BY GROUPING SETS((SCHREGNO,SEMESTER),SCHREGNO) ";
        $query .=       ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO AND TT0.SEMESTER = TT7.SEMESTER ";

        $query .= "ORDER BY TT0.SCHREGNO ,TT0.SEMESTER";

        return $query;
    }

    //出欠データ参照---SubForm1
    function getAttendRemark($model)
    {
        //対象生徒の時間割データ ＳＨＲ
        $query  = "WITH SCHEDULE_SCHREG AS( ";
        $query .=      "SELECT  T2.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD  ";
        $query .=      "FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2, CHAIR_DAT T3  ";
        $query .=      "WHERE   T1.YEAR = '".CTRL_YEAR."'  ";
        $query .=          "AND T1.SEMESTER <= '".CTRL_SEMESTER."'  ";
        $query .=          "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE  ";
        $query .=          "AND T1.EXECUTEDATE BETWEEN '".CTRL_YEAR."-04-01"."' AND '".CTRL_DATE."'  ";
        $query .=          "AND T1.YEAR = T2.YEAR  ";
        $query .=          "AND T1.SEMESTER = T2.SEMESTER  ";
        $query .=          "AND T1.CHAIRCD = T2.CHAIRCD  ";
        $query .=          "AND T2.SCHREGNO = '".$model->schregno."' ";
        $query .=          "AND T1.YEAR = T3.YEAR  ";
        $query .=          "AND T1.SEMESTER = T3.SEMESTER  ";
        $query .=          "AND T1.CHAIRCD = T3.CHAIRCD  ";
        $query .=          "AND SUBSTR(T3.SUBCLASSCD,1,2) = '92' ";
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
        $query .=      "GROUP BY T2.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD  ";
        $query .=      ")  ";

        //メインの表
        //対象生徒の出欠データ(備考)
        $query .=      "SELECT  T0.SCHREGNO, T1.SEMESTER, T0.ATTENDDATE, T0.PERIODCD, L1.REP_DI_CD AS DI_CD, T0.DI_REMARK ";
        $query .=      "FROM    ATTEND_DAT T0  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T0.YEAR AND L1.DI_CD = T0.DI_CD, ";
        $query .=              "SCHEDULE_SCHREG T1  ";
        $query .=      "WHERE   T0.YEAR = '".CTRL_YEAR."' AND  ";
        $query .=              "T0.ATTENDDATE BETWEEN '".CTRL_YEAR."-04-01"."' AND '".CTRL_DATE."' AND  ";
        $query .=              "T0.DI_REMARK IS NOT NULL AND  ";
        $query .=              "T0.DI_REMARK <> '' AND  ";
        $query .=              "T0.SCHREGNO = T1.SCHREGNO AND  ";
        $query .=              "T0.ATTENDDATE = T1.EXECUTEDATE AND  ";
        $query .=              "T0.PERIODCD = T1.PERIODCD  ";
        $query .=     "ORDER BY T1.SEMESTER, T0.ATTENDDATE DESC, T0.PERIODCD ";

        return $query;
    }

}
?>
