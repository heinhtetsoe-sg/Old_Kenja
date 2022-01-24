<?php

require_once('for_php7.php');

class knjd133aQuery extends Query {

    //学年コンボ
    function getSelectGrade($model) {
        $query  = "SELECT ";
        $query .= "    GRADE_NAME1 AS LABEL, ";
        $query .= "    GRADE AS VALUE ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY GRADE ";

        return $query;
    }
    //科目一覧
    function getSubclassList($model) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE, ";
            $query .= "     SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    (SUBSTR(SUBCLASSCD,1,2) = '90' OR ";
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
            $query .= "             IN (SELECT ";
            $query .= "                     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
            $query .= "                 FROM ";
            $query .= "                     SUBCLASS_DETAIL_DAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                     SUBCLASS_SEQ = '009')) ";
        } else {
            $query .= "     SUBCLASSCD IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'D051')) ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       CLASSCD, ";
            $query .= "       SCHOOL_KIND, ";
            $query .= "       CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD ";
        return $query;
    }
    //実行履歴一覧
    function getListRireki($year, $model) {
        $query  = " WITH T_CREDIT_CALC(CREDIT_CALC, CREDIT_CALC_NAME) AS ( ";
        $query .= "     VALUES('1','欠課時数') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2','全員') ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.GRADE_NAME1, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     T1.CREDIT_CALC, ";
        $query .= "     L2.CREDIT_CALC_NAME, ";
        $query .= "     T1.ATTEND_DATE ";
        $query .= " FROM ";
        $query .= "     NV_CCALC_EXEC_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = T1.YEAR AND G1.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "                                 AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                 AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN T_CREDIT_CALC L2 ON L2.CREDIT_CALC = T1.CREDIT_CALC ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";
        return $query;
    }

    //科目名（エラーメッセージ用）
    function getSubclassName($subclasscd, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
        }
        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$subclass_array[0]."' AND ";
            $query .= "     SCHOOL_KIND     = '".$subclass_array[1]."' AND ";
            $query .= "     CURRICULUM_CD   = '".$subclass_array[2]."' AND ";
            $query .= "     SUBCLASSCD      = '".$subclass_array[3]."' ";
        } else {
            $query .= "     SUBCLASSCD = '{$subclasscd}' ";
        }
        return $query;
    }
    //科目の講座名簿の存在チェック
    function getExistsChair($year, $sem, $subclasscd, $grade, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
        }
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
            $query .= "     T2.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     T2.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     T2.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     T2.SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "     T2.SUBCLASSCD = '{$subclasscd}' AND ";
        }
        $query .= "     T1.SCHREGNO   IN (SELECT ";
        $query .= "                           R1.SCHREGNO ";
        $query .= "                       FROM ";
        $query .= "                           SCHREG_REGD_DAT R1 ";
        $query .= "                       WHERE ";
        $query .= "                           R1.YEAR = '{$year}' AND ";
        $query .= "                           R1.SEMESTER = '{$sem}' AND ";
        $query .= "                           R1.GRADE = '{$grade}') ";
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
    //実行履歴データ・追加
    function getInsertRireki($model, $calcDate, $calcTime, $subclasscd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
        }
        $data = array();
        $data["CALC_DATE"][TEXT]    = $calcDate;
        $data["CALC_TIME"][TEXT]    = $calcTime;
        $data["GRADE"][TEXT]        = $model->field["GRADE"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]   = $subclasscd;
        }
        $data["CREDIT_CALC"][TEXT]  = $model->field["SHORI"];
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["ATTEND_DATE"][TEXT]  = str_replace("/", "-", $model->field["ATTEND_DATE"]);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "NV_CCALC_EXEC_DAT");
        return $query;
    }
    //①RECORD_DATに存在しない生徒を追加
    function getInsertNonStdRec($year, $sem, $subclasscd, $grade, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
        }
        $staffcd = STAFFCD;
        $query  = " INSERT INTO RECORD_DAT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " (YEAR,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,TAKESEMES,SCHREGNO,REGISTERCD,UPDATED) ";
        } else {
            $query .= " (YEAR,SUBCLASSCD,TAKESEMES,SCHREGNO,REGISTERCD,UPDATED) ";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     '0' AS TAKESEMES, ";
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
            $query .= "    T2.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "    T2.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "    T2.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "    T2.SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    T2.SUBCLASSCD = '{$subclasscd}' AND ";
        }
        $query .= "     T1.SCHREGNO   IN (SELECT ";
        $query .= "                           R1.SCHREGNO ";
        $query .= "                       FROM ";
        $query .= "                           SCHREG_REGD_DAT R1 ";
        $query .= "                       WHERE ";
        $query .= "                           R1.YEAR = '{$year}' AND ";
        $query .= "                           R1.SEMESTER = '{$sem}' AND ";
        $query .= "                           R1.GRADE = '{$grade}') AND ";
        $query .= "     T1.SCHREGNO NOT IN (SELECT ";
        $query .= "                             R1.SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             RECORD_DAT R1 ";
        $query .= "                         WHERE ";
        $query .= "                             R1.YEAR = '{$year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             R1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "                             R1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "                             R1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "                             R1.SUBCLASSCD       = '".$subclass_array[3]."') ";
        } else {
            $query .= "                             R1.SUBCLASSCD = '{$subclasscd}') ";
        }
        return $query;
    }
    //科目毎・生徒毎に科目の単位を自動計算し、RECORD_DATに登録する。
    function getCreditUpdateList($model, $year, $sem, $subclasscd) {
        $attendDate = str_replace("/", "-", $model->field["ATTEND_DATE"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
        }
        $query  = "";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     T4.CREDITS AS COMP_CREDIT, ";
        $query .= "     T4.CREDITS AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN RECORD_DAT T3 ON T3.YEAR = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND T3.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "                             AND T3.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "                             AND T3.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "                             AND T3.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "                             AND T3.SUBCLASSCD = '{$subclasscd}' ";
        }
        $query .= "                             AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND T3.GRAD_VALUE IS NULL "; //評定がNULL
        $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '{$year}' ";
        $query .= "                            AND T4.COURSECD = T1.COURSECD ";
        $query .= "                            AND T4.MAJORCD = T1.MAJORCD ";
        $query .= "                            AND T4.GRADE = T1.GRADE ";
        $query .= "                            AND T4.COURSECODE = T1.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND T4.CLASSCD = T3.CLASSCD ";
            $query .= "                             AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                             AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        } else {
            $query .= "                            AND T4.SUBCLASSCD = T3.SUBCLASSCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$sem}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "     AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6 ";
        $query .= "                    WHERE   T6.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                            T6.GRD_DIV IN ('2','3') AND ";
        $query .= "                            T6.GRD_DATE < DATE('{$attendDate}') ) ";
        $query .= "     AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T7 ";
        $query .= "                    WHERE   T7.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                            T7.TRANSFERCD IN ('1','2') AND ";
        $query .= "                            DATE('{$attendDate}') BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        return $query;
    }
    //更新
    function getUpdateRecordDat($year, $subCd, $schno, $gradValue, $compCredit, $getCredit, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subCd);
        }
        $data = array();

        $data["GRAD_VALUE"][NUMBER]     = $gradValue;
        $data["COMP_CREDIT"][NUMBER]    = $compCredit;
        $data["GET_CREDIT"][NUMBER]     = $getCredit;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";
        
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where =  " WHERE "
                     ."     YEAR = '{$year}' AND "
                     ."     CLASSCD          = '".$subclass_array[0]."' AND "
                     ."     SCHOOL_KIND      = '".$subclass_array[1]."' AND "
                     ."     CURRICULUM_CD    = '".$subclass_array[2]."' AND "
                     ."     SUBCLASSCD       = '".$subclass_array[3]."' AND "
                     ."     SCHREGNO = '{$schno}' ";
        } else {
            $where =  " WHERE "
                     ."     YEAR = '{$year}' AND "
                     ."     SUBCLASSCD = '{$subCd}' AND "
                     ."     SCHREGNO = '{$schno}' ";
        }
        $query = Query::updateSQL($data, "RECORD_DAT", $where);
        return $query;
    }
/******************************************************************************************/
/******************************************************************************************/
/******************************************************************************************/
    //出欠集計開始日付などを取得(端数処理に使用)
    function getAttendDate() {
        $semester = CTRL_SEMESTER;
        $query  = "SELECT SEMESTER ";
        $query .= "      ,MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR ";
        $query .= "      ,MONTH ";
        $query .= "      ,MAX(APPOINTED_DAY) AS MAX_APP ";
        $query .= "FROM   ATTEND_SEMES_DAT ";
        $query .= "WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER <= '{$semester}' ";
        $query .= "GROUP BY SEMESTER,MONTH ";
        $query .= "ORDER BY 2,3 ";
        return $query;
    }
    //学校マスタ(欠課時数を取得に使用)
    function getSchoolMst($year) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        return $query;
    }
    //履修上限値を超えた生徒・科目
    function getRishuJougenOver($model, $attend_seme, $attend_month, $attend_sdate, $knjSchoolMst) {
        $semester = CTRL_SEMESTER;
        $date = str_replace("/","-",$model->field["ATTEND_DATE"]);
        $absent_cov      = $knjSchoolMst["ABSENT_COV"];
        $absent_cov_late = $knjSchoolMst["ABSENT_COV_LATE"];
        $amari_kuriage   = $knjSchoolMst["AMARI_KURIAGE"];
        $year = CTRL_YEAR;

        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '{$year}' ";
        $query .= "         AND SEMESTER = '{$semester}' ";
        $query .= "         AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= " ) ";

        //集計
        $query .= " ,ATTEND_SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR = '{$year}' ";
        $query .= "         AND SEMESTER <= '{$attend_seme}' ";
        $query .= "         AND MONTH IN ('". implode($attend_month, "','") ."') ";
        $query .= " ) ";

        $query .= " ,SCHEDULE AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXECUTEDATE, ";
        $query .= "         PERIODCD, ";
        $query .= "         CHAIRCD, ";
        $query .= "         DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT ";
        $query .= "     WHERE ";
        $query .= "             EXECUTEDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "         AND PERIODCD != '0' ";
        $query .= " ) ";

        $query .= " ,T_attend_dat AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        $query .= "         W1.ATTENDDATE, ";
        $query .= "         W1.PERIODCD, ";
        $query .= "         CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END AS DI_CD, ";
        $query .= "         L1.MULTIPLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAT W1 ";
        $query .= "         LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = W1.YEAR AND L1.DI_CD = W1.DI_CD ";
        $query .= "     WHERE ";
        $query .= "             W1.ATTENDDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "         AND NOT EXISTS( SELECT ";
        $query .= "                             'X' ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_TRANSFER_DAT T7 ";
        $query .= "                         WHERE ";
        $query .= "                                 T7.SCHREGNO = W1.SCHREGNO ";
        $query .= "                             AND T7.TRANSFERCD IN('1','2') ";
        $query .= "                             AND W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        $query .= " ) ";

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

        //端数
        $query .= " ,T_attend AS ( ";
        $query .= "     SELECT ";
        $query .= "         S1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     S1.CLASSCD, ";
            $query .= "     S1.SCHOOL_KIND, ";
            $query .= "     S1.CURRICULUM_CD, ";
        }
        $query .= "         S1.SUBCLASSCD, ";
        $query .= "         S1.SEMESTER, ";
        $query .= "         COUNT(S3.SCHREGNO)  AS OFFDAYS, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8'  THEN 1 ELSE 0 END)  AS  ABSENT, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9'  THEN 1 ELSE 0 END)  AS  SUSPEND, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF, ";
        $query .= "         SUM(CASE WHEN S2.DI_CD IN('15','23','24') THEN SMALLINT(VALUE(S2.MULTIPLY, '1')) ELSE 0 END)  AS  LATE,  ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '25' THEN 1 WHEN '26' THEN 1 ELSE 0 END)  AS  KOUDOME  ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "             SUBCLASSCD, ";
        $query .= "             T2.SEMESTER ";
        $query .= "         FROM ";
        $query .= "             SCHEDULE T1, ";
        $query .= "             CHAIR_STD_DAT T2, ";
        $query .= "             CHAIR_DAT T3, ";
        $query .= "             SCHNO T4 ";
        $query .= "         WHERE ";
        $query .= "                 T1.CHAIRCD  = T3.CHAIRCD ";
        $query .= "             AND T3.YEAR     = '{$year}' ";
        $query .= "             AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "             AND T2.YEAR     = '{$year}' ";
        $query .= "             AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "             AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "             AND T4.SCHREGNO = T2.SCHREGNO ";
        $query .= "             AND NOT EXISTS( SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SCH_CHR_COUNTFLG T5 ";
        $query .= "                             WHERE ";
        $query .= "                                 T5.EXECUTEDATE  = T1.EXECUTEDATE AND ";
        $query .= "                                 T5.PERIODCD     = T1.PERIODCD AND ";
        $query .= "                                 T5.CHAIRCD      = T1.CHAIRCD AND ";
        $query .= "                                 T5.GRADE        = T4.GRADE AND ";
        $query .= "                                 T5.HR_CLASS     = T4.HR_CLASS AND ";
        $query .= "                                 T1.DATADIV      IN ('0','1') AND "; //テスト(DATADIV=2)以外
        $query .= "                                 T5.COUNTFLG     = '0' ";
        $query .= "                           ) ";
        $query .= "             AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
        $query .= "                             WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                               AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "                               AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "                               AND TEST.DATADIV     = T1.DATADIV) "; //テスト(DATADIV=2)
        $query .= "             AND NOT EXISTS( SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SCHREG_BASE_MST T6 ";
        $query .= "                             WHERE ";
        $query .= "                                 T6.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "                                 ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                  (T6.ENT_DIV IN('4','5')     AND T6.ENT_DATE > T1.EXECUTEDATE)) ";
        $query .= "                           ) ";
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
        $query .= "         GROUP BY ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "             SUBCLASSCD, ";
        $query .= "             T2.SEMESTER)S1 ";
        $query .= "     LEFT JOIN T_attend_dat        S2 ON  S2.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S2.ATTENDDATE = S1.EXECUTEDATE ";
        $query .= "                                      AND S2.PERIODCD   = S1.PERIODCD ";
        $query .= "     LEFT JOIN SCHREG_TRANSFER_DAT S3 ON  S3.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S3.TRANSFERCD = '2' ";
        $query .= "                                      AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE ";
        $query .= "     LEFT JOIN SCHREG_TRANSFER_DAT S4 ON  S4.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S4.TRANSFERCD = '1' ";
        $query .= "                                      AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE ";
        $query .= "     GROUP BY ";
        $query .= "         S1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     S1.CLASSCD, ";
            $query .= "     S1.SCHOOL_KIND, ";
            $query .= "     S1.CURRICULUM_CD, ";
        }
        $query .= "         S1.SUBCLASSCD, ";
        $query .= "         S1.SEMESTER ";
        $query .= " ) ";

        //集計と端数
        $query .= " ,ATTEND_SUM AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     W1.CLASSCD, ";
            $query .= "     W1.SCHOOL_KIND, ";
            $query .= "     W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.SEMESTER, ";
        $query .= "         SUM(VALUE(W1.SICK,0) + VALUE(W1.NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .=           "+ VALUE(W1.ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .=           "+ VALUE(W1.SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .=           "+ VALUE(W1.MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .=           "+ VALUE(W1.OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .=           "+ VALUE(W1.VIRUS,0)";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .=           "+ VALUE(W1.KOUDOME,0)";
        }
        $query .= "            ) AS SICK, ";
        $query .= "         SUM(VALUE(W1.NOTICE,0))     NOTICE, ";
        $query .= "         SUM(VALUE(W1.NONOTICE,0))   NONOTICE, ";
        $query .= "         SUM(VALUE(W1.ABSENT,0))     ABSENT, ";
        $query .= "         SUM(VALUE(W1.SUSPEND,0))    SUSPEND, ";
        if ($model->virus) {
            $query .= "           SUM(VALUE(W1.VIRUS, 0))   VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           SUM(VALUE(W1.KOUDOME, 0)) KOUDOME, ";
        }
        $query .= "         SUM(VALUE(W1.MOURNING,0))   MOURNING, ";
        $query .= "         SUM(VALUE(W1.LATE,0))       LATE, ";
        $query .= "         SUM(VALUE(W1.EARLY,0))      EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS W1, ";
        $query .= "         SCHNO W0 ";
        $query .= "     WHERE ";
        $query .= "         W1.SCHREGNO = W0.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         W1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     W1.CLASSCD, ";
            $query .= "     W1.SCHOOL_KIND, ";
            $query .= "     W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.SEMESTER ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         W2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     W2.CLASSCD, ";
            $query .= "     W2.SCHOOL_KIND, ";
            $query .= "     W2.CURRICULUM_CD, ";
        }
        $query .= "         W2.SUBCLASSCD, ";
        $query .= "         W2.SEMESTER, ";
        $query .= "         VALUE(W2.SICK,0) + VALUE(W2.NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .= "          + VALUE(W2.ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .= "          + VALUE(W2.SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .= "          + VALUE(W2.MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "          + VALUE(W2.OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .= "          + VALUE(W2.VIRUS,0)";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .=           "+ VALUE(W2.KOUDOME,0)";
        }
        $query .= "                AS SICK, ";
        $query .= "         VALUE(W2.NOTICE,0)      NOTICE, ";
        $query .= "         VALUE(W2.NONOTICE,0)    NONOTICE, ";
        $query .= "         VALUE(W2.ABSENT,0)      ABSENT, ";
        $query .= "         VALUE(W2.SUSPEND,0)     SUSPEND, ";
        if ($model->virus) {
            $query .= "           VALUE(W2.VIRUS, 0))   VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           VALUE(W2.KOUDOME, 0)) KOUDOME, ";
        }
        $query .= "         VALUE(W2.MOURNING,0)    MOURNING, ";
        $query .= "         VALUE(W2.LATE,0)        LATE, ";
        $query .= "         VALUE(W2.EARLY,0)       EARLY ";
        $query .= "     FROM ";
        $query .= "         T_attend W2 ";
        $query .= " ) ";

        //学期毎に清算
        $query .= " ,ATTEND_SUM2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SEMESTER, ";
        $query .= "         SUM(SICK)       SICK, ";
        $query .= "         SUM(NOTICE)     NOTICE, ";
        $query .= "         SUM(NONOTICE)   NONOTICE, ";
        $query .= "         SUM(ABSENT)     ABSENT, ";
        $query .= "         SUM(SUSPEND)    SUSPEND, ";
        if ($model->virus) {
            $query .= "           SUM(VIRUS)   VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           SUM(KOUDOME) KOUDOME, ";
        }
        $query .= "         SUM(MOURNING)   MOURNING, ";
        $query .= "         SUM(LATE)       LATE, ";
        $query .= "         SUM(EARLY)      EARLY, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
            $query .= "     (SUM(LATE) + SUM(EARLY)) AS TIKOKU_SOUTAI, ";
        } else {
            if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     0 AS TIKOKU_SOUTAI, ";
            } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     MOD((sum(LATE) + sum(EARLY)), {$absent_cov_late}) AS TIKOKU_SOUTAI, ";
            } else {
                $query .= "     (SUM(LATE) + SUM(EARLY)) AS TIKOKU_SOUTAI, ";
            }
        }
        if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      decimal((float(sum(LATE) + sum(EARLY)) / {$absent_cov_late}) + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)),4,1) as NOTICE_LATE, ";
        } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ((sum(LATE) + sum(EARLY)) / {$absent_cov_late}) + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)) as NOTICE_LATE, ";
        } else {
            $query .= "      sum(NOTICE) + sum(NONOTICE) + sum(SICK) as NOTICE_LATE, ";
        }
        $query .= "         SUM(LATE) + SUM(EARLY) + SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE3 ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM ";
        $query .= "     WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN ( ";
        } else {
            $query .= "         SUBCLASSCD NOT IN ( ";
        }
        $query .= "             SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
        } else {
            $query .= "                 COMBINED_SUBCLASSCD ";
        }
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '{$year}') ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SEMESTER ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.COMBINED_CLASSCD AS CLASSCD, ";
            $query .= "         T1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "         T1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ";
        }
        $query .= "         T1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "         T2.SEMESTER, ";
        $query .= "         SUM(T2.SICK)        SICK, ";
        $query .= "         SUM(T2.NOTICE)      NOTICE, ";
        $query .= "         SUM(T2.NONOTICE)    NONOTICE, ";
        $query .= "         SUM(T2.ABSENT)      ABSENT, ";
        $query .= "         SUM(T2.SUSPEND)     SUSPEND, ";
        if ($model->virus) {
            $query .= "           SUM(T2.VIRUS)   VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           SUM(T2.KOUDOME) KOUDOME, ";
        }
        $query .= "         SUM(T2.MOURNING)    MOURNING, ";
        $query .= "         SUM(T2.LATE)        LATE, ";
        $query .= "         SUM(T2.EARLY)       EARLY, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
            $query .= "     (SUM(T2.LATE) + SUM(T2.EARLY)) AS TIKOKU_SOUTAI, ";
        } else {
            if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     0 AS TIKOKU_SOUTAI, ";
            } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     MOD((sum(T2.LATE) + sum(T2.EARLY)), {$absent_cov_late}) AS TIKOKU_SOUTAI, ";
            } else {
                $query .= "     (SUM(T2.LATE) + SUM(T2.EARLY)) AS TIKOKU_SOUTAI, ";
            }
        }
        if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      decimal((float(sum(T2.LATE) + sum(T2.EARLY)) / {$absent_cov_late}) + (sum(T2.NOTICE) + sum(T2.NONOTICE) + sum(T2.SICK)),4,1) as NOTICE_LATE, ";
        } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ((sum(T2.LATE) + sum(T2.EARLY)) / {$absent_cov_late}) + (sum(T2.NOTICE) + sum(T2.NONOTICE) + sum(T2.SICK)) as NOTICE_LATE, ";
        } else {
            $query .= "      sum(T2.NOTICE) + sum(T2.NONOTICE) + sum(T2.SICK) as NOTICE_LATE, ";
        }
        $query .= "         SUM(LATE) + SUM(EARLY) + SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE3 ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "         ATTEND_SUM T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.ATTEND_CLASSCD       = T2.CLASSCD AND ";
            $query .= "         T1.ATTEND_SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
            $query .= "         T1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "         T1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     GROUP BY ";
        $query .= "         T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.COMBINED_CLASSCD, ";
            $query .= "         T1.COMBINED_SCHOOL_KIND, ";
            $query .= "         T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "         T1.COMBINED_SUBCLASSCD, ";
        $query .= "         T2.SEMESTER ";
        $query .= " ) ";

        //年間で清算
        $query .= " ,ATTEND_SUM3 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUM(SICK)       SICK, ";
        $query .= "         SUM(NOTICE)     NOTICE, ";
        $query .= "         SUM(NONOTICE)   NONOTICE, ";
        $query .= "         SUM(ABSENT)     ABSENT, ";
        $query .= "         SUM(SUSPEND)    SUSPEND, ";
        if ($model->virus) {
            $query .= "           SUM(VIRUS)   VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           SUM(KOUDOME) KOUDOME, ";
        }
        $query .= "         SUM(MOURNING)   MOURNING, ";
        $query .= "         SUM(LATE)       LATE, ";
        $query .= "         SUM(EARLY)      EARLY, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS KETUJISU, ";
        if ($model->Properties["chikokuHyoujiFlg"] == "1") {
                $query .= "     SUM(LATE) + SUM(EARLY) AS TIKOKU_SOUTAI, ";
        } else {
            if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     SUM(TIKOKU_SOUTAI) TIKOKU_SOUTAI, ";
            } else {
                if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "      0 AS TIKOKU_SOUTAI, ";
                } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "      MOD((SUM(LATE) + SUM(EARLY)), {$absent_cov_late}) AS TIKOKU_SOUTAI, ";
                } elseif (($absent_cov == "5") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "      CASE WHEN MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) >= {$amari_kuriage} ";
                    $query .= "           THEN MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) -  {$amari_kuriage} ";
                    $query .= "           ELSE MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) ";
                    $query .= "      END AS TIKOKU_SOUTAI, ";
                } else {
                    $query .= "      SUM(LATE) + SUM(EARLY) AS TIKOKU_SOUTAI, ";
                }
            }
        }
        if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "     SUM(NOTICE_LATE) NOTICE_LATE, "; //欠課
        } else {
            if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "      DECIMAL((FLOAT(SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)),4,1) AS NOTICE_LATE, ";
            } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "      ((SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) AS NOTICE_LATE, ";
            } elseif (($absent_cov == "5") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "      ((SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) + (CASE WHEN MOD((SUM(LATE) + SUM(EARLY)) , {$absent_cov_late}) >= {$amari_kuriage} ";
                $query .= "                                                                                                          THEN 1 ";
                $query .= "                                                                                                          ELSE 0 ";
                $query .= "                                                                                                     END) AS NOTICE_LATE, ";
            } else {
                $query .= "      SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE, ";
            }
        }
        $query .= "         SUM(LATE) + SUM(EARLY) + SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ";
        $query .= "         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE3 ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM2 ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";

        //上限値を取得
        $query .= " ), T_ABSENCE AS ( ";
            if ($knjSchoolMst["JUGYOU_JISU_FLG"] == '1') { //1：法定時数 2：実時数
                $query .= " SELECT ";
                $query .= "     T2.*, ";
                $query .= "     L1.ABSENCE_HIGH     AS ABSENCE_HIGH, ";
                $query .= "     L1.GET_ABSENCE_HIGH AS GET_ABSENCE_HIGH, ";
                $query .= "     L1.ABSENCE_HIGH     - VALUE(L1.ABSENCE_WARN_RISHU_SEM{$semester}, 0)    AS RISHU_WARN, ";
                $query .= "     L1.GET_ABSENCE_HIGH - VALUE(L1.ABSENCE_WARN_SHUTOKU_SEM{$semester}, 0)  AS SHUTOKU_WARN ";
                $query .= " FROM ";
                $query .= "     SCHNO T1 ";
                $query .= " INNER JOIN ";
                $query .= "     ATTEND_SUM3 T2 ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT  JOIN ";
                $query .= "     V_CREDIT_MST L1 ON  L1.YEAR     = '{$year}' ";
                $query .= "                   AND L1.COURSECD   = T1.COURSECD ";
                $query .= "                   AND L1.MAJORCD    = T1.MAJORCD ";
                $query .= "                   AND L1.GRADE      = T1.GRADE ";
                $query .= "                   AND L1.COURSECODE = T1.COURSECODE ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                 AND L1.CLASSCD       = T2.CLASSCD ";
                    $query .= "                 AND L1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
                    $query .= "                 AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ";
                }
                $query .= "                   AND L1.SUBCLASSCD = T2.SUBCLASSCD ";
            } else {
                $query .= " SELECT ";
                $query .= "     T2.*, ";
                $query .= "     L1.COMP_ABSENCE_HIGH AS ABSENCE_HIGH, ";
                $query .= "     L1.GET_ABSENCE_HIGH  AS GET_ABSENCE_HIGH, ";
                $query .= "     L1.COMP_ABSENCE_HIGH - VALUE(L2.ABSENCE_WARN_RISHU_SEM{$semester}, 0)   AS RISHU_WARN, ";
                $query .= "     L1.GET_ABSENCE_HIGH  - VALUE(L2.ABSENCE_WARN_SHUTOKU_SEM{$semester}, 0) AS SHUTOKU_WARN ";
                $query .= " FROM ";
                $query .= "     SCHNO T1 ";
                $query .= " INNER JOIN ";
                $query .= "     ATTEND_SUM3 T2 ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT  JOIN ";
                $query .= "     SCHREG_ABSENCE_HIGH_DAT L1 ON  L1.YEAR       = '{$year}' ";
                $query .= "                                AND L1.SCHREGNO   = T1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                             AND L1.CLASSCD       = T2.CLASSCD ";
                    $query .= "                             AND L1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
                    $query .= "                             AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ";
                }
                $query .= "                                AND L1.SUBCLASSCD = T2.SUBCLASSCD ";
                $query .= "                                AND L1.DIV        = '2' ";
                $query .= " LEFT  JOIN ";
                $query .= "     V_CREDIT_MST L2 ON  L2.YEAR     = '{$year}' ";
                $query .= "                   AND L2.COURSECD   = T1.COURSECD ";
                $query .= "                   AND L2.MAJORCD    = T1.MAJORCD ";
                $query .= "                   AND L2.GRADE      = T1.GRADE ";
                $query .= "                   AND L2.COURSECODE = T1.COURSECODE ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "                 AND L2.CLASSCD       = T2.CLASSCD ";
                    $query .= "                 AND L2.SCHOOL_KIND   = T2.SCHOOL_KIND ";
                    $query .= "                 AND L2.CURRICULUM_CD = T2.CURRICULUM_CD ";
                }
                $query .= "                   AND L2.SUBCLASSCD = T2.SUBCLASSCD ";
            }

        $query .= " ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     T_ABSENCE T1 ";
        $query .= " WHERE ";
        $query .= "     0 < T1.ABSENCE_HIGH AND T1.ABSENCE_HIGH < T1.NOTICE_LATE AND "; //履修・超過
        $query .= "    (SUBSTR(T1.SUBCLASSCD,1,2) = '90' OR ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            $query .= "             IN (SELECT ";
            $query .= "                     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
            $query .= "                 FROM ";
            $query .= "                     SUBCLASS_DETAIL_DAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                     SUBCLASS_SEQ = '009')) ";
        } else {
            $query .= "     T1.SUBCLASSCD IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'D051')) ";
        }

        return $query;
    }
}
?>
