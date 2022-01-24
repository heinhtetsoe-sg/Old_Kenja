<?php

require_once('for_php7.php');

class knjd214Query extends Query {

    //年組リスト
    function getAuth($model)
    {
        $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR = '" .CTRL_YEAR."'";
        $query .= "  AND SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["GRADE"] != "999") {
            $query .= "  AND GRADE = '".$model->field["GRADE"] ."'";
        }
        $query .= "ORDER BY GRADE, HR_CLASS ";

        return $query;
    }

    //学年コンボ
    function getSelectGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //テスト種別取得
    function getTestItem($model, $sem)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
//        $query .= "     TESTITEMNAME AS LABEL ";
        $query .= "     rtrim(ltrim(substr(TESTITEMNAME,1,9))) as LABEL "; //頭全角３文字
        $query .= " FROM ";
        $query .=       $model->testTable ;
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     AND SEMESTER = '".$sem."' ";
        }
        $query .= " AND TESTKINDCD || TESTITEMCD IN ('0101','0201','0202') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //処理対象レコード
    function getClearSql($model, $year, $sem) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);

        $query  = "";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T3.CLASSCD, ";
            $query .= " T3.SCHOOL_KIND, ";
            $query .= " T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND (T2.GRD_DATE IS NULL OR ";
        $query .= "                                       T2.GRD_DIV IS NULL OR ";
        $query .= "                                       T2.GRD_DIV = '4') ";
        $query .= "     INNER JOIN RECORD_SCORE_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND T3.YEAR = '{$year}' ";
        $query .= "                             AND T3.SEMESTER = '9' ";
        $query .= "                             AND T3.TESTKINDCD = '99' ";
        $query .= "                             AND T3.TESTITEMCD = '00' ";
        $query .= "                             AND T3.SCORE_DIV = '00' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        return $query;
    }

    //クリア処理
    function getClearQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = knjd214Query::getClearSql($model, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $classCd        = $row["CLASSCD"];
                $schoolKind     = $row["SCHOOL_KIND"];
                $curriculumCd   = $row["CURRICULUM_CD"];
            }
            $subCd      = $row["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            //RECORD_SCORE_DATのレコードを削除。
            $query = knjd214Query::getDeleteRecordDat(CTRL_YEAR, "9", "99", "00", "00", $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //クリア処理が行われたかチェック
    function getClearExists($model, $year, $sem) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND (T2.GRD_DATE IS NULL OR ";
        $query .= "                                       T2.GRD_DIV IS NULL OR ";
        $query .= "                                       T2.GRD_DIV = '4') ";
        $query .= "     INNER JOIN RECORD_SCORE_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND T3.YEAR = '{$year}' ";
        $query .= "                             AND T3.SEMESTER = '9' ";
        $query .= "                             AND T3.TESTKINDCD = '99' ";
        $query .= "                             AND T3.TESTITEMCD = '00' ";
        $query .= "                             AND T3.SCORE_DIV = '00' ";
//      $query .= "                             AND (T3.VALUE IS NOT NULL ";
//      $query .= "                              OR  T3.COMP_CREDIT IS NOT NULL ";
//      $query .= "                              OR  T3.GET_CREDIT IS NOT NULL) ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        return $query;
    }

    //処理対象レコード（コピー）
    function getCopySql($model, $year, $sem) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);
        $code   = $model->field["TESTKINDCD"];
        $div    = (substr($code, 1, 2) == "99") ? "00" : "01";

        $query  = "";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T3.CLASSCD, ";
            $query .= " T3.SCHOOL_KIND, ";
            $query .= " T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     T3.VALUE AS VALUE, ";
        $query .= "     case when 1 <= T3.VALUE then T4.CREDITS ";
        $query .= "          else NULL end AS COMP_CREDIT, ";
        $query .= "     case when 1 < T3.VALUE then T4.CREDITS ";
        $query .= "          when 1 = T3.VALUE then 0 ";
        $query .= "          else NULL end AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND (T2.GRD_DATE IS NULL OR ";
        $query .= "                                       T2.GRD_DIV IS NULL OR ";
        $query .= "                                       T2.GRD_DIV = '4') ";
        $query .= "     INNER JOIN RECORD_SCORE_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND T3.YEAR = '{$year}' ";
        $query .= "                             AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD = '{$code}' ";
        $query .= "                             AND T3.SCORE_DIV = '{$div}' ";
        $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '{$year}' ";
        $query .= "                            AND T4.COURSECD = T1.COURSECD ";
        $query .= "                            AND T4.MAJORCD = T1.MAJORCD ";
        $query .= "                            AND T4.GRADE = T1.GRADE ";
        $query .= "                            AND T4.COURSECODE = T1.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T4.CLASSCD = T3.CLASSCD ";
            $query .= "                        AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                        AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "                            AND T4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        return $query;
    }

    //処理対象レコード（合併先科目の単位自動計算）
    function getCombinedCopySql($model, $year, $sem) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);

        $query  = "";
        $query .= " WITH T_COMBINED AS ( ";
        $query .= "     SELECT ";
        $query .= "            T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T1.COMBINED_CLASSCD, ";
            $query .= "        T1.COMBINED_SCHOOL_KIND, ";
            $query .= "        T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "            T1.COMBINED_SUBCLASSCD, ";
        $query .= "            COUNT(T2.SUBCLASSCD) AS MOTO_CNT, ";
        $query .= "            SUM(case when T2.COMP_CREDIT IS NOT NULL OR T2.GET_CREDIT IS NOT NULL then 1 else 0 end) AS CREDIT_CNT, ";
        $query .= "            SUM(T2.COMP_CREDIT) AS COMP_CREDIT, ";
        $query .= "            SUM(T2.GET_CREDIT) AS GET_CREDIT, ";
        $query .= "            SUM(case when 1 < T2.VALUE then T2.GET_CREDIT ";
        $query .= "                     when 1 = T2.VALUE then T2.COMP_CREDIT end) AS GET_CREDIT_Y ";
        $query .= "     FROM   SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "            RECORD_SCORE_DAT T2 ";
        $query .= "     WHERE  T1.YEAR = '{$year}' ";
        $query .= "       AND  T2.YEAR = '{$year}' ";
        $query .= "       AND  T2.SEMESTER = '9' ";
        $query .= "       AND  T2.TESTKINDCD = '99' ";
        $query .= "       AND  T2.TESTITEMCD = '00' ";
        $query .= "       AND  T2.SCORE_DIV = '00' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND  T2.CLASSCD = T1.ATTEND_CLASSCD ";
            $query .= "   AND  T2.SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND ";
            $query .= "   AND  T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ";
        }
        $query .= "       AND  T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ";
        $query .= "     GROUP BY ";
        $query .= "            T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T1.COMBINED_CLASSCD, ";
            $query .= "        T1.COMBINED_SCHOOL_KIND, ";
            $query .= "        T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "            T1.COMBINED_SUBCLASSCD ";
        $query .= "     ) ";
        $query .= " , T_COMBINED_FLG AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T1.COMBINED_CLASSCD, ";
            $query .= "        T1.COMBINED_SCHOOL_KIND, ";
            $query .= "        T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "            T1.COMBINED_SUBCLASSCD, ";
        $query .= "            T1.CALCULATE_CREDIT_FLG AS FLG, ";
        $query .= "            COUNT(T1.ATTEND_SUBCLASSCD) AS CNT ";
        $query .= "     FROM   SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        $query .= "     WHERE  T1.YEAR = '{$year}' ";
        $query .= "     GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T1.COMBINED_CLASSCD, ";
            $query .= "        T1.COMBINED_SCHOOL_KIND, ";
            $query .= "        T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "            T1.COMBINED_SUBCLASSCD, ";
        $query .= "            T1.CALCULATE_CREDIT_FLG ";
        $query .= "     ) ";

        //評定を設定しない科目
        if ($model->Properties["useClassDetailDat"] == '1') {
            $noValue = "(DETAIL.CLASSCD = T3.CLASSCD or '90' = T3.CLASSCD)";
        } else {
            $noValue = "(D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) or '90' = substr(T3.SUBCLASSCD,1,2))";
        }
        $noCnt1 = "0 < T5.CREDIT_CNT and T6.CNT = T5.MOTO_CNT";
        $noCnt2 = "0 < T5.CREDIT_CNT and 0 < T5.MOTO_CNT";

        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T3.CLASSCD, ";
            $query .= " T3.SCHOOL_KIND, ";
            $query .= " T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     case when ".$noValue." then NULL ";
        $query .= "          else T3.VALUE end AS VALUE, ";
        $query .= "     case when ".$noValue." and T6.FLG = '1' and ".$noCnt1." then T4.CREDITS ";
        $query .= "          when ".$noValue." and T6.FLG = '2' and ".$noCnt2." then T5.COMP_CREDIT ";
        $query .= "          when ".$noValue." then NULL ";
        $query .= "          when 1 <= T3.VALUE and T6.FLG = '1' then T4.CREDITS ";
        $query .= "          when 1 <= T3.VALUE and T6.FLG = '2' then T5.COMP_CREDIT ";
        $query .= "          else NULL end AS COMP_CREDIT, ";
        $query .= "     case when ".$noValue." and T6.FLG = '1' and ".$noCnt1." then T4.CREDITS ";
        $query .= "          when ".$noValue." and T6.FLG = '2' and ".$noCnt2." then T5.GET_CREDIT ";
        $query .= "          when ".$noValue." then NULL ";
        $query .= "          when 1 < T3.VALUE and T6.FLG = '1' then T4.CREDITS ";
        $query .= "          when 1 < T3.VALUE and T6.FLG = '2' and D015.NAMESPARE1 = 'Y' then T5.GET_CREDIT_Y ";
        $query .= "          when 1 < T3.VALUE and T6.FLG = '2' then T5.GET_CREDIT ";
        $query .= "          when 1 = T3.VALUE then 0 ";
        $query .= "          else NULL end AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND (T2.GRD_DATE IS NULL OR ";
        $query .= "                                       T2.GRD_DIV IS NULL OR ";
        $query .= "                                       T2.GRD_DIV = '4') ";
        $query .= "     INNER JOIN RECORD_SCORE_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND T3.YEAR = '{$year}' ";
        $query .= "                             AND T3.SEMESTER = '9' ";
        $query .= "                             AND T3.TESTKINDCD = '99' ";
        $query .= "                             AND T3.TESTITEMCD = '00' ";
        $query .= "                             AND T3.SCORE_DIV = '00' ";
        $query .= "     INNER JOIN T_COMBINED_FLG T6 ON T6.COMBINED_SUBCLASSCD = T3.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND T6.COMBINED_CLASSCD = T3.CLASSCD ";
            $query .= "                             AND T6.COMBINED_SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                             AND T6.COMBINED_CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN T_COMBINED T5 ON T5.SCHREGNO = T3.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T5.COMBINED_CLASSCD = T3.CLASSCD ";
            $query .= "                        AND T5.COMBINED_SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                        AND T5.COMBINED_CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "                            AND T5.COMBINED_SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     LEFT JOIN V_NAME_MST D015 ON D015.YEAR = T1.YEAR ";
        $query .= "                              AND D015.NAMECD1 = 'D015' ";
        $query .= "                              AND D015.NAMECD2 = '01' ";
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "     LEFT JOIN CLASS_DETAIL_DAT DETAIL ON DETAIL.YEAR = T1.YEAR ";
            $query .= "                                      AND DETAIL.CLASS_SEQ = '003' ";
            $query .= "                                      AND DETAIL.CLASSCD = T3.CLASSCD ";
            $query .= "                                      AND DETAIL.SCHOOL_KIND = T3.SCHOOL_KIND ";
        } else {
            $query .= "     LEFT JOIN V_NAME_MST D008 ON D008.YEAR = T1.YEAR ";
            $query .= "                              AND D008.NAMECD1 = CASE WHEN EXISTS (SELECT 'X' FROM V_NAME_MST WHERE YEAR = '{$year}' AND NAMECD1 = 'D' || T3.SCHOOL_KIND || '08') THEN 'D' || T3.SCHOOL_KIND || '08' ";
            $query .= "                                                      ELSE 'D008' ";
            $query .= "                                                 END ";
            $query .= "                              AND D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) ";
        }
        $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '{$year}' ";
        $query .= "                            AND T4.COURSECD = T1.COURSECD ";
        $query .= "                            AND T4.MAJORCD = T1.MAJORCD ";
        $query .= "                            AND T4.GRADE = T1.GRADE ";
        $query .= "                            AND T4.COURSECODE = T1.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T4.CLASSCD = T3.CLASSCD ";
            $query .= "                        AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                        AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "                            AND T4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        return $query;
    }

    //コピー処理
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = knjd214Query::getCopySql($model, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $classCd        = $row["CLASSCD"];
                $schoolKind     = $row["SCHOOL_KIND"];
                $curriculumCd   = $row["CURRICULUM_CD"];
            }
            $subCd      = $row["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            $gradValue  = $row["VALUE"];
            $compCredit = $row["COMP_CREDIT"];
            $getCredit  = $row["GET_CREDIT"];
            //評価を学年評定にコピーする。単位もコピーする。
            $query = knjd214Query::getInsertRecordDat(CTRL_YEAR, "9", "99", "00", "00", $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //コピー処理（合併先科目の単位自動計算）
    function getCombinedCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = knjd214Query::getCombinedCopySql($model, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $classCd        = $row["CLASSCD"];
                $schoolKind     = $row["SCHOOL_KIND"];
                $curriculumCd   = $row["CURRICULUM_CD"];
            }
            $subCd      = $row["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            $gradValue  = $row["VALUE"];
            $compCredit = $row["COMP_CREDIT"];
            $getCredit  = $row["GET_CREDIT"];
            //再処理のため、RECORD_SCORE_DATのレコードを一旦削除。
            $query = knjd214Query::getDeleteRecordDat(CTRL_YEAR, "9", "99", "00", "00", $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
            //評価を学年評定にコピーする。単位もコピーする。
            $query = knjd214Query::getInsertRecordDat(CTRL_YEAR, "9", "99", "00", "00", $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //RECORD_SCORE_DATのレコードを削除。
    function getDeleteRecordDat($year, $sem, $kind, $item, $div, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model) {
        $query  = "";
        $query .= " DELETE FROM RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$sem}' ";
        $query .= "     AND TESTKINDCD = '{$kind}' ";
        $query .= "     AND TESTITEMCD = '{$item}' ";
        $query .= "     AND SCORE_DIV = '{$div}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD = '{$classCd}' ";
            $query .= " AND SCHOOL_KIND = '{$schoolKind}' ";
            $query .= " AND CURRICULUM_CD = '{$curriculumCd}' ";
        }
        $query .= "     AND SUBCLASSCD = '{$subCd}' ";
        $query .= "     AND SCHREGNO = '{$schno}' ";
        return $query;
    }

    //追加
    function getInsertRecordDat($year, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model) {
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = $sem;
        $data["TESTKINDCD"][TEXT]   = $kind;
        $data["TESTITEMCD"][TEXT]   = $item;
        $data["SCORE_DIV"][TEXT]    = $div;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $classCd;
            $data["SCHOOL_KIND"][TEXT]   = $schoolKind;
            $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
        }
        $data["SUBCLASSCD"][TEXT]   = $subCd;
        $data["SCHREGNO"][TEXT]     = $schno;
        //学年評定
        $data["VALUE"][NUMBER]      = $gradValue;
        $data["COMP_CREDIT"][NUMBER]= $compCredit;
        $data["GET_CREDIT"][NUMBER] = $getCredit;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
        return $query;
    }
}
?>
