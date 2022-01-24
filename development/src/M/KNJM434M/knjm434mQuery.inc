<?php

require_once('for_php7.php');

class knjm434mQuery extends Query {

    //年組リスト
    function getHrClass($model) {
        $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR = '" .CTRL_YEAR."'";
        $query .= "  AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "ORDER BY GRADE, HR_CLASS ";
        return $query;
    }

    //クリア処理対象レコード
    function getClearSql($model, $year, $sem) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);
        $orState = false;

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
        $query .= "     T3.GRAD_VALUE, ";
        $query .= "     T3.COMP_CREDIT, ";
        $query .= "     T3.GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        //卒業予定のみ
        if ($model->field["SOTUGYO_YOTEI"] == '1') {
            $query .= "     INNER JOIN SCHREG_BASE_YEAR_DETAIL_MST D1 ";
            $query .= "          ON D1.SCHREGNO = T1.SCHREGNO ";
            $query .= "         AND D1.YEAR = T1.YEAR ";
            $query .= "         AND D1.BASE_SEQ = '001' ";
        }
        $query .= "     INNER JOIN RECORD_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "                             AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND SUBSTR(T3.SUBCLASSCD,1,2) < '92' ";
        $query .= "                             AND (";
        if ($model->field["SHORI"] == "1" && $model->field["CLEAR1"] == "1" || $model->field["SHORI"] == "2" && $model->field["COPY1"] == "1") {
            $query .= "                                 T3.GRAD_VALUE IS NOT NULL ";
            $orState = true;
        }
        if ($model->field["SHORI"] == "1" && $model->field["CLEAR2"] == "1" || $model->field["SHORI"] == "2" && $model->field["COPY2"] == "1") {
            if ($orState) { $query .= " OR "; }
            $query .= "                                 T3.COMP_CREDIT IS NOT NULL ";
            $orState = true;
        }
        if ($model->field["SHORI"] == "1" && $model->field["CLEAR3"] == "1" || $model->field["SHORI"] == "2" && $model->field["COPY3"] == "1") {
            if ($orState) { $query .= " OR "; }
            $query .= "                                 T3.GET_CREDIT IS NOT NULL";
            $orState = true;
        }
        $query .= "                                ) ";
        $query .= "     LEFT JOIN V_NAME_MST M015 ON M015.YEAR = T1.YEAR AND M015.NAMECD1 = 'M015' AND M015.NAME3 = T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND M015.NAME1 = T3.CLASSCD AND M015.ABBV1 = T3.SCHOOL_KIND AND M015.NAME2 = T3.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN V_NAME_MST M016 ON M016.YEAR = T1.YEAR AND M016.NAMECD1 = 'M016' AND M016.NAME3 = T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND M016.NAME1 = T3.CLASSCD AND M016.ABBV1 = T3.SCHOOL_KIND AND M016.NAME2 = T3.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        if ($model->field["SUBCLASS_FLG"] == '1') { // 前期科目
            $query .= "     AND (M015.NAME1 IS NOT NULL AND M016.NAME1 IS NULL) ";
        } else if ($model->field["SUBCLASS_FLG"] == '2') { // 通年、後期科目
            $query .= "     AND ((M015.NAME1 IS NULL AND M016.NAME1 IS NOT NULL) OR (M015.NAME1 IS NULL AND M016.NAME1 IS NULL)) ";
        } else if ($model->field["SUBCLASS_FLG"] == '3') { // 全て
        }
        return $query;
    }

    //クリア処理
    function getClearQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = knjm434mQuery::getClearSql($model, CTRL_YEAR, CTRL_SEMESTER);
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
            $gradValue  = "";
            $compCredit = "";
            $getCredit  = "";
            //評定、履修単位数、修得単位数をNULLで更新。
            $query = knjm434mQuery::getUpdateRecordDat($db, CTRL_YEAR, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //コピー処理対象レコード
    function getCopySql($model, $year, $sem) {
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
        $query .= "     T3.SCHREGNO, ";
        $query .= "     L1.ASSESSLEVEL AS GRAD_VALUE, ";
        $query .= "     case when 1 <= L1.ASSESSLEVEL then T4.CREDITS ";
        $query .= "          else NULL end AS COMP_CREDIT, ";
        $query .= "     case when 1 < L1.ASSESSLEVEL then T4.CREDITS ";
        $query .= "          when 1 = L1.ASSESSLEVEL then 0 ";
        $query .= "          else NULL end AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        //卒業予定のみ
        if ($model->field["SOTUGYO_YOTEI"] == '1') {
            $query .= "     INNER JOIN SCHREG_BASE_YEAR_DETAIL_MST D1 ";
            $query .= "          ON D1.SCHREGNO = T1.SCHREGNO ";
            $query .= "         AND D1.YEAR = T1.YEAR ";
            $query .= "         AND D1.BASE_SEQ = '001' ";
        }
        $query .= "     INNER JOIN RECORD_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "                             AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND SUBSTR(T3.SUBCLASSCD,1,2) < '92' ";
        $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = T1.YEAR ";
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
        $query .= "     LEFT JOIN ASSESS_MST L1 ON L1.ASSESSCD = '3' ";
        $query .= "                            AND T3.GRAD_VALUE2 BETWEEN L1.ASSESSLOW AND L1.ASSESSHIGH ";
        $query .= "     LEFT JOIN V_NAME_MST M015 ON M015.YEAR = T1.YEAR AND M015.NAMECD1 = 'M015' AND M015.NAME3 = T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND M015.NAME1 = T3.CLASSCD AND M015.ABBV1 = T3.SCHOOL_KIND AND M015.NAME2 = T3.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN V_NAME_MST M016 ON M016.YEAR = T1.YEAR AND M016.NAMECD1 = 'M016' AND M016.NAME3 = T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND M016.NAME1 = T3.CLASSCD AND M016.ABBV1 = T3.SCHOOL_KIND AND M016.NAME2 = T3.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        if ($model->field["SUBCLASS_FLG"] == '1') { // 前期科目
            $query .= "     AND (M015.NAME1 IS NOT NULL AND M016.NAME1 IS NULL) ";
        } else if ($model->field["SUBCLASS_FLG"] == '2') { // 通年、後期科目
            $query .= "     AND ((M015.NAME1 IS NOT NULL AND M016.NAME1 IS NOT NULL) OR (M015.NAME1 IS NULL AND M016.NAME1 IS NULL) OR (M015.NAME1 IS NULL AND M016.NAME1 IS NOT NULL)) ";
        } else if ($model->field["SUBCLASS_FLG"] == '3') { // 全て
        }
        return $query;
    }

    //コピー処理
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = knjm434mQuery::getCopySql($model, CTRL_YEAR, CTRL_SEMESTER);
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
            $gradValue  = $row["GRAD_VALUE"];
            $compCredit = $row["COMP_CREDIT"];
            $getCredit  = $row["GET_CREDIT"];
            //評価を学年評定にコピーする。単位もコピーする。
            $query = knjm434mQuery::getUpdateRecordDat($db, CTRL_YEAR, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //更新
    function getUpdateRecordDat($db, $year, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model) {
        $data = array();
        if ($classCd !== '90') {
            if ($model->field["SHORI"] == '1' && $model->field["CLEAR1"] == '1' || $model->field["SHORI"] == '2' && $model->field["COPY1"] == '1') {
                $data["GRAD_VALUE"][NUMBER]     = $gradValue;
            }
            if ($model->field["SHORI"] == '1' && $model->field["CLEAR2"] == '1' || $model->field["SHORI"] == '2' && $model->field["COPY2"] == '1') {
                $data["COMP_CREDIT"][NUMBER]    = $compCredit;
            }
            if ($model->field["SHORI"] == '1' && $model->field["CLEAR3"] == '1' || $model->field["SHORI"] == '2' && $model->field["COPY3"] == '1') {
                $data["GET_CREDIT"][NUMBER]     = $getCredit;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "      YEAR = '{$year}' ";
            $where .= " AND  SUBCLASSCD = '{$subCd}' ";
            $where .= " AND  SCHREGNO = '{$schno}' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= " AND  CLASSCD = '{$classCd}' ";
                $where .= " AND  SCHOOL_KIND = '{$schoolKind}' ";
                $where .= " AND  CURRICULUM_CD = '{$curriculumCd}' ";
            }
        //評価しない科目(CLASSCD='90')の場合はスクリーング、レポートで判断する
        } else {
            //クリア処理
            if ($model->field["SHORI"] === '1') {
                if ($model->field["CLEAR2"] === '1') {
                    $data["COMP_CREDIT"][NUMBER]    = "";
                }
                if ($model->field["CLEAR3"] === '1') {
                    $data["GET_CREDIT"][NUMBER]     = "";
                }
            //コピー処理
            } else {
                //スクリーング、レポートチェック
                $getRow = array();
                $getRow = $db->getRow(knjm434mQuery::getSougouCheckDat($year, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model), DB_FETCHMODE_ASSOC);
                if ($getRow["REP_PASS_FLG"] != "" && $getRow["SCHOOLING_PASS_FLG"] != "") {
                    if ($model->field["COPY2"] === '1') {
                        $data["COMP_CREDIT"][NUMBER]    = $getRow["CREDITS"];
                    }
                    if ($model->field["COPY3"] === '1') {
                        $data["GET_CREDIT"][NUMBER]     = $getRow["CREDITS"];
                    }
                }
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "      YEAR = '{$year}' ";
            $where .= " AND  SUBCLASSCD = '{$subCd}' ";
            $where .= " AND  SCHREGNO = '{$schno}' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= " AND  CLASSCD = '{$classCd}' ";
                $where .= " AND  SCHOOL_KIND = '{$schoolKind}' ";
                $where .= " AND  CURRICULUM_CD = '{$curriculumCd}' ";
            }
        }
        $query = Query::updateSQL($data, "RECORD_DAT", $where);
        return $query;
    }

    //評価しない科目(CLASSCD='90')のスクーリング、レポートチェック
    function getSougouCheckDat($year, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model) {
        //後期までの回数で処理する
        $setsemester = '2';
        
        $query  = " WITH CHAIR AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T2.CLASSCD, ";
            $query .= "         T2.SCHOOL_KIND, ";
            $query .= "         T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T1.CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         CHAIR_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '{$year}' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$setsemester."' AND ";
        $query .= "         T1.CHAIRCD  = T2.CHAIRCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND T2.CLASSCD = '{$classCd}' ";
            $query .= "     AND T2.SCHOOL_KIND = '{$schoolKind}' ";
            $query .= "     AND T2.CURRICULUM_CD = '{$curriculumCd}' ";
        }
        $query .= "         AND T2.SUBCLASSCD = '{$subCd}' ";
        $query .= "         AND T1.SCHREGNO = '{$schno}' ";
        $query .= " ) ";
        //レポート回数（規定回数）
        $query .= " , STANDARD_CNT AS ( ";
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         COUNT(STANDARD_SEQ) AS STANDARD_CNT ";
        $query .= "     FROM ";
        $query .= "         REP_STANDARDDATE_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '{$year}' AND ";
        $query .= "         REPORTDIV   <= '".$setsemester."' AND ";
        $query .= "         EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     V_NAME_MST N1 ";
        $query .= "                 WHERE ";
        $query .= "                     N1.YEAR     = T1.YEAR AND ";
        $query .= "                     N1.NAMECD1  = 'M002' AND ";
        $query .= "                     N1.NAMECD2  = T1.REPORTDIV ";
        $query .= "                 ) ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND T1.CLASSCD = '{$classCd}' ";
            $query .= "     AND T1.SCHOOL_KIND = '{$schoolKind}' ";
            $query .= "     AND T1.CURRICULUM_CD = '{$curriculumCd}' ";
        }
        $query .= "         AND T1.SUBCLASSCD = '{$subCd}' ";
        $query .= "     GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        $query .= " ) ";
        //レポート回数（再提出回数MAX）
        $query .= " , MAX_SEQ AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.STANDARD_SEQ, ";
        $query .= "         MAX(T1.REPRESENT_SEQ) AS REPRESENT_SEQ ";
        $query .= "     FROM ";
        $query .= "         REP_PRESENT_DAT T1, ";
        $query .= "         (SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             CLASSCD, ";
            $query .= "             SCHOOL_KIND, ";
            $query .= "             CURRICULUM_CD, ";
        }
        $query .= "             SUBCLASSCD, ";
        $query .= "             STANDARD_SEQ ";
        $query .= "         FROM ";
        $query .= "             REP_STANDARDDATE_DAT T1 ";
        $query .= "         WHERE ";
        $query .= "             YEAR        = '{$year}' AND ";
        $query .= "             REPORTDIV   <= '".$setsemester."' AND ";
        $query .= "             EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         V_NAME_MST N1 ";
        $query .= "                     WHERE ";
        $query .= "                         N1.YEAR     = T1.YEAR AND ";
        $query .= "                         N1.NAMECD1  = 'M002' AND ";
        $query .= "                         N1.NAMECD2  = T1.REPORTDIV ";
        $query .= "                     ) ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR          = '{$year}' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD       = T2.CLASSCD AND ";
            $query .= "         T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
            $query .= "         T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "         T1.SUBCLASSCD    = T2.SUBCLASSCD AND ";
        $query .= "         T1.STANDARD_SEQ  = T2.STANDARD_SEQ ";
        $query .= "         AND T1.SCHREGNO = '{$schno}' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.STANDARD_SEQ ";
        $query .= " ) ";
        //合格レポート数
        $query .= " , REP_PASS_CNT AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     SUM(CASE N1.NAMESPARE1 WHEN '1' THEN 1 ELSE 0 END) AS REP_PASS_CNT ";
        $query .= " FROM ";
        $query .= "     REP_PRESENT_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'M003' AND N1.NAMECD2 = T1.GRAD_VALUE, ";
        $query .= "     MAX_SEQ T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR          = '{$year}' AND ";
        $query .= "     T1.SCHREGNO      = T2.SCHREGNO AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD       = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD    = T2.SUBCLASSCD AND ";
        $query .= "     T1.STANDARD_SEQ  = T2.STANDARD_SEQ AND ";
        $query .= "     T1.REPRESENT_SEQ = T2.REPRESENT_SEQ ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND T1.CLASSCD = '{$classCd}' ";
            $query .= "     AND T1.SCHOOL_KIND = '{$schoolKind}' ";
            $query .= "     AND T1.CURRICULUM_CD = '{$curriculumCd}' ";
        }
        $query .= "         AND T1.SUBCLASSCD = '{$subCd}' ";
        $query .= "         AND T1.SCHREGNO = '{$schno}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";
        $query .= " ) ";
        //レポート判定
        $query .= " , REP_PASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         CASE WHEN T1.REP_PASS_CNT = T2.STANDARD_CNT THEN '1' ELSE NULL END AS REP_PASS_FLG ";
        $query .= "     FROM ";
        $query .= "         REP_PASS_CNT T1, ";
        $query .= "         STANDARD_CNT T2 ";
        $query .= "     WHERE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD          = T2.CLASSCD AND ";
            $query .= "         T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
            $query .= "         T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        }
        $query .= "         T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= " ) ";
        //スクーリング規定回数
        $query .= " , SCH_SEQ AS ( ";
        $query .= "     SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUM(VALUE(SCH_SEQ_MIN, 0)) AS SCH_SEQ_MIN ";
        $query .= "     FROM ";
        $query .= "         CHAIR_CORRES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD = '{$classCd}' ";
            $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
            $query .= "     AND CURRICULUM_CD = '{$curriculumCd}' ";
        }
        $query .= "         AND SUBCLASSCD = '{$subCd}' ";
        $query .= "     GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        $query .= " ) ";
        //スクーリング回数
        $query .= " , ATTEND_ALL AS ( ";
        $query .= "   SELECT  ";
        $query .= "       T1.YEAR,  ";
        $query .= "       T2.SEMESTER,  ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       T3.CLASSCD,  ";
            $query .= "       T3.SCHOOL_KIND,  ";
            $query .= "       T3.CURRICULUM_CD,  ";
        }
        $query .= "       T3.SUBCLASSCD,  ";
        $query .= "       T1.SCHREGNO,  ";
        $query .= "       T1.SCHOOLINGKINDCD,  ";
        $query .= "       T4.NAMESPARE1,  ";
        $query .= "       T1.EXECUTEDATE,  ";
        $query .= "       T1.PERIODCD,  ";
        $query .= "       T1.CREDIT_TIME, ";
        $query .= "       T5.SCH_SEQ_MIN ";
        $query .= "   FROM  ";
        $query .= "       SCH_ATTEND_DAT T1  ";
        $query .= "       INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR  ";
        $query .= "           AND T2.SEMESTER <> '9'  ";
        $query .= "           AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE  ";
        $query .= "       INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR  ";
        $query .= "           AND T3.SEMESTER = T2.SEMESTER  ";
        $query .= "           AND T3.CHAIRCD = T1.CHAIRCD  ";
        $query .= "       LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'M001'  ";
        $query .= "           AND T4.NAMECD2 = T1.SCHOOLINGKINDCD  ";
        $query .= "       LEFT JOIN CHAIR_CORRES_DAT T5 ON T5.YEAR = T1.YEAR ";
        $query .= "           AND T5.CHAIRCD = T3.CHAIRCD ";
        $query .= "   WHERE  ";
        $query .= "       T1.YEAR = '{$year}'  ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND T3.CLASSCD = '{$classCd}' ";
            $query .= "     AND T3.SCHOOL_KIND = '{$schoolKind}' ";
            $query .= "     AND T3.CURRICULUM_CD = '{$curriculumCd}' ";
        }
        $query .= "         AND T3.SUBCLASSCD = '{$subCd}' ";
        $query .= "         AND T1.SCHREGNO = '{$schno}' ";
        $query .= " ), ATTEND_KIND1 AS ( ";
        $query .= " SELECT  ";
        $query .= "         1 AS KIND, ";
        $query .= "         YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, SCHREGNO, ";
        $query .= "         COUNT(DISTINCT EXECUTEDATE) AS JISU1, ";
        $query .= "         COUNT(DISTINCT EXECUTEDATE) AS JISU2 ";
        $query .= "     FROM ATTEND_ALL ";
        $query .= "     WHERE NAMESPARE1 = '1' AND SCHOOLINGKINDCD = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, SCHREGNO ";
        $query .= " ), ATTEND_KIND2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         2 AS KIND, ";
        $query .= "         YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, SCHREGNO, ";
        $query .= "         SCH_SEQ_MIN, ";
        $query .= "         SCH_SEQ_MIN * INT(VALUE(L1.NAME1, '6')) / 10 AS LIMIT, ";
        $query .= "         SUM(CREDIT_TIME) AS JISU1, ";
        $query .= "         VALUE(INT(MIN(SCH_SEQ_MIN * INT(VALUE(L1.NAME1, '6')) / 10, SUM(CREDIT_TIME))), 0) AS JISU2 ";
        $query .= "     FROM ATTEND_ALL ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'M020' AND L1.NAMECD2 = '01'";
        $query .= "     WHERE SCHOOLINGKINDCD = '2' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, SCHREGNO, ";
        $query .= "         SCH_SEQ_MIN, ";
        $query .= "         L1.NAME1 ";
        $query .= " ), ATTEND_KIND_ AS ( ";
        $query .= "     SELECT  ";
        $query .= "         3 AS KIND, ";
        $query .= "         YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, SCHREGNO, ";
        $query .= "         SUM(CREDIT_TIME) AS JISU1, ";
        $query .= "         SUM(CREDIT_TIME) AS JISU2 ";
        $query .= "     FROM ATTEND_ALL ";
        $query .= "     WHERE NAMESPARE1 = '1' AND SCHOOLINGKINDCD <> '1' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, SCHREGNO ";
        $query .= " ), SUBCLASS_ATTEND AS ( ";
        $query .= "     SELECT KIND, YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, SCHREGNO, JISU1, JISU2 ";
        $query .= "     FROM ATTEND_KIND1 ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT KIND, YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, SCHREGNO, JISU1, JISU2 ";
        $query .= "     FROM ATTEND_KIND2 ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT KIND, YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, SCHREGNO, JISU1, JISU2 ";
        $query .= "     FROM ATTEND_KIND_ ";
        $query .= " ), SUBCLASS_ATTEND_SUM AS ( ";
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, SCHREGNO, SUM(T1.JISU2) AS SCHOOLING_TIME ";
        $query .= "     FROM SUBCLASS_ATTEND T1 ";
        $query .= "     GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, SCHREGNO ";
        $query .= " ) ";
        //スクーリング実績
        $query .= " , SCH_TIME AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     VALUE(T2.SCH_SEQ_MIN, 0) AS SCH_SEQ_MIN, ";
        $query .= "     SUM(VALUE(T3.SCHOOLING_TIME, 0)) AS SCHOOLING_TIME ";
        $query .= " FROM ";
        $query .= "     CHAIR T1 ";
        $query .= "     LEFT JOIN SCH_SEQ T2 ON T1.SUBCLASSCD    = T2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                         AND T1.CLASSCD       = T2.CLASSCD ";
            $query .= "                         AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
            $query .= "                         AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN SUBCLASS_ATTEND_SUM T3 ON T1.SCHREGNO   = T3.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                           AND T1.CLASSCD    = T3.CLASSCD ";
            $query .= "                           AND T1.SCHOOL_KIND    = T3.SCHOOL_KIND ";
            $query .= "                           AND T1.CURRICULUM_CD    = T3.CURRICULUM_CD ";
        }
        $query .= "                           AND T1.SUBCLASSCD    = T3.SUBCLASSCD ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     SCH_SEQ_MIN ";
        $query .= " ) ";
        //スクーリング判定
        $query .= " , SCH_PASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         CASE WHEN SCH_SEQ_MIN <= SCHOOLING_TIME THEN '1' ELSE NULL END AS SCHOOLING_PASS_FLG ";
        $query .= "     FROM ";
        $query .= "         SCH_TIME ";
        $query .= " ) ";

        //メイン
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T4.CREDITS, ";
        $query .= "     T2.REP_PASS_FLG, ";
        $query .= "     T3.SCHOOLING_PASS_FLG ";
        $query .= " FROM ";
        $query .= "     CHAIR T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT S1 ON S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND S1.YEAR     = T1.YEAR ";
        $query .= "                                  AND S1.SEMESTER = T1.SEMESTER ";
        $query .= "     INNER JOIN SCHREG_BASE_MST S2 ON S2.SCHREGNO = S1.SCHREGNO ";
        $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = S1.YEAR ";
        $query .= "                            AND T4.COURSECD = S1.COURSECD ";
        $query .= "                            AND T4.MAJORCD = S1.MAJORCD ";
        $query .= "                            AND T4.GRADE = S1.GRADE ";
        $query .= "                            AND T4.COURSECODE = S1.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T4.CLASSCD = T1.CLASSCD ";
            $query .= "                        AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                        AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                            AND T4.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     LEFT JOIN REP_PASS T2 ON T1.SCHREGNO      = T2.SCHREGNO ";
            $query .= "                          AND T1.CLASSCD       = T2.CLASSCD ";
            $query .= "                          AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
            $query .= "                          AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
            $query .= "                          AND T1.SUBCLASSCD    = T2.SUBCLASSCD ";
            $query .= "     LEFT JOIN SCH_PASS T3 ON T1.SCHREGNO      = T3.SCHREGNO ";
            $query .= "                          AND T1.CLASSCD       = T3.CLASSCD ";
            $query .= "                          AND T1.SCHOOL_KIND   = T3.SCHOOL_KIND ";
            $query .= "                          AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
            $query .= "                          AND T1.SUBCLASSCD    = T3.SUBCLASSCD ";
        } else {
            $query .= "     LEFT JOIN REP_PASS T2 ON T1.SCHREGNO      = T2.SCHREGNO ";
            $query .= "                          AND T1.SUBCLASSCD    = T2.SUBCLASSCD ";
            $query .= "     LEFT JOIN SCH_PASS T3 ON T1.SCHREGNO      = T3.SCHREGNO ";
            $query .= "                          AND T1.SUBCLASSCD    = T3.SUBCLASSCD ";
        }
        return $query;
    }
}
?>
