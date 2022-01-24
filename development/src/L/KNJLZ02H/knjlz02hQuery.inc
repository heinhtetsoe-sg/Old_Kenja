<?php
class knjlz02hQuery extends Query
{

    //名称マスタ取得
    function getNameMst($year, $namecd1, $namecd2 = "")
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

    function getYear($model)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      ENTEXAMYEAR AS LABEL,";
        $query .= "      ENTEXAMYEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR <> '{$model->examYear}' ";
        $query .= "  UNION ";
        $query .= "      VALUES('{$model->examYear}', '{$model->examYear}')";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      LABEL,";
        $query .= "      VALUE";
        $query .= "  FROM ";
        $query .= "      TMP ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //入試区分取得
    function getTestDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV || ':' || TESTDIV_ABBV AS LABEL,  ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->leftYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";

        return $query;
    }

    //リスト
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     T1.TESTSUBCLASS_NAME, ";
        $query .= "     T1.PERFECT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.TESTSUBCLASSCD ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $div = "")
    {
        $testSubCd = ($div == "chk") ? $model->field["TESTSUBCLASSCD"] : $model->testSubCd;

        $query  = " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     TESTSUBCLASS_NAME, ";
        $query .= "     PERFECT, ";
        $query .= "     REMARK1 AS NAMESEX_HIDE_FLAG, ";
        $query .= "     REMARK2 AS TESTSUBCLASS_KIND ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR           = '{$model->leftYear}' ";
        $query .= "     AND APPLICANTDIV      = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV           = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE         = '1' ";
        $query .= "     AND TESTSUBCLASSCD    = '{$testSubCd}' ";

        return $query;
    }


    /************ コピー ***************/
    //ある年度にデータが存在するのかチェック
    function getCheckTestSubClassCdDatSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND EXAM_TYPE    = '1' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_TESTSUBCLASSCD_DAT)
    function &getCopyTestSubClassCdDatQuery($model)
    {
        $nextYear = $model->leftYear + 1;

        $query  = " INSERT INTO ENTEXAM_TESTSUBCLASSCD_DAT(ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,TESTSUBCLASSCD,TESTSUBCLASS_NAME,PERFECT,REMARK1,REMARK2,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' AS ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '1', ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     T1.TESTSUBCLASS_NAME, ";
        $query .= "     T1.PERFECT, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     '" . STAFFCD . "', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT T1 ";
        $query .= "     INNER JOIN V_NAME_MST T2 ";
        $query .= "        ON T2.YEAR    = {$nextYear} ";
        $query .= "       AND T2.NAMECD1 = 'L003' ";
        $query .= "       AND T2.NAMECD2 = T1.APPLICANTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->leftYear}' ";

        return $query;
    }


    /******** 追加更新処理  ********/
    //--- INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $hideFlag = ($fields["NAMESEX_HIDE_FLAG"] == "1") ? $fields["NAMESEX_HIDE_FLAG"] : '0';

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]       = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]      = $model->applicantdiv;
        $data["TESTDIV"][TEXT]           = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]         = "1";
        $data["TESTSUBCLASSCD"][TEXT]    = $fields["TESTSUBCLASSCD"];
        $data["TESTSUBCLASS_NAME"][TEXT] = $fields["TESTSUBCLASS_NAME"];
        $data["PERFECT"][TEXT]           = $fields["PERFECT"];
        $data["REMARK1"][TEXT]           = $hideFlag;
        $data["REMARK2"][TEXT]           = $fields["TESTSUBCLASS_KIND"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_TESTSUBCLASSCD_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $hideFlag = ($fields["NAMESEX_HIDE_FLAG"] == "1") ? $fields["NAMESEX_HIDE_FLAG"] : '0';

        $data = array();
        $data["TESTSUBCLASSCD"][TEXT]    = $fields["TESTSUBCLASSCD"];
        $data["TESTSUBCLASS_NAME"][TEXT] = $fields["TESTSUBCLASS_NAME"];
        $data["PERFECT"][TEXT]           = $fields["PERFECT"];
        $data["REMARK1"][TEXT]           = $hideFlag;
        $data["REMARK2"][TEXT]           = $fields["TESTSUBCLASS_KIND"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $where  = " WHERE ";
        $where .= "   ENTEXAMYEAR        = '{$model->leftYear}' ";
        $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV        = '{$model->testdiv}' ";
        $where .= "   AND EXAM_TYPE      = '1' ";
        $where .= "   AND TESTSUBCLASSCD = '{$fields["TESTSUBCLASSCD"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_TESTSUBCLASSCD_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR           = '{$model->leftYear}' ";
        $query .= "   AND APPLICANTDIV      = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV           = '{$model->testdiv}' ";
        $query .= "   AND EXAM_TYPE      = '1' ";
        $query .= "   AND TESTSUBCLASSCD    = '{$fields["TESTSUBCLASSCD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
