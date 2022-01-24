<?php

require_once('for_php7.php');
class knjd657cquery extends Query
{
    //年組一覧取得
    public function getGrade()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     RTRIM(CHAR(INT(GRADE))) || '学年' AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_DOCUMENT_GDAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試グループ
    public function getGroupcd($model)
    {
        $query  = " SELECT ";
        $query .= "     GROUPCD AS VALUE, ";
        $query .= "     GROUPCD || ' ' || GROUPNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     GROUP_DIV = '{$model->group_div}' AND ";
        $query .= "     STF_AUTH_CD = '{$model->stf_auth_cd}' ";
        $query .= " ORDER BY ";
        $query .= "     GROUPCD ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            /************/
            /* まず削除 */
            /************/
            $query  = " DELETE FROM ";
            $query .= "     MOCK_DOCUMENT_GDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR             = '{$data_arr[$i]["YEAR"]}' AND ";
            $query .= "     GROUP_DIV        = '{$model->group_div}' AND ";
            $query .= "     STF_AUTH_CD      = '{$model->stf_auth_cd}' AND ";
            $query .= "     GROUPCD          = '{$data_arr[$i]["GROUPCD"]}' AND ";
            $query .= "     GRADE            = '{$data_arr[$i]["GRADE"]}' AND ";
            $query .= "     MOCK_SUBCLASS_CD = '{$data_arr[$i]["MOCK_SUBCLASS_CD"]}' ";

            $db->query($query);

            /**************/
            /* インサート */
            /**************/
            $data = array();
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["GROUP_DIV"][TEXT]        = $model->group_div;
            $data["STF_AUTH_CD"][TEXT]      = $model->stf_auth_cd;
            $data["GROUPCD"][TEXT]          = $data_arr[$i]["GROUPCD"];
            $data["GRADE"][TEXT]            = $data_arr[$i]["GRADE"];
            $data["MOCK_SUBCLASS_CD"][TEXT] = $data_arr[$i]["MOCK_SUBCLASS_CD"];
            if ($data_arr[$i]["MOCK_SUBCLASS_CD"] == '000000') {
                $data["REMARK2"][TEXT]          = $data_arr[$i]["REMARK2"];
            } else {
                $data["REMARK1"][TEXT]          = $data_arr[$i]["REMARK1"];
            }

            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][NUMBER]   = "SYSDATE()";

            $query = Query::insertSQL($data, "MOCK_DOCUMENT_GDAT");
            $db->query($query);

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

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data = array();
            $query  = " DELETE FROM ";
            $query .= "     MOCK_DOCUMENT_GDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR             = '{$data_arr[$i]["YEAR"]}' AND ";
            $query .= "     GROUP_DIV        = '{$model->group_div}' AND ";
            $query .= "     STF_AUTH_CD      = '{$model->stf_auth_cd}' AND ";
            $query .= "     GROUPCD          = '{$data_arr[$i]["GROUPCD"]}' AND ";
            $query .= "     GRADE            = '{$data_arr[$i]["GRADE"]}' AND ";
            $query .= "     MOCK_SUBCLASS_CD = '{$data_arr[$i]["MOCK_SUBCLASS_CD"]}' ";

            $db->query($query);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $year = CTRL_YEAR;

        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     GROUPCD, ";
        $query .= "     GRADE, ";
        $query .= "     MOCK_SUBCLASS_CD, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     MOCK_DOCUMENT_GDAT ";
        $query .= " WHERE ";
        $query .= "     GROUP_DIV = '{$model->group_div}' AND ";
        $query .= "     STF_AUTH_CD = '{$model->stf_auth_cd}' AND ";
        if ($model->field["GROUPCD"] != "999999") {
            $query .= "     GROUPCD       = '{$model->field["GROUPCD"]}' AND ";
        }
        if ($model->field["GRADE"] != "999999") {
            $query .= "     GRADE = '{$model->field["GRADE"]}' AND ";
        }
        $query .= "     YEAR = '{$year}' ";

        return $query;
    }

    public function checkMstData($model, $cd, $type)
    {
        if ($type == 'GROUPCD') {
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     MOCK_GROUP_MST ";
            $query .= " WHERE ";
            $query .= "     GROUP_DIV   = '{$model->group_div}' AND ";
            $query .= "     STF_AUTH_CD = '{$model->stf_auth_cd}' AND ";
            $query .= "     GROUPCD     = '{$cd}' ";
        } else {
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     MOCK_SUBCLASS_MST ";
            $query .= " WHERE ";
            $query .= "     MOCK_SUBCLASS_CD = '{$cd}' ";
        }
        return $query;
    }
}
