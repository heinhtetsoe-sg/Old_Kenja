<?php
class knjl3030Query extends Query {
    
    //名称マスタ取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || VALUE(NAME1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$year}' AND ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "    AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度コンボ
    function getYear($model) {
        $query  = "     SELECT DISTINCT ";
        $query .= "         ENTEXAMYEAR AS LABEL,";
        $query .= "         ENTEXAMYEAR AS VALUE";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= "     ORDER BY ";
        $query .= "         ENTEXAMYEAR ";

        return $query;
    }

    //入試区分取得
    function getTestDiv($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL,  ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->leftYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";

        return $query;
    }
    
    //試験科目
    function getTestSubclassCd($model) {
        $nameField = ($model->applicantdiv == "1") ? "NAME1" : "NAME2";

        $query  = "     SELECT ";
        $query .= "         T1.TESTSUBCLASSCD AS VALUE, ";
        $query .= "         T1.TESTSUBCLASSCD || ':' || T2.{$nameField} AS LABEL ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_TESTSUBCLASSCD_DAT T1 ";
        $query .= "         INNER JOIN V_NAME_MST T2 ";
        $query .= "             ON T2.YEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.NAMECD1 = 'L009' ";
        $query .= "             AND T2.NAMECD2 = T1.TESTSUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "         AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "         AND T1.EXAM_TYPE    = '1' ";
        $query .= "         AND T2.{$nameField} IS NOT NULL ";

        return $query;
    }

    //リスト
    function getList($model) {
        $nameField = ($model->applicantdiv == "1") ? "NAME1" : "NAME2";

        $query  = " SELECT ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     T1.TESTSUBCLASSCD || ':' || T3.{$nameField} AS TESTSUBCLASSNAME, ";
        $query .= "     T1.PERFECT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST T1 ";
        $query .= " INNER JOIN ENTEXAM_TESTSUBCLASSCD_DAT T2 ";
        $query .= "     ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "     AND T2.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "     AND T2.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ";
        $query .= " INNER JOIN V_NAME_MST T3 ";
        $query .= "     ON T3.YEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND T3.NAMECD1 = 'L009' ";
        $query .= "     AND T3.NAMECD2 = T1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.COURSECD     = '0' ";
        $query .= "     AND T1.MAJORCD      = '000' ";
        $query .= "     AND T1.EXAMCOURSECD = '0000' ";
        $query .= "     AND T1.EXAM_TYPE    = '1' ";
        $query .= "     AND T3.{$nameField} IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.TESTSUBCLASSCD ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $div = "") {
        $testSubCd = ($div == "chk") ? $model->field["TESTSUBCLASSCD"] : $model->testSubCd;

        $query  = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR           = '{$model->leftYear}' ";
        $query .= "      AND APPLICANTDIV      = '{$model->applicantdiv}' ";
        $query .= "      AND TESTDIV           = '{$model->testdiv}' ";
        $query .= "      AND TESTSUBCLASSCD    = '{$testSubCd}' ";

        return $query;
    }

/************ コピー ***************/
    //ある年度にデータが存在するのかチェック
    function getCheckPerfectMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND COURSECD     = '0' ";
        $query .= "     AND MAJORCD      = '000' ";
        $query .= "     AND EXAMCOURSECD = '0000' ";
        $query .= "     AND EXAM_TYPE    = '1' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_TESTDIV_MST)
    function &getCopyPerfectMstQuery($db, $model) {
        $nextYear = $model->leftYear + 1;

        $query  = " INSERT INTO ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' AS ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '0', ";
        $query .= "     '000', ";
        $query .= "     '0000', ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     '1', ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.RATE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST T1 ";
        $query .= "     INNER JOIN ENTEXAM_TESTSUBCLASSCD_DAT T2 ";
        $query .= "         ON T2.ENTEXAMYEAR     = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV        = T1.TESTDIV ";
        $query .= "         AND T2.EXAM_TYPE      = T1.EXAM_TYPE ";
        $query .= "         AND T2.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->leftYear}' ";

        return $query;
   }

/******** 追加更新処理  ********/
    //--- INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $model->testdiv;
        $data["COURSECD"][TEXT]         = "0";
        $data["MAJORCD"][TEXT]          = "000";
        $data["EXAMCOURSECD"][TEXT]     = "0000";
        $data["TESTSUBCLASSCD"][TEXT]   = $fields["TESTSUBCLASSCD"];
        $data["EXAM_TYPE"][TEXT]        = "1";
        $data["PERFECT"][TEXT]          = $fields["PERFECT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PERFECT_EXAMTYPE_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $model->testdiv;
        $data["COURSECD"][TEXT]         = "0";
        $data["MAJORCD"][TEXT]          = "000";
        $data["EXAMCOURSECD"][TEXT]     = "0000";
        $data["TESTSUBCLASSCD"][TEXT]   = $fields["TESTSUBCLASSCD"];
        $data["EXAM_TYPE"][TEXT]        = "1";
        $data["PERFECT"][TEXT]          = $fields["PERFECT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where .= " WHERE ";
        $where .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV       = '{$model->testdiv}' ";

        $query = Query::updateSQL($data, "ENTEXAM_PERFECT_EXAMTYPE_MST", $where);
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR           = '{$model->leftYear}' ";
        $query .= "   AND APPLICANTDIV      = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV           = '{$model->testdiv}' ";
        $query .= "   AND TESTSUBCLASSCD    = '{$fields["TESTSUBCLASSCD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
