<?php
class knjl601hQuery extends Query
{
    
    //名称マスタ取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
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

    //年度コンボ
    public function getYear($model)
    {
        $query  = "  WITH TMP(LABEL, VALUE) AS (  ";
        $query .= "  SELECT DISTINCT ";
        $query .= "      ENTEXAMYEAR AS LABEL,";
        $query .= "      ENTEXAMYEAR AS VALUE";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_TESTDIV_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR <> '{$model->examyear}' ";
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
        $query  = "  SELECT ";
        $query .= "    * ";
        $query .= "  FROM ";
        $query .= "      ENTEXAM_TESTDIV_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "      AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "  ORDER BY ";
        $query .= "      TESTDIV ";

        return $query;
    }

    //１レコード取得
    public function getRow($model, $div = "")
    {
        $testdiv = ($div == "chk") ? $model->field["TESTDIV"] : $model->testdiv;

        $query  = "  SELECT ";
        $query  .= "     TESTDIV, ";
        $query  .= "     TESTDIV_NAME, ";
        $query  .= "     TESTDIV_ABBV, ";
        $query  .= "     TEST_DATE ";
        $query  .= " FROM ";
        $query  .= "     ENTEXAM_TESTDIV_MST ";
        $query .= "  WHERE ";
        $query .= "      ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query .= "      AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "      AND TESTDIV      = '{$testdiv}' ";

        return $query;
    }

    //１レコード取得
    public function getRowTestSubClass($model, $div = "")
    {
        $testdiv = ($div == "chk") ? $model->field["TESTDIV"] : $model->testdiv;

        $query  = "  SELECT ";
        $query  .= "      L009.SEQ AS TESTSUBCLASSCD, ";
        $query  .= "      L009.NAME1 AS TESTSUBCLASS_NAME, ";
        $query  .= "      L009.ABBV1 AS TESTSUBCLASS_ABBV, ";
        $query  .= "      CASE WHEN SUBCLASS.TESTSUBCLASSCD IS NULL THEN FALSE ELSE TRUE END AS CHECKED ";
        $query  .= "  FROM ";
        $query  .= "      ENTEXAM_SETTING_MST L009 ";
        $query  .= "      LEFT JOIN ENTEXAM_TESTSUBCLASSCD_DAT SUBCLASS ";
        $query  .= "             ON SUBCLASS.ENTEXAMYEAR    = L009.ENTEXAMYEAR ";
        $query  .= "            AND SUBCLASS.APPLICANTDIV   = L009.APPLICANTDIV ";
        $query  .= "            AND SUBCLASS.TESTDIV        = '{$testdiv}' ";
        $query  .= "            AND SUBCLASS.EXAM_TYPE      = '1' ";
        $query  .= "            AND SUBCLASS.TESTSUBCLASSCD = L009.SEQ ";
        $query  .= "  WHERE ";
        $query  .= "      L009.ENTEXAMYEAR      = '{$model->leftYear}' ";
        $query  .= "      AND L009.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query  .= "      AND L009.SETTING_CD   = 'L009' ";
        $query  .= "  ORDER BY ";
        $query  .= "      VALUE(TESTSUBCLASSCD, 0) ";

        return $query;
    }

    /************ コピー ***************/
    //ある年度にデータが存在するのかチェック
    public function getCheckTestDivMstSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_TESTDIV_MST)
    public function &getCopyTestDivMstQuery($db, $model)
    {
        $nextYear = $model->leftYear + 1;

        $query  = " INSERT INTO ENTEXAM_TESTDIV_MST(ENTEXAMYEAR,APPLICANTDIV,TESTDIV,TESTDIV_NAME,TESTDIV_ABBV,TEST_DATE,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' AS ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     TESTDIV_ABBV, ";
        $query .= "     TEST_DATE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->leftYear."' ";

        return $query;
    }

    //対象年度からのコピー(ENTEXAM_TESTDIV_MST)
    public function &getCopyTestSubClassDatQuery($db, $model)
    {
        $nextYear = $model->leftYear + 1;

        $query  = " INSERT INTO ENTEXAM_TESTSUBCLASSCD_DAT( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}' AS ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->leftYear."' ";

        return $query;
    }

    /******** 追加更新処理  ********/
    //--- INSERT
    public function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $testdiv = sprintf("%02d", $fields["TESTDIV"]);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $testdiv;
        $data["TESTDIV_NAME"][TEXT]     = $fields["TESTDIV_NAME"];
        $data["TESTDIV_ABBV"][TEXT]     = $fields["TESTDIV_ABBV"];
        $data["TEST_DATE"][TEXT]        = str_replace("/", "-", $fields["TEST_DATE"]);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_TESTDIV_MST");
        $db->query($query);
        unset($data);

        $result = $db->query(knjl601hQuery::getRowTestSubClass($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!is_null($this->field["TESTSUBCLASSCD"]) && in_array($row["TESTSUBCLASSCD"], $this->field["TESTSUBCLASSCD"])) {
                $data["ENTEXAMYEAR"][TEXT]       = $model->leftYear;
                $data["APPLICANTDIV"][TEXT]      = $model->applicantdiv;
                $data["TESTDIV"][TEXT]           = $testdiv;
                $data["EXAM_TYPE"][TEXT]         = "1";
                $data["TESTSUBCLASSCD"][TEXT]    = $row["TESTSUBCLASSCD"];
                $data["REGISTERCD"][TEXT]        = STAFFCD;
                $data["UPDATED"][FUNC]           = "sysdate()";

                $query = Query::insertSQL($data, "ENTEXAM_TESTSUBCLASSCD_DAT");
                $db->query($query);
            }
        }
        $result->free();

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $examhallcd = $model->groupdiv."0".$fields["GROUPCD"];
        $testdiv = sprintf("%02d", $fields["TESTDIV"]);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->leftYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $testdiv;
        $data["TESTDIV_NAME"][TEXT]     = $fields["TESTDIV_NAME"];
        $data["TESTDIV_ABBV"][TEXT]     = $fields["TESTDIV_ABBV"];
        $data["TEST_DATE"][TEXT]        = str_replace("/", "-", $fields["TEST_DATE"]);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where .= " WHERE ";
        $where .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $where .= "   AND TESTDIV       = '{$testdiv}' ";

        $query = Query::updateSQL($data, "ENTEXAM_TESTDIV_MST", $where);
        $db->query($query);

        $result = $db->query(knjl601hQuery::getRowTestSubClass($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!is_null($this->field["TESTSUBCLASSCD"]) && in_array($row["TESTSUBCLASSCD"], $this->field["TESTSUBCLASSCD"])) {
                if ($row["CHECKED"]) {
                    // チェックあり、データあり　何もしない
                } else {
                    // チェックあり、データなし　追加
                    unset($data);

                    $data["ENTEXAMYEAR"][TEXT]       = $model->leftYear;
                    $data["APPLICANTDIV"][TEXT]      = $model->applicantdiv;
                    $data["TESTDIV"][TEXT]           = $testdiv;
                    $data["EXAM_TYPE"][TEXT]         = "1";
                    $data["TESTSUBCLASSCD"][TEXT]    = $row["TESTSUBCLASSCD"];
                    $data["REGISTERCD"][TEXT]        = STAFFCD;
                    $data["UPDATED"][FUNC]           = "sysdate()";
    
                    $query = Query::insertSQL($data, "ENTEXAM_TESTSUBCLASSCD_DAT");
                    $db->query($query);
                }
            } else {
                if ($row["CHECKED"]) {
                    // チェックなし、データあり　削除
                    $query  = " DELETE FROM ";
                    $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT ";
                    $query .= " WHERE ";
                    $query .= "   ENTEXAMYEAR        = '{$model->leftYear}' ";
                    $query .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
                    $query .= "   AND TESTDIV        = '{$testdiv}' ";
                    $query .= "   AND TESTSUBCLASSCD = '".$row["TESTSUBCLASSCD"]."' ";
            
                    $db->query($query);
                } else {
                    // チェックなし、データなし　何もしない
                }
            }
        }
        $result->free();

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model, $fields)
    {
        $testdiv = sprintf("%02d", $fields["TESTDIV"]);
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV       = '{$testdiv}' ";

        $db->query($query);

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR       = '{$model->leftYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND TESTDIV       = '{$testdiv}' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
}
