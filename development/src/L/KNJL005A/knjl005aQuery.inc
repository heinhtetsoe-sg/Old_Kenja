<?php

require_once('for_php7.php');

class knjl005aQuery extends Query
{

    //入試年度一覧取得
    public function selectYearQuery()
    {
        $query  = " WITH T_ALL (VALUE, LABEL) AS ( ";
        $query .= "      VALUES('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= "      ) ";
        $query .= " SELECT ";
        $query .= "      VALUE, ";
        $query .= "      LABEL ";
        $query .= " FROM ";
        $query .= "      T_ALL ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR AS VALUE, ";
        $query .= "     ENTEXAMYEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_MAPPING_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMst($model, $namecd1, $nameCd2 = "")
    {
        $query  = " SELECT ";
        if ($nameCd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //テスト種別取得
    public function getTestDivMst($model, $testdiv = "")
    {
        $query  = " SELECT ";
        if ($testdiv) {
            $query .= " TESTDIV_NAME ";
        } else {
            $query .= "     TESTDIV AS VALUE, ";
            $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
            $query .= "     DEFAULT_FLG ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST TEST ";
        $query .= " WHERE ";
        $query .= "         TEST.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND TEST.APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($testdiv) {
            $query .= "     AND TEST.TESTDIV = '".$testdiv."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     TEST.TESTDIV ";
        }

        return $query;
    }

    //受験型
    public function getExamtypeMst($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_TYPE AS VALUE, ";
        $query .= "     EXAM_TYPE || ':' || EXAMTYPE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAM_TYPE ";

        return $query;
    }

    //リスト取得
    public function selectQuery($model)
    {
        $query .= "   SELECT ";
        $query .= "       MAPPING.ENTEXAMYEAR, ";
        $query .= "       MAPPING.APPLICANTDIV, ";
        $query .= "       MAPPING.TESTDIV, ";
        $query .= "       MAPPING.MAPPING_NO, ";
        $query .= "       MAPPING.TESTDIV_NAME, ";
        $query .= "       MAPPING.REMARK1 || ':' || L006.NAME1 AS SHDIV, ";
        $query .= "       MAPPING.REMARK2 || ':' || Lx58.NAME1 AS COURSEDIV, ";
        $query .= "       MAPPING.REMARK3 || ':' || EXAMTYPE.EXAMTYPE_NAME AS EXAM_TYPE ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_MIRAI_MAPPING_MST MAPPING ";
        $query .= "       LEFT JOIN V_NAME_MST L006  ON L006.YEAR = MAPPING.ENTEXAMYEAR ";
        $query .= "                                 AND L006.NAMECD1 = 'L006' ";
        $query .= "                                 AND L006.NAMECD2 =  MAPPING.REMARK1";
        $query .= "       LEFT JOIN V_NAME_MST Lx58  ON Lx58.YEAR    =  MAPPING.ENTEXAMYEAR ";
        $query .= "                                 AND Lx58.NAMECD1 =  (CASE WHEN MAPPING.APPLICANTDIV = '1' THEN 'LJ58' WHEN MAPPING.APPLICANTDIV = '2' THEN 'LH58' ELSE NULL END) ";
        $query .= "                                 AND Lx58.NAMECD2 =  MAPPING.REMARK2";
        $query .= "       LEFT JOIN ENTEXAM_EXAMTYPE_MST EXAMTYPE ON EXAMTYPE.ENTEXAMYEAR  = MAPPING.ENTEXAMYEAR ";
        $query .= "                                              AND EXAMTYPE.APPLICANTDIV = MAPPING.APPLICANTDIV ";
        $query .= "                                              AND EXAMTYPE.EXAM_TYPE    = MAPPING.REMARK3 ";
        $query .= "   WHERE ";
        $query .= "         MAPPING.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND MAPPING.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND MAPPING.TESTDIV      = '".$model->testdiv."' ";
        $query .= "   ORDER BY ";
        $query .= "       MAPPING.MAPPING_NO ";

        return $query;
    }

    //１レコード取得
    public function getRow($year, $applicantdiv, $testdiv, $mappingNo)
    {
        $query  = " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     MAPPING_NO, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     REMARK1 AS SHDIV, ";
        $query .= "     REMARK2 AS COURSEDIV, ";
        $query .= "     REMARK3 AS EXAM_TYPE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_MAPPING_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$testdiv."' ";
        $query .= "     AND MAPPING_NO   = '".$mappingNo."' ";

        return $query;
    }

    //認識用文字列の重複チェック
    public function getExistsSameNameRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_MAPPING_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND TESTDIV_NAME = '{$model->field["TESTDIV_NAME"]}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //対応関係の重複チェック
    public function getExistsSameMappingRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_MAPPING_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND REMARK1      = '{$model->field["SHDIV"]}' ";
        $query .= "     AND REMARK2      = '{$model->field["COURSEDIV"]}' ";
        $query .= "     AND REMARK3      = '{$model->field["EXAM_TYPE"]}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }
  

    /************/
    /* 更新処理 */
    /************/
    //INSERT
    public function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["MAPPING_NO"][TEXT]       = $fields["MAPPING_NO"];
        $data["TESTDIV_NAME"][TEXT]     = $fields["TESTDIV_NAME"];
        $data["REMARK1"][TEXT]          = $fields["SHDIV"];
        $data["REMARK2"][TEXT]          = $fields["COURSEDIV"];
        $data["REMARK3"][TEXT]          = $fields["EXAM_TYPE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_MIRAI_MAPPING_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["TESTDIV_NAME"][TEXT]     = $fields["TESTDIV_NAME"];
        $data["REMARK1"][TEXT]          = $fields["SHDIV"];
        $data["REMARK2"][TEXT]          = $fields["COURSEDIV"];
        $data["REMARK3"][TEXT]          = $fields["EXAM_TYPE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR       = '".$fields["ENTEXAMYEAR"]."'";
        $where .= "   AND APPLICANTDIV      = '".$fields["APPLICANTDIV"]."'";
        $where .= "   AND TESTDIV           = '".$fields["TESTDIV"]."' ";
        $where .= "   AND MAPPING_NO        = '".$fields["MAPPING_NO"]."' ";

        $query = Query::updateSQL($data, "ENTEXAM_MIRAI_MAPPING_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_MIRAI_MAPPING_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."'";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND TESTDIV         = '".$fields["TESTDIV"]."'";
        $query .= "         AND MAPPING_NO      = '".$fields["MAPPING_NO"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    /********************/
    /* 前年度コピー処理 */
    /********************/
    //ある年度のデータが存在するかチェック
    public function checkData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     'X' ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_MAPPING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    //COPY
    public function getCopyQuery($thisYear, $nextYear)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_MIRAI_MAPPING_MST ";
        $query .= " ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     MAPPING_NO, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REMARK3, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     MAPPING_NO, ";
        $query .= "     TESTDIV_NAME, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REMARK3, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_MIRAI_MAPPING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$thisYear."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
