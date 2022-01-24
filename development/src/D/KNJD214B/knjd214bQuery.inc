<?php

require_once('for_php7.php');

class knjd214bQuery extends Query {

    //年組リスト
    function getAuth($model)
    {
        $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR = '" .CTRL_YEAR."'";
        $query .= "  AND SEMESTER = '".$model->field["SEMESTER"]."' ";
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
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    function getSemester($semeDiv = "")
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if (strlen($semeDiv)) {
            $query .= "    AND SEMESTER = '{$semeDiv}' ";
        } else {
            $query .= "    AND SEMESTER <> '9' ";
        }
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

    //クリア処理
    function getClearQuery($model) {
        //変数
        $year   = CTRL_YEAR;
        $sem    = $model->field["SEMESTER"];
        $kind   = "99";
        $item   = "00";
        $div    = "00";
        //ＤＢ接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //クリアするレコードを抽出
        $query  = knjd214bQuery::getClearSql($model, $year, $sem, $kind, $item, $div, "clear");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $classCd        = $row["CLASSCD"];
                $schoolKind     = $row["SCHOOL_KIND"];
                $curriculumCd   = $row["CURRICULUM_CD"];
            }
            $subCd  = $row["SUBCLASSCD"];
            $schno  = $row["SCHREGNO"];
            //RECORD_SCORE_DATのレコードをクリア。
            $query = knjd214bQuery::getDeleteRecordDat($year, $sem, $kind, $item, $div, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
        }
        $result->free();
        //ＤＢコミット
        $db->commit();
        Query::dbCheckIn($db);
    }

    //クリアレコード
    function getClearSql($model, $year, $sem, $kind, $item, $div, $sel) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);
        //SQL
        $query  = "";
        $query .= " SELECT ";
        if ($sel == "clear") {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " T3.CLASSCD, ";
                $query .= " T3.SCHOOL_KIND, ";
                $query .= " T3.CURRICULUM_CD, ";
            }
            $query .= " T3.SUBCLASSCD, ";
            $query .= " T3.SCHREGNO ";
        }
        if ($sel == "count") {
            $query .= " COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND (T2.GRD_DATE IS NULL OR ";
        $query .= "                                       T2.GRD_DIV IS NULL OR ";
        $query .= "                                       T2.GRD_DIV = '4') ";
        $query .= "     INNER JOIN RECORD_SCORE_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND T3.YEAR = '{$year}' ";
        $query .= "                             AND T3.SEMESTER = '{$sem}' ";
        $query .= "                             AND T3.TESTKINDCD = '{$kind}' ";
        $query .= "                             AND T3.TESTITEMCD = '{$item}' ";
        $query .= "                             AND T3.SCORE_DIV = '{$div}' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        return $query;
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

    //コピー処理
    function getCopyQuery($model) {
        //変数
        $year   = CTRL_YEAR;
        $sem    = $model->field["SEMESTER"];
        $kind   = "99";
        $item   = "00";
        $div    = "00";
        //ＤＢ接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //コピーするレコードを抽出
        $query  = knjd214bQuery::getCopySql($model, $year, $sem);
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
            //評価を学期末評価にコピーする。
            $query = knjd214bQuery::getInsertRecordDat($year, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
        }
        $result->free();
        //ＤＢコミット
        $db->commit();
        Query::dbCheckIn($db);
    }

    //処理対象レコード（コピー）
    function getCopySql($model, $year, $sem) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);
        $code   = $model->field["TESTKINDCD"];
        $div    = (substr($code, 1, 2) == "99") ? "00" : "01";
        //SQL
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
        $query .= "     T3.VALUE ";
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
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        return $query;
    }

    //追加
    function getInsertRecordDat($year, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $classCd, $schoolKind, $curriculumCd, $model) {
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
        //評価
        $data["VALUE"][NUMBER]      = $gradValue;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
        return $query;
    }
}
?>
