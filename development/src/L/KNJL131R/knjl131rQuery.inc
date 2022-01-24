<?php

require_once('for_php7.php');

class knjl131rQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //出身学校
    function getFinSchool($model) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     BASE.FS_CD AS VALUE, ";
        $query .= "     BASE.FS_CD || ':' || FSCHOOL.FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN FINSCHOOL_MST FSCHOOL ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.JUDGEMENT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ";
        $query .= "     AND BASE.ENTDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BDETAIL030.REMARK2 AS RECEPTNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     COURSE.EXAMCOURSE_NAME, ";
        $query .= "     PRINT.PRINTFLG, ";
        $query .= "     PRINT.GET_YOUROKU, ";
        $query .= "     PRINT.GET_MEDEXAM ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL030 ON BASE.ENTEXAMYEAR = BDETAIL030.ENTEXAMYEAR ";
        $query .= "          AND BASE.APPLICANTDIV = BDETAIL030.APPLICANTDIV ";
        $query .= "          AND BASE.EXAMNO = BDETAIL030.EXAMNO ";
        $query .= "          AND BDETAIL030.SEQ = '030' ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON BASE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR ";
        $query .= "          AND BASE.APPLICANTDIV = COURSE.APPLICANTDIV ";
        $query .= "          AND BDETAIL030.REMARK1 = COURSE.TESTDIV ";
        $query .= "          AND BASE.SUC_COURSECD = COURSE.COURSECD ";
        $query .= "          AND BASE.SUC_MAJORCD = COURSE.MAJORCD ";
        $query .= "          AND BASE.SUC_COURSECODE = COURSE.EXAMCOURSECD ";
        $query .= "     LEFT JOIN ENTEXAM_PRINT_DAT PRINT ON BASE.ENTEXAMYEAR = PRINT.ENTEXAMYEAR ";
        $query .= "          AND BASE.EXAMNO = PRINT.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($model->finschoolcd != '0000000') {
            $query .= "     AND BASE.FS_CD = '{$model->finschoolcd}' ";
        }
        $query .= "     AND BASE.JUDGEMENT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ";
        $query .= "     AND BASE.ENTDIV = '1' ";
        if ($model->printZumi != "1") {
            $query .= "     AND VALUE(PRINT.PRINTFLG, '0') != '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     TRANSLATE_H_ZK(BASE.NAME_KANA) ";

        return $query;
    }

    function getPrintData($year, $examno) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PRINT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND EXAMNO = '{$examno}' ";
        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            $query = knjl131rQuery::getPrintData($model->year, $examno);
            $dataCnt = $db->getOne($query);

            $data = array();
            $data["GET_YOUROKU"][TEXT]         = $model->field["YOUROKU_DATA"][$receptno];
            $data["GET_MEDEXAM"][TEXT]         = $model->field["MEDEXAM_DATA"][$receptno];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            if ($dataCnt == 0) {
                $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                $data["EXAMNO"][TEXT]       = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_PRINT_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' ";
                $where .= "   AND EXAMNO  = '{$examno}' ";
                $query = Query::updateSQL($data, "ENTEXAM_PRINT_DAT", $where);
            }
            $db->query($query);

        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //更新
    function getUpdatePrintQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        $retPrintData = "";
        $sep = "";
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            //チェックありの人を処理
            if ($model->field["PRINT_DATA"][$receptno] != "1") continue;

            $retPrintData .= $sep.$examno;
            $sep = ",";

            $query = knjl131rQuery::getPrintData($model->year, $examno);
            $dataCnt = $db->getOne($query);

            $data = array();
            $data["PRINTFLG"][TEXT]         = "1";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            if ($dataCnt == 0) {
                $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                $data["EXAMNO"][TEXT]       = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_PRINT_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' ";
                $where .= "   AND EXAMNO  = '{$examno}' ";
                $query = Query::updateSQL($data, "ENTEXAM_PRINT_DAT", $where);
            }
            $db->query($query);

        }

        $db->commit();
        Query::dbCheckIn($db);

        return $retPrintData;
    }
}
?>
