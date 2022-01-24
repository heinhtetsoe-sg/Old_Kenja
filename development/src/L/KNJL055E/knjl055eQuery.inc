<?php

require_once('for_php7.php');

class knjl055equery extends Query {

    function getNameMst($model, $namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";

        return $query;
    }

    //CSV
    function updateQueryCsv($model, &$data_arr) {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_SCORE_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
            $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
            $query .= "     AND EXAM_TYPE = '1' ";
            $query .= "     AND RECEPTNO = '{$data_arr[$i]["EXAMNO"]}' ";
            $db->query($query);

            //ENTEXAM_CKIP_CSV_DAT
            for ($subCnt = 1; $subCnt < 4; $subCnt++) {
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]        = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]       = $model->field["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]            = $model->field["TESTDIV"];
                $data["EXAM_TYPE"][TEXT]          = "1";
                $data["RECEPTNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                $data["TESTSUBCLASSCD"][TEXT]     = $subCnt;
                $data["SCORE"][NUMBER]            = $data_arr[$i]["SCORE{$subCnt}"];

                $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    function getCkipCsv($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CKIP_CSV_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";

        return $query;
    }

}
?>
