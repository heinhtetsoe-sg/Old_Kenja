<?php

require_once('for_php7.php');
class knjz261_2aQuery extends Query
{

    //リスト取得
    public function getList()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     DUTYSHARE_MST ";
        $query .= " ORDER BY  ";
        $query .= "     DUTYSHARECD ";

        return $query;
    }

    //１レコード取得
    public function getRow($dutysharecd, $flg = "")
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     DUTYSHARE_MST ";
        $query .= " WHERE ";
        $query .= "     DUTYSHARECD = '".$dutysharecd."' ";

        if (!$flg) {
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $row = $db2->getRow($query, DB_FETCHMODE_ASSOC);
        }

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return $row;
    }

    //年度データの確認
    public function isExisting($dutysharecd)
    {
        $db    = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     DUTYSHARE_YDAT ";
        $query .= " WHERE ";
        $query .= "     DUTYSHARECD = '".$dutysharecd."' ";

        $row = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)) {
            return true;
        } else {
            return false;
        }
    }

    //教育委員会用の学校コード取得
    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //存在チェック -- DUTYSHARE_MST
    public function checkDutyShareMst($dutysharecd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM  ";
        $query .= "     DUTYSHARE_MST ";
        $query .= " WHERE  ";
        $query .= "     DUTYSHARECD  = " .$dutysharecd." ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //UPDATE
    public function &getUpdateQuery($fields, $div)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $data = array();
        if ($div == "add") {
            $data["DUTYSHARECD"][TEXT]      = $fields["DUTYSHARECD"];
        }
        $data["SHARENAME"][TEXT]        = $fields["SHARENAME"];
        $data["BASE_SHARENAME"][TEXT]   = $fields["BASE_SHARENAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($div == "add") {
            $query = Query::insertSQL($data, "DUTYSHARE_MST");
            $db->query($query);

            //学校コード取得
            $schoolcd = $db->getOne(knjz261_2aQuery::getSchoolCd());

            //学校別教育委員会テーブルへのデータ追加
            $data2 = array();
            $data2["EDBOARD_SCHOOLCD"][TEXT]    = $schoolcd;
            $data2["DUTYSHARECD"][TEXT]         = $fields["DUTYSHARECD"];
            $data2["EDBOARD_FLG"][TEXT]         = "2";
            $data2["REGISTERCD"][TEXT]          = STAFFCD;
            $data2["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data2, "EDBOARD_DUTYSHARE_DAT");
            $db2->query($query);
        } else {
            $where = " WHERE DUTYSHARECD = '".$fields["DUTYSHARECD"]."' ";
            $query = Query::updateSQL($data, "DUTYSHARE_MST", $where);
            $db->query($query);
        }

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }

    //DELETE
    public function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //削除
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     DUTYSHARE_MST ";
        $query .= " WHERE ";
        $query .= "     DUTYSHARECD = '".$fields["DUTYSHARECD"]."' ";

        $db->query($query);

        //学校コード取得
        $schoolcd = $db->getOne(knjz261_2aQuery::getSchoolCd());

        //削除（学校別教育委員会テーブル）
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     EDBOARD_DUTYSHARE_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$schoolcd."' AND ";
        $query .= "     DUTYSHARECD = '".$fields["DUTYSHARECD"]."' ";

        $db2->query($query);

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }

    //CSV取込（追加）
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $data = array();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data = array();
            $data["DUTYSHARECD"][TEXT]    = $data_arr[$i]["DUTYSHARECD"];
            $data["SHARENAME"][TEXT]      = $data_arr[$i]["SHARENAME"];
            $data["BASE_SHARENAME"][TEXT] = $data_arr[$i]["BASE_SHARENAME"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]      = "SYSDATE()";

            $query = Query::insertSQL($data, "DUTYSHARE_MST");
            $db->query($query);
            $cnt++;
        }
        $db->commit();

        return $cnt;
    }

    //CSV書出データ取得
    public function getCsvData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     DUTYSHARE_MST T1 ";
        $query .= " ORDER BY ";
        $query .= "     DUTYSHARECD ";

        return $query;
    }
}
