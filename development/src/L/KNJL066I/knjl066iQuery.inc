<?php
class knjl066iQuery extends Query
{
    /************ 左画面 ***************/

    //年度コンボ
    public function getYear($model)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      ENTEXAMYEAR AS LABEL,";
        $query .= "      ENTEXAMYEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_HALL_YDAT ";
        $query .= "  WHERE ";
        $query .= "       ENTEXAMYEAR <> '{$model->examyear}' ";
        $query .= "  UNION ";
        $query .= "      VALUES('{$model->examyear}', '{$model->examyear}')";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      LABEL,";
        $query .= "      VALUE";
        $query .= "  FROM ";
        $query .= "      TMP ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //リスト
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "   T1.APPLICANTDIV, ";
        $query .= "   T2.NAME1 AS APPLICANTDIV_NAME, ";
        $query .= "   T1.TESTDIV, ";
        $query .= "   T3.TESTDIV_ABBV AS TESTDIV_NAME, ";
        $query .= "   SUBSTR(T1.EXAMHALLCD , 1, 1) AS GROUPDIV, ";
        $query .= "   SUBSTR(T1.EXAMHALLCD , 3, 2) AS GROUPCD, ";
        $query .= "   T1.EXAMHALL_NAME AS GROUPNAME, ";
        $query .= "   T1.EXAMHALL_ABBV AS GROUPNAME_ABBV, ";
        $query .= "   T1.CAPA_CNT AS GROUPPEOPLE ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HALL_YDAT T1 ";
        $query .= "   INNER JOIN V_NAME_MST T2 ";
        $query .= "      ON T2.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "     AND T2.NAMECD1 = 'L003' ";
        $query .= "     AND T2.NAMECD2 = T1.APPLICANTDIV ";
        $query .= "   INNER JOIN ENTEXAM_TESTDIV_MST T3 ";
        $query .= "      ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "     AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND T3.TESTDIV      = T1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR       = '{$model->leftYear}' ";
        $query .= "   AND T1.EXAM_TYPE     = '1' "; //固定
        $query .= "   AND SUBSTR(T1.EXAMHALLCD , 1, 1) = '{$model->groupdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     GROUPCD ";

        return $query;
    }

    /************ 右画面 ***************/

    //名称マスタ取得
    public function getNameMst($year, $namecd1, $namecd2="")
    {
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

    //入試区分
    public function getTestDiv($model, $applicantdiv)
    {
        $query  = "     SELECT ";
        $query .= "       TESTDIV AS VALUE, ";
        $query .= "       TESTDIV || ':' || TESTDIV_ABBV AS LABEL";
        $query .= "     FROM ";
        $query .= "       ENTEXAM_TESTDIV_MST ";
        $query .= "     WHERE ";
        $query .= "       ENTEXAMYEAR = '{$model->leftYear}' ";
        $query .= "       AND APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     ORDER BY ";
        $query .= "       VALUE ";

        return $query;
    }

    //１レコード取得
    public function getRow($model, $div = "")
    {
        $applicantdiv = ($div == "chk") ? $model->field["APPLICANTDIV"] : $model->applicantdiv;
        $testdiv = ($div == "chk") ? $model->field["TESTDIV"] : $model->testdiv;
        $groupdiv = $model->groupdiv;
        $groupcd = ($div == "chk") ? $model->field["GROUPCD"] : $model->groupcd;

        $query  = " SELECT ";
        $query .= "   T1.APPLICANTDIV, ";
        $query .= "   T1.TESTDIV, ";
        $query .= "   SUBSTR(T1.EXAMHALLCD , 3, 2) AS GROUPCD, ";
        $query .= "   T1.EXAMHALL_NAME AS GROUPNAME, ";
        $query .= "   T1.EXAMHALL_ABBV AS GROUPNAME_ABBV, ";
        $query .= "   T1.CAPA_CNT AS GROUPPEOPLE ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HALL_YDAT T1 ";
        $query .= "   INNER JOIN V_NAME_MST T2 ";
        $query .= "      ON T2.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "     AND T2.NAMECD1 = 'L003' ";
        $query .= "     AND T2.NAMECD2 = T1.APPLICANTDIV ";
        $query .= "   INNER JOIN ENTEXAM_TESTDIV_MST T3 ";
        $query .= "      ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "     AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND T3.TESTDIV      = T1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR       = '{$model->leftYear}' ";
        $query .= "   AND T1.APPLICANTDIV  = '{$applicantdiv}' ";
        $query .= "   AND T1.TESTDIV       = '{$testdiv}' ";
        $query .= "   AND T1.EXAM_TYPE     = '1' "; //固定
        $query .= "   AND SUBSTR(T1.EXAMHALLCD , 1, 1) = '{$groupdiv}' ";
        $query .= "   AND SUBSTR(T1.EXAMHALLCD , 3, 2) = '{$groupcd}' ";
        $query .= " ORDER BY ";
        $query .= "     GROUPCD ";

        return $query;
    }


    /************ コピー ***************/
    //ある年度にデータが存在するのかチェック
    public function getCheckHallYdatSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_HALL_YDAT)　※次年度で入試制度、入試区分の設定がされているものだけ
    public function &getCopyHallYdatQuery($db, $model)
    {
        $nextYear = $model->leftYear + 1;

        $query  = " INSERT INTO ENTEXAM_HALL_YDAT ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' AS ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T1.EXAMHALLCD, ";
        $query .= "     T1.EXAMHALL_NAME, ";
        $query .= "     T1.EXAMHALL_ABBV, ";
        $query .= "     T1.CAPA_CNT, ";
        $query .= "     cast(null as varchar(10)), "; //受験番号範囲は次年度にコピーしない
        $query .= "     cast(null as varchar(10)), ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= "   INNER JOIN V_NAME_MST T2 ";
        $query .= "      ON T2.YEAR    = {$nextYear} ";
        $query .= "     AND T2.NAMECD1 = 'L003' ";
        $query .= "     AND T2.NAMECD2 = T1.APPLICANTDIV ";
        $query .= "   INNER JOIN ENTEXAM_TESTDIV_MST T3 ";
        $query .= "      ON T3.ENTEXAMYEAR  = {$nextYear} ";
        $query .= "     AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND T3.TESTDIV      = T1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->leftYear."' ";

        return $query;
    }


    /******** 追加更新処理  ********/
    //--- INSERT
    public function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $examhallcd = $model->groupdiv."0".$fields["GROUPCD"];

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]        = '1';
        $data["EXAMHALLCD"][TEXT]       = $examhallcd;
        $data["EXAMHALL_NAME"][TEXT]    = $fields["GROUPNAME"] ;
        $data["EXAMHALL_ABBV"][TEXT]    = $fields["GROUPNAME_ABBV"];
        $data["CAPA_CNT"][NUMBER]       = $fields["GROUPPEOPLE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_HALL_YDAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $examhallcd = $model->groupdiv."0".$fields["GROUPCD"];

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]        = "1";
        $data["EXAMHALLCD"][TEXT]       = $examhallcd;
        $data["EXAMHALL_NAME"][TEXT]    = $fields["GROUPNAME"] ;
        $data["EXAMHALL_ABBV"][TEXT]    = $fields["GROUPNAME_ABBV"];
        $data["CAPA_CNT"][NUMBER]       = $fields["GROUPPEOPLE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where .= " WHERE ";
        $where .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $where .= "   AND APPLICANTDIV  = '{$fields["APPLICANTDIV"]}' ";
        $where .= "   AND TESTDIV       = '{$fields["TESTDIV"]}' ";
        $where .= "   AND EXAM_TYPE     = '1' "; //固定
        $where .= "   AND EXAMHALLCD    = '{$examhallcd}' ";

        $query = Query::updateSQL($data, "ENTEXAM_HALL_YDAT", $where);
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $examhallcd = $model->groupdiv."0".$fields["GROUPCD"];

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$fields["APPLICANTDIV"]}' ";
        $query .= "   AND TESTDIV       = '{$fields["TESTDIV"]}' ";
        $query .= "   AND EXAM_TYPE     = '1' "; //固定
        $query .= "   AND EXAMHALLCD    = '{$examhallcd}' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
