<?php

require_once('for_php7.php');

class knjm704dQuery extends Query
{
    //年度取得
    public function getCopyYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SPECIAL_ACTIVITY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR <= '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //データ取得
    public function selectQuery()
    {
        $query  = " SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SPECIALCD ";
        $query .= "     , SPECIAL_SDATE ";
        $query .= "     , SPECIAL_EDATE ";
        $query .= "     , SPECIALACTIVITYNAME ";
        $query .= "     , SPECIALACTIVITYTIME ";
        $query .= " FROM ";
        $query .= "     SPECIAL_ACTIVITY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SPECIAL_SDATE ";

        return $query;
    }

    //１レコード取得
    public function getRow($year, $specialcd)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SPECIALCD ";
        $query .= "     , SPECIAL_SDATE ";
        $query .= "     , SPECIAL_EDATE ";
        $query .= "     , SPECIALACTIVITYNAME ";
        $query .= "     , SPECIALACTIVITYTIME ";
        $query .= " FROM ";
        $query .= "     SPECIAL_ACTIVITY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SPECIALCD = '".$specialcd."' ";

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    /***** 追加、更新、削除 *****/
    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]                 = $model->field["YEAR"];
        $data["SPECIALCD"][TEXT]            = $model->field["SPECIALCD"];
        $data["SPECIAL_SDATE"][DATE]        = str_replace("-", "/", $model->field["SPECIAL_SDATE"]);
        $data["SPECIAL_EDATE"][DATE]        = str_replace("-", "/", $model->field["SPECIAL_EDATE"]);
        $data["SPECIALACTIVITYNAME"][TEXT]  = $model->field["SPECIALACTIVITYNAME"];
        $data["SPECIALACTIVITYTIME"][TEXT]  = $model->field["SPECIALACTIVITYTIME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "SPECIAL_ACTIVITY_MST");
        $db->query($query);

        $db->commit();

        Query::dbCheckIn($db);
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]                 = $model->field["YEAR"];
        $data["SPECIALCD"][TEXT]            = $model->field["SPECIALCD"];
        $data["SPECIAL_SDATE"][DATE]        = str_replace("-", "/", $model->field["SPECIAL_SDATE"]);
        $data["SPECIAL_EDATE"][DATE]        = str_replace("-", "/", $model->field["SPECIAL_EDATE"]);
        $data["SPECIALACTIVITYNAME"][TEXT]  = $model->field["SPECIALACTIVITYNAME"];
        $data["SPECIALACTIVITYTIME"][TEXT]  = $model->field["SPECIALACTIVITYTIME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE YEAR             = '".$model->field["YEAR"]."' ";
        $where .= "   AND SPECIALCD         = '".$model->field["SPECIALCD"]."' ";

        $query = Query::updateSQL($data, "SPECIAL_ACTIVITY_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM SPECIAL_ACTIVITY_MST ";
        $query .= " WHERE YEAR             = '".$model->field["YEAR"]."' ";
        $query .= "   AND SPECIALCD         = '".$model->field["SPECIALCD"]."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /***** 前年度コピー処理 *****/
    //今年度のデータが存在するかチェック
    public function checkData($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "     CASE WHEN YEAR = '".CTRL_YEAR."'  THEN 'X' ";
        $query .= "          WHEN YEAR = '".$model->copyYear."' THEN 'Y' END  AS CNT";
        $query .= " FROM ";
        $query .= "     SPECIAL_ACTIVITY_MST ";

        $result = $db->query($query);
        $opt_year = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = $row["CNT"];
        }

        Query::dbCheckIn($db);
        return $opt_year;
    }

    //前年度のデータをコピー
    public function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO SPECIAL_ACTIVITY_MST ";
        $query .= "                ( ";
        $query .= "                 YEAR, ";
        $query .= "                 SPECIALCD, ";
        $query .= "                 SPECIAL_SDATE, ";
        $query .= "                 SPECIAL_EDATE, ";
        $query .= "                 SPECIALACTIVITYNAME, ";
        $query .= "                 SPECIALACTIVITYTIME, ";
        $query .= "                 REGISTERCD, ";
        $query .= "                 UPDATED ";
        $query .= "                ) ";
        $query .= "    (SELECT ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         SPECIALCD, ";
        $query .= "         REPLACE(SPECIAL_SDATE, '".$model->copyYear."', '".CTRL_YEAR."'), ";
        $query .= "         REPLACE(SPECIAL_EDATE, '".$model->copyYear."', '".CTRL_YEAR."'), ";
        $query .= "         SPECIALACTIVITYNAME, ";
        $query .= "         SPECIALACTIVITYTIME, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         SPECIAL_ACTIVITY_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR             = '".$model->copyYear."' ";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /***** CSV *****/
    //CSV 書き出し
    public function getCsvData()
    {
        $query  = " SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SPECIALCD ";
        $query .= "     , SPECIAL_SDATE ";
        $query .= "     , SPECIAL_EDATE ";
        $query .= "     , SPECIALACTIVITYNAME ";
        $query .= "     , SPECIALACTIVITYTIME ";
        $query .= " FROM ";
        $query .= "     SPECIAL_ACTIVITY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CSV取込
    public function insertQueryCsv(&$db, $data_arr)
    {
        $data = array();
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data["YEAR"][TEXT]                = $data_arr[$i]["YEAR"];
            $data["SPECIALCD"][TEXT]           = $data_arr[$i]["SPECIALCD"];
            $data["SPECIAL_SDATE"][DATE]       = $data_arr[$i]["SPECIAL_SDATE"];
            $data["SPECIAL_EDATE"][DATE]       = $data_arr[$i]["SPECIAL_EDATE"];
            $data["SPECIALACTIVITYNAME"][TEXT] = $data_arr[$i]["SPECIALACTIVITYNAME"];
            $data["SPECIALACTIVITYTIME"][TEXT] = $data_arr[$i]["SPECIALACTIVITYTIME"];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][FUNC]             = "sysdate()";

            $query = Query::insertSQL($data, "SPECIAL_ACTIVITY_MST");

            $db->query($query);
            $cnt++;
        }
        return $cnt;
    }
    //DB重複チェック
    public function checkDbDul($data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SPECIAL_ACTIVITY_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$data["YEAR"]."' ";
        $query .= "  AND SPECIALCD = '".$data["SPECIALCD"]."' ";

        return $query;
    }
}
