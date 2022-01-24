<?php

require_once('for_php7.php');

class knjl432mQuery extends Query
{

    //校種コンボ
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || VALUE(ABBV1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "       YEAR    = '{$model->objYear}' ";
        $query .= "   AND NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験ID取得
    public function getExamId($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY || ':' || EXAM_NAME AS LABEL, ";
        $query .= "     EXAM_SCHOOL_KIND || '-' || APPLICANT_DIV || '-' || COURSE_DIV || '-' || FREQUENCY AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST ";
        $query .= " WHERE ";
        $query .= "       YEAR        = '{$model->objYear}' ";
        $query .= "   AND EXAM_SCHOOL_KIND = '{$model->examSchoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得
    public function getSettingMst($model, $settingCd)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     SEQ AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "       ENTEXAMYEAR = '{$model->objYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->examSchoolKind}' ";
        $query .= "   AND SETTING_CD = '{$settingCd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     RECEPT.EXAMNO, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     FLOOR(RANK5S.EXAM_SCORE) AS EXAM_SCORE, ";
        $query .= "     RECEPT.JUDGEMENT AS JUDGEMENT_CD, ";
        $query .= "     CASE WHEN RECEPT.JUDGEMENT = '1' THEN '合格' ";
        $query .= "          WHEN RECEPT.JUDGEMENT = '2' THEN '合格' ";
        $query .= "          WHEN RECEPT.JUDGEMENT = '9' THEN '不合格' ELSE '' ";
        $query .= "     END AS JUDGEMENT, ";
        $query .= "     CASE WHEN RECEPT.JUDGEMENT = '2' THEN '1' ELSE '' END AS ADVANCE_CHECK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "            ON BASE.YEAR   = RECEPT.YEAR ";
        $query .= "           AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_STD_RANK_DAT RANK5S ";
        $query .= "            ON RANK5S.YEAR          = RECEPT.YEAR ";
        $query .= "           AND RANK5S.RECEPTNO      = RECEPT.RECEPTNO ";
        $query .= "           AND RANK5S.EXAM_SUBCLASS = '99' ";
        $query .= "           AND RANK5S.SUMMARY_DIV   = 'TS' "; //TS:総合計
        $query .= " WHERE ";
        $query .= "       RECEPT.YEAR               = '{$model->objYear}' ";
        $query .= "   AND RECEPT.EXAM_SCHOOL_KIND   = '{$model->wkExamId[0]}' ";
        $query .= "   AND RECEPT.APPLICANT_DIV      = '{$model->wkExamId[1]}' ";
        $query .= "   AND RECEPT.COURSE_DIV         = '{$model->wkExamId[2]}' ";
        $query .= "   AND RECEPT.FREQUENCY          = '{$model->wkExamId[3]}' ";
        if ($model->lowerScore != "") {
            $query .= "   AND FLOOR(RANK5S.EXAM_SCORE) >= {$model->lowerScore} ";
        }
        if ($model->upperScore != "") {
            $query .= "   AND FLOOR(RANK5S.EXAM_SCORE) <= {$model->upperScore} ";
        }
        $query .= " ORDER BY ";
        if ($model->sort == "2") {
            $query .= "     RECEPT.RECEPTNO, ";
            $query .= "     RECEPT.EXAMNO, ";
        }
        $query .= "     FLOOR(RANK5S.EXAM_SCORE) DESC ";
        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->receptnoArray as $key => $receptno) {
            $examno = $model->examnoArray[$key];
            $advanceCheck = $model->advanceCheckArray[$key];
            $judgement = ($model->judgement == "1" && $advanceCheck == "1") ? "2" : $model->judgement;

            //対象受験生
            $where  = " WHERE ";
            $where .= "       YEAR             = '{$model->objYear}' ";
            $where .= "   AND EXAM_SCHOOL_KIND = '{$model->wkExamId[0]}' ";
            $where .= "   AND APPLICANT_DIV    = '{$model->wkExamId[1]}' ";
            $where .= "   AND COURSE_DIV       = '{$model->wkExamId[2]}' ";
            $where .= "   AND FREQUENCY        = '{$model->wkExamId[3]}' ";
            $where .= "   AND EXAMNO           = '{$examno}' ";
            $where .= "   AND RECEPTNO         = '{$receptno}' ";

            //受験設定データの取得
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= "  FROM ";
            $query .= "     ENTEXAM_STD_RECEPT_DAT ";
            $query .= $where;
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //更新 -- ENTEXAM_STD_RECEPT_DAT
            $data = array();
            $data["JUDGEMENT"][TEXT]        = $judgement;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if (is_array($Row)) {
                $data["SUC_COURSECD"][TEXT]     = $Row["SUC_COURSECD"];
                $data["SUC_MAJORCD"][TEXT]      = $Row["SUC_MAJORCD"];
                $data["SUC_COURSECODE"][TEXT]   = $Row["SUC_COURSECODE"];
                $query = Query::updateSQL($data, "ENTEXAM_STD_RECEPT_DAT", $where);
            } else {
                $data["YEAR"][TEXT]             = $model->objYear;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["EXAM_SCHOOL_KIND"][TEXT] = $model->wkExamId[0];
                $data["APPLICANT_DIV"][TEXT]    = $model->wkExamId[1];
                $data["COURSE_DIV"][TEXT]       = $model->wkExamId[2];
                $data["FREQUENCY"][TEXT]        = $model->wkExamId[3];
                $data["RECEPTNO"][TEXT]         = $receptno;
                $query = Query::insertSQL($data, "ENTEXAM_STD_RECEPT_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            knjl432mQuery::insertQuery($model, $db, $data_arr, $i);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);
        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            knjl432mQuery::deleteQuery($model, $db, $data_arr, $i);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //データ削除
    public function deleteQuery($model, $db, $data_arr, $i)
    {

        //削除 -- ENTEXAM_STD_RECEPT_DAT
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "       YEAR             = '{$data_arr[$i]["YEAR"]}' ";
        $query .= "   AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '{$data_arr[$i]["EXAM_ID"]}' ";
        $query .= "   AND EXAMNO           = '{$data_arr[$i]["EXAMNO"]}' ";
        $query .= "   AND RECEPTNO         = '{$data_arr[$i]["RECEPTNO"]}' ";
        $db->query($query);
    }

    //データ登録
    public function insertQuery($model, $db, $data_arr, $i)
    {

        //対象受験生
        $where  = " WHERE ";
        $where .= "       YEAR             = '{$data_arr[$i]["YEAR"]}' ";
        $where .= "   AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '{$data_arr[$i]["EXAM_ID"]}' ";
        $where .= "   AND EXAMNO           = '{$data_arr[$i]["EXAMNO"]}' ";
        $where .= "   AND RECEPTNO         = '{$data_arr[$i]["RECEPTNO"]}' ";

        //受験設定データの取得
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT ";
        $query .= $where;
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //更新 -- ENTEXAM_STD_RECEPT_DAT
        $wkExamId = explode("-", $data_arr[$i]["EXAM_ID"]);
        $data = array();
        $data["JUDGEMENT"][TEXT]        = $data_arr[$i]["JUDGEMENT"];
        if (strlen($data_arr[$i]["SUC_COURSECODE"]) > 0) {
            $data["SUC_COURSECD"][TEXT]     = substr($data_arr[$i]["SUC_COURSECODE"], 0, 1);
            $data["SUC_MAJORCD"][TEXT]      = substr($data_arr[$i]["SUC_COURSECODE"], 1, 3);
            $data["SUC_COURSECODE"][TEXT]   = substr($data_arr[$i]["SUC_COURSECODE"], 4);
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";
        if (is_array($Row)) {
            $query = Query::updateSQL($data, "ENTEXAM_STD_RECEPT_DAT", $where);
        } else {
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["EXAM_SCHOOL_KIND"][TEXT] = substr($data_arr[$i]["EXAM_ID"], 0, 1);
            $data["APPLICANT_DIV"][TEXT]    = substr($data_arr[$i]["EXAM_ID"], 1, 2);
            $data["COURSE_DIV"][TEXT]       = substr($data_arr[$i]["EXAM_ID"], 3, 4);
            $data["FREQUENCY"][TEXT]        = substr($data_arr[$i]["EXAM_ID"], 7, 2);
            $data["RECEPTNO"][TEXT]         = $data_arr[$i]["RECEPTNO"];
            $query = Query::insertSQL($data, "ENTEXAM_STD_RECEPT_DAT");
        }
        $db->query($query);
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //一覧表示
    public function selectCsvQuery($model)
    {
        $wkExamId = ($model->examId != "") ? explode("-", $model->examId) : array();
        
        $query  = " SELECT ";
        $query .= "     RECEPT.YEAR, ";
        $query .= "     STD.EXAM_SCHOOL_KIND || STD.APPLICANT_DIV || STD.COURSE_DIV ||  STD.FREQUENCY AS EXAM_ID, ";
        $query .= "     STD.EXAM_NAME, ";
        $query .= "     RECEPT.EXAMNO, ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     RECEPT.JUDGEMENT, ";
        $query .= "     RECEPT.SUC_COURSECD || RECEPT.SUC_MAJORCD || RECEPT.SUC_COURSECODE AS SUC_COURSECODE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "            ON BASE.YEAR   = RECEPT.YEAR ";
        $query .= "           AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_STD_MST STD ";
        $query .= "            ON STD.YEAR               = RECEPT.YEAR ";
        $query .= "           AND STD.EXAM_SCHOOL_KIND   = RECEPT.EXAM_SCHOOL_KIND ";
        $query .= "           AND STD.APPLICANT_DIV      = RECEPT.APPLICANT_DIV ";
        $query .= "           AND STD.COURSE_DIV         = RECEPT.COURSE_DIV ";
        $query .= "           AND STD.FREQUENCY          = RECEPT.FREQUENCY ";
        $query .= " WHERE ";
        $query .= "       RECEPT.YEAR               = '{$model->objYear}' ";
        $query .= "   AND RECEPT.EXAM_SCHOOL_KIND   = '{$wkExamId[0]}' ";
        $query .= "   AND RECEPT.APPLICANT_DIV      = '{$wkExamId[1]}' ";
        $query .= "   AND RECEPT.COURSE_DIV         = '{$wkExamId[2]}' ";
        $query .= "   AND RECEPT.FREQUENCY          = '{$wkExamId[3]}' ";
        $query .= " ORDER BY ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     RECEPT.EXAMNO ";
        
        return $query;
    }
}
