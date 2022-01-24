<?php

require_once('for_php7.php');

class knjm435dQuery extends Query {

    //年組リスト
    function getHrClass($model) {
        $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR = '" .CTRL_YEAR."'";
        $query .= "  AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "ORDER BY GRADE, HR_CLASS ";
        return $query;
    }

    //学期コンボ
    function getSemecmb() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目（講座）リスト
    function getChrSubCd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRNAME AS LABEL, ";
        $query .= "     T1.CHAIRCD || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN REP_STANDARDDATE_DAT R1 ON R1.YEAR = T1.YEAR ";
        $query .= "           AND R1.CLASSCD = T1.CLASSCD ";
        $query .= "           AND R1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "           AND R1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "           AND R1.SUBCLASSCD = T1.SUBCLASSCD ";
        //参照・更新可（制限付き）
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     INNER JOIN CHAIR_STF_DAT S1 ON S1.YEAR = T1.YEAR ";
            $query .= "           AND S1.SEMESTER = T1.SEMESTER ";
            $query .= "           AND S1.CHAIRCD = T1.CHAIRCD ";
            $query .= "           AND S1.STAFFCD = '" .STAFFCD ."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クリア処理対象レコード
    function getClearSql($model, $year, $sem) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);
        $orState = false;

        $chrsub_array = array();
        $chrsub_array = explode("-", $model->field["SELSUB"]);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T3.CLASSCD, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T3.CURRICULUM_CD, ";
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     T3.VALUE AS SCORE, ";
        $query .= "     T3.COMP_CREDIT, ";
        $query .= "     T3.GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN V_RECORD_SCORE_HIST_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = '9' ";
        $query .= "         AND T3.TESTKINDCD = '99' ";
        $query .= "         AND T3.TESTITEMCD = '00' ";
        $query .= "         AND T3.SCORE_DIV= '09' ";
        $query .= "         AND T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["SELSUB"] != '') {
            $query .= "         AND T3.CLASSCD      = '".$chrsub_array[1]."' ";
            $query .= "         AND T3.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
            $query .= "         AND T3.CURRICULUM_CD= '".$chrsub_array[3]."' ";
            $query .= "         AND T3.SUBCLASSCD   = '".$chrsub_array[4]."' ";
        }
        $query .= "         AND (T3.VALUE IS NOT NULL OR T3.COMP_CREDIT IS NOT NULL OR T3.GET_CREDIT IS NOT NULL) ";
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

        $query  = knjm435dQuery::getClearSql($model, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $classCd        = $row["CLASSCD"];
            $schoolKind     = $row["SCHOOL_KIND"];
            $curriculumCd   = $row["CURRICULUM_CD"];
            $subCd      = $row["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            $score = "";
            $compCredit = "";
            $getCredit  = "";

            //評定、履修単位数、修得単位数をNULLで更新。
            $query = knjm435dQuery::getUpdateRecordDat($db, CTRL_YEAR, $subCd, $schno, $score, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //仮評定フラグ
            $query = knjm435dQuery::delRecordProvFlgDat($model, $classCd, $schoolKind, $curriculumCd, $subCd, $schno);
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

        $chrsub_array = array();
        $chrsub_array = explode("-", $model->field["SELSUB"]);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T3.CLASSCD, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T3.CURRICULUM_CD, ";
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     ROUND(AVG(FLOAT(T3.VALUE)),0) AS SCORE, ";
        $query .= "     SUM(T3.COMP_CREDIT) AS COMP_CREDIT, ";
        $query .= "     SUM(T3.GET_CREDIT) AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN V_RECORD_SCORE_HIST_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "         AND T3.SEMESTER <= '".$model->field["SEMECMB"]."' ";
        $query .= "         AND T3.TESTKINDCD = '99' ";
        $query .= "         AND T3.TESTITEMCD = '00' ";
        $query .= "         AND T3.SCORE_DIV= '09' ";
        $query .= "         AND T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["SELSUB"] != '') {
            $query .= "         AND T3.CLASSCD      = '".$chrsub_array[1]."' ";
            $query .= "         AND T3.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
            $query .= "         AND T3.CURRICULUM_CD= '".$chrsub_array[3]."' ";
            $query .= "         AND T3.SUBCLASSCD   = '".$chrsub_array[4]."' ";
        }
        $query .= "         AND (T3.VALUE IS NOT NULL OR T3.COMP_CREDIT IS NOT NULL OR T3.GET_CREDIT IS NOT NULL) ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        $query .= " GROUP BY ";
        $query .= "     T3.CLASSCD, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T3.CURRICULUM_CD, ";
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SCHREGNO ";
        return $query;
    }

    //コピー処理
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = knjm435dQuery::getCopySql($model, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $classCd        = $row["CLASSCD"];
            $schoolKind     = $row["SCHOOL_KIND"];
            $curriculumCd   = $row["CURRICULUM_CD"];
            $subCd      = $row["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            $score = $row["SCORE"];
            $compCredit = $row["COMP_CREDIT"];
            $getCredit  = $row["GET_CREDIT"];

            //評価を学年評定にコピーする。単位もコピーする。
            $query = knjm435dQuery::getUpdateRecordDat($db, CTRL_YEAR, $subCd, $schno, $score, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //仮評定フラグ
            $query = knjm435dQuery::delRecordProvFlgDat($model, $classCd, $schoolKind, $curriculumCd, $subCd, $schno);
            $db->query($query);
            if ($model->field["PROV_FLG"] == "1") {
                $query = knjm435dQuery::insRecordProvFlgDat($model, $classCd, $schoolKind, $curriculumCd, $subCd, $schno, $model->field["PROV_FLG"]);
                $db->query($query);
            }
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //更新
    function getUpdateRecordDat($db, $year, $subCd, $schno, $score, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model) {
        $seq = $db->getOne("SELECT MAX(SEQ) FROM V_RECORD_SCORE_HIST_DAT "
                         . " WHERE "
                         . "      YEAR = '{$year}' "
                         . " AND  SEMESTER = '9' "
                         . " AND  TESTKINDCD = '99' "
                         . " AND  TESTITEMCD = '00' "
                         . " AND  SCORE_DIV = '09' "
                         . " AND  CLASSCD = '{$classCd}' "
                         . " AND  SCHOOL_KIND= '{$schoolKind}' "
                         . " AND  CURRICULUM_CD= '{$curriculumCd}' "
                         . " AND  SUBCLASSCD = '{$subCd}' "
                         . " AND  SCHREGNO = '{$schno}' "
                         );
        if ($seq == '') {
            $seq = '1';
            $data["YEAR"][TEXT] = $year;
            $data["SEMESTER"][TEXT] = "9";
            $data["TESTKINDCD"][TEXT] = "99";
            $data["TESTITEMCD"][TEXT] = "00";
            $data["SCORE_DIV"][TEXT] = "09";
            $data["CLASSCD"][TEXT] = $classCd;
            $data["SCHOOL_KIND"][TEXT] = $schoolKind;
            $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
            $data["SUBCLASSCD"][TEXT] = $subCd;
            $data["SCHREGNO"][TEXT] = $schno;
            $data["SEQ"][NUMBER] = $seq;
            $data["TEST_DATE"][TEXT] = CTRL_DATE;
            $query = Query::insertSQL($data, "RECORD_SCORE_HIST_DAT");
            $db->query($query);
        }

        $data = array();
        $data["VALUE"][NUMBER]          = $score;
        $data["COMP_CREDIT"][NUMBER]    = $compCredit;
        $data["GET_CREDIT"][NUMBER]     = $getCredit;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "      YEAR = '{$year}' ";
        $where .= " AND  SEMESTER = '9' ";
        $where .= " AND  TESTKINDCD = '99' ";
        $where .= " AND  TESTITEMCD = '00' ";
        $where .= " AND  SCORE_DIV = '09' ";
        $where .= " AND  CLASSCD = '{$classCd}' ";
        $where .= " AND  SCHOOL_KIND = '{$schoolKind}' ";
        $where .= " AND  CURRICULUM_CD = '{$curriculumCd}' ";
        $where .= " AND  SUBCLASSCD = '{$subCd}' ";
        $where .= " AND  SCHREGNO = '{$schno}' ";
        $where .= " AND  SEQ = ".$seq." ";

        $query = Query::updateSQL($data, "RECORD_SCORE_HIST_DAT", $where);
        return $query;
    }

    //仮評定フラグ削除
    function delRecordProvFlgDat($model, $classCd, $schoolKind, $curriculumCd, $subCd, $schno) {
        $year = CTRL_YEAR;

        $query  = " DELETE FROM RECORD_PROV_FLG_DAT ";
        $query .= " WHERE YEAR = '{$year}' ";
        $query .= "     AND CLASSCD      = '".$classCd."' ";
        $query .= "     AND SCHOOL_KIND  = '".$schoolKind."' ";
        $query .= "     AND CURRICULUM_CD= '".$curriculumCd."' ";
        $query .= "     AND SUBCLASSCD   = '".$subCd."' ";
        $query .= "     AND SCHREGNO = '{$schno}'";

        return $query;
    }

    //仮評定フラグ追加
    function insRecordProvFlgDat($model, $classCd, $schoolKind, $curriculumCd, $subCd, $schno, $setProvFlg) {
        $year = CTRL_YEAR;

        $data = array();
        $data["YEAR"][TEXT]             = $year;
        $data["CLASSCD"][TEXT]          = $classCd;
        $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
        $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
        $data["SUBCLASSCD"][TEXT]       = $subCd;
        $data["SCHREGNO"][TEXT]         = $schno;
        $data["PROV_FLG"][TEXT]         = $setProvFlg;
        $data["REGISTERCD"][TEXT]       = STAFFCD ;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "RECORD_PROV_FLG_DAT");
        return $query;
    }
}
?>
