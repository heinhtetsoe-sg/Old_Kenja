<?php

require_once('for_php7.php');
class knjz281_2aQuery extends Query
{

    //リスト取得
    public function getList()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     JOB_MST ";
        $query .= " ORDER BY  ";
        $query .= "     JOBCD ";

        return $query;
    }

    //１レコード取得
    public function getRow($jobcd, $flg = "")
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     JOB_MST ";
        $query .= " WHERE ";
        $query .= "     JOBCD = '".$jobcd."' ";

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
    public function isExisting($jobcd)
    {
        $db    = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     JOB_YDAT ";
        $query .= " WHERE ";
        $query .= "     JOBCD = '".$jobcd."' ";

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

    //存在チェック -- 名称マスタ
    public function checkNameMst($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //存在チェック -- 産業分類コード
    public function checkIndustoryTypeCd($industoryTypeName, $IndustoryTypeCd)
    {
        if ($industoryTypeName == "INDUSTRY_LCD") {
            $setTableName = "INDUSTRY_L_MST";
        } elseif ($industoryTypeName == "INDUSTRY_MCD") {
            $setTableName = "INDUSTRY_M_MST";
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$setTableName} ";
        $query .= " WHERE ";
        $query .= "     {$industoryTypeName} = '{$IndustoryTypeCd}' ";

        return $query;
    }

    //存在チェック -- JOB_MST
    public function checkJobMst($jobcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM  ";
        $query .= "     JOB_MST ";
        $query .= " WHERE  ";
        $query .= "     JOBCD  = " .$jobcd." ";

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
            $data["JOBCD"][TEXT]            = $fields["JOBCD"];
        }
        $data["JOBNAME"][TEXT]          = $fields["JOBNAME"];
        $data["BASE_JOBNAME"][TEXT]     = $fields["BASE_JOBNAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($div == "add") {
            $query = Query::insertSQL($data, "JOB_MST");
            $db->query($query);

            //学校コード取得
            $schoolcd = $db->getOne(knjz281_2aQuery::getSchoolCd());

            //学校別教育委員会テーブルへのデータ追加
            $data2 = array();
            $data2["EDBOARD_SCHOOLCD"][TEXT]    = $schoolcd;
            $data2["JOBCD"][TEXT]               = $fields["JOBCD"];
            $data2["EDBOARD_FLG"][TEXT]         = "2";
            $data2["REGISTERCD"][TEXT]          = STAFFCD;
            $data2["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data2, "EDBOARD_JOB_DAT");
            $db2->query($query);
        } else {
            $where = " WHERE JOBCD = '".$fields["JOBCD"]."' ";
            $query = Query::updateSQL($data, "JOB_MST", $where);
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
        $query .= "     JOB_MST ";
        $query .= " WHERE ";
        $query .= "     JOBCD = '".$fields["JOBCD"]."' ";

        $db->query($query);

        //学校コード取得
        $schoolcd = $db->getOne(knjz281_2aQuery::getSchoolCd());

        //削除（学校別教育委員会テーブル）
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     EDBOARD_JOB_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$schoolcd."' AND ";
        $query .= "     JOBCD = '".$fields["JOBCD"]."' ";

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
            $data["JOBCD"][TEXT]        = $data_arr[$i]["JOBCD"];
            $data["JOBNAME"][TEXT]      = $data_arr[$i]["JOBNAME"];
            $data["BASE_JOBNAME"][TEXT] = $data_arr[$i]["BASE_JOBNAME"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "SYSDATE()";

            $query = Query::insertSQL($data, "JOB_MST");
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
        $query .= "     JOB_MST T1 ";
        $query .= " ORDER BY ";
        $query .= "     JOBCD ";

        return $query;
    }
}
