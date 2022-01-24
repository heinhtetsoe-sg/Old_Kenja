<?php

require_once('for_php7.php');
class knjz420aQuery extends Query
{
    //１レコード取得
    public function getRow($cd)
    {
        $db = Query::dbCheckOut();
        $query = "SELECT * FROM COMPANY_MST ".
                 " WHERE COMPANY_CD = '" .$cd ."'";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //一覧取得
    public function getList()
    {
        $query  = " SELECT ";
        $query .= "     COMPANY_CD, ";
        $query .= "     COMPANY_NAME, ";
        $query .= "     SHUSHOKU_ADDR, ";
        $query .= "     SHIHONKIN, ";
        $query .= "     SONINZU, ";
        $query .= "     TONINZU, ";
        $query .= "     INDUSTRY_LCD, ";
        $query .= "     INDUSTRY_MCD, ";
        $query .= "     COMPANY_SORT || ':' || T2.NAME1 AS COMPANY_SORT, ";
        $query .= "     TARGET_SEX || ':' || T3.NAME1 AS TARGET_SEX, ";
        $query .= "     ZIPCD, ";
        $query .= "     ADDR1, ";
        $query .= "     ADDR2, ";
        $query .= "     TELNO, ";
        $query .= "     REMARK ";
        $query .= " FROM COMPANY_MST ";
        $query .= " LEFT JOIN V_NAME_MST T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND T2.NAMECD1 = 'E008' AND T2.NAMECD2 = COMPANY_SORT ";
        $query .= " LEFT JOIN V_NAME_MST T3 ON T3.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND T3.NAMECD1 = 'E007' AND T3.NAMECD2 = TARGET_SEX ";
        $query .= " ORDER BY COMPANY_CD ";
        return $query;
    }

    //職種の取得
    public function getCompanycd()
    {
        $query  = " SELECT NAMECD2 AS VALUE,NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR='".CTRL_YEAR."' ";
        $query .= "       AND NAMECD1='E008' ";
        $query .= " ORDER BY NAMECD2";
        return $query;
    }

    //募集対象の取得
    public function getTarget()
    {
        $query  = " SELECT NAMECD2 AS VALUE,NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR='".CTRL_YEAR."' ";
        $query .= "       AND NAMECD1='E007' ";
        $query .= " ORDER BY NAMECD2";
        return $query;
    }

    //産業種別のコンボ(大)
    public function getIndustryLcd()
    {
        $query  = " SELECT ";
        $query .= "     INDUSTRY_LCD || ' ' || INDUSTRY_LNAME AS LABEL, ";
        $query .= "     INDUSTRY_LCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_L_MST ";

        return $query;
    }
    //産業種別のコンボ(中)
    public function getIndustryMcd($industry_lcd)
    {
        $query  = " SELECT ";
        $query .= "     INDUSTRY_MCD || ' ' || INDUSTRY_MNAME AS LABEL, ";
        $query .= "     INDUSTRY_MCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     INDUSTRY_M_MST ";
        $query .= " WHERE ";
        $query .= "     INDUSTRY_LCD = '{$industry_lcd}' ";

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

    //存在チェック -- COMPANY_MST
    public function checkCompanyMst($company_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM  ";
        $query .= "     COMPANY_MST ";
        $query .= " WHERE  ";
        $query .= "     COMPANY_CD  = " .$company_cd." ";

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

    //INSERT
    public function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        $data["COMPANY_CD"][TEXT]    = $fields["COMPANY_CD"];
        $data["COMPANY_NAME"][TEXT]  = $fields["COMPANY_NAME"];
        $data["SHUSHOKU_ADDR"][TEXT] = $fields["SHUSHOKU_ADDR"];
        $data["SHIHONKIN"][TEXT]     = $fields["SHIHONKIN"];
        $data["SONINZU"][NUMBER]     = $fields["SONINZU"];
        $data["TONINZU"][NUMBER]     = $fields["TONINZU"];
        $data["INDUSTRY_LCD"][TEXT]  = $fields["INDUSTRY_LCD"];
        $data["INDUSTRY_MCD"][TEXT]  = $fields["INDUSTRY_MCD"];
        $data["COMPANY_SORT"][TEXT]  = $fields["COMPANY_SORT"];
        $data["TARGET_SEX"][TEXT]    = $fields["TARGET_SEX"];
        $data["ZIPCD"][TEXT]         = $fields["ZIPCD"];
        $data["ADDR1"][TEXT]         = $fields["ADDR1"];
        $data["ADDR2"][TEXT]         = $fields["ADDR2"];
        $data["TELNO"][TEXT]         = $fields["TELNO"];
        $data["REMARK"][TEXT]        = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";
        $query = Query::insertSQL($data, "COMPANY_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();
        $data["COMPANY_CD"][TEXT]    = $fields["COMPANY_CD"];
        $data["COMPANY_NAME"][TEXT]  = $fields["COMPANY_NAME"];
        $data["SHUSHOKU_ADDR"][TEXT] = $fields["SHUSHOKU_ADDR"];
        $data["SHIHONKIN"][TEXT]     = $fields["SHIHONKIN"];
        $data["SONINZU"][NUMBER]     = $fields["SONINZU"];
        $data["TONINZU"][NUMBER]     = $fields["TONINZU"];
        $data["INDUSTRY_LCD"][TEXT]  = $fields["INDUSTRY_LCD"];
        $data["INDUSTRY_MCD"][TEXT]  = $fields["INDUSTRY_MCD"];
        $data["COMPANY_SORT"][TEXT]  = $fields["COMPANY_SORT"];
        $data["TARGET_SEX"][TEXT]    = $fields["TARGET_SEX"];
        $data["ZIPCD"][TEXT]         = $fields["ZIPCD"];
        $data["ADDR1"][TEXT]         = $fields["ADDR1"];
        $data["ADDR2"][TEXT]         = $fields["ADDR2"];
        $data["TELNO"][TEXT]         = $fields["TELNO"];
        $data["REMARK"][TEXT]        = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";
        $where = " WHERE COMPANY_CD  = '" .$fields["COMPANY_CD"]."'";
        $query = Query::updateSQL($data, "COMPANY_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();
        $query = "DELETE FROM COMPANY_MST WHERE COMPANY_CD  = '" .$fields["COMPANY_CD"] ."'";
        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
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
            $data["COMPANY_CD"][TEXT]    = $data_arr[$i]["COMPANY_CD"];
            $data["COMPANY_NAME"][TEXT]  = $data_arr[$i]["COMPANY_NAME"];
            $data["SHUSHOKU_ADDR"][TEXT] = $data_arr[$i]["SHUSHOKU_ADDR"];
            $data["SHIHONKIN"][TEXT]     = $data_arr[$i]["SHIHONKIN"];
            $data["SONINZU"][NUMBER]     = $data_arr[$i]["SONINZU"];
            $data["TONINZU"][NUMBER]     = $data_arr[$i]["TONINZU"];
            $data["INDUSTRY_LCD"][TEXT]  = $data_arr[$i]["INDUSTRY_LCD"];
            $data["INDUSTRY_MCD"][TEXT]  = $data_arr[$i]["INDUSTRY_MCD"];
            $data["COMPANY_SORT"][TEXT]  = $data_arr[$i]["COMPANY_SORT"];
            $data["TARGET_SEX"][TEXT]    = $data_arr[$i]["TARGET_SEX"];
            $data["ZIPCD"][TEXT]         = $data_arr[$i]["ZIPCD"];
            $data["ADDR1"][TEXT]         = $data_arr[$i]["ADDR1"];
            $data["ADDR2"][TEXT]         = $data_arr[$i]["ADDR2"];
            $data["TELNO"][TEXT]         = $data_arr[$i]["TELNO"];
            $data["REMARK"][TEXT]        = $data_arr[$i]["REMARK"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $query = Query::insertSQL($data, "COMPANY_MST");
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
        $query .= "     COMPANY_MST T1 ";
        $query .= " ORDER BY ";
        $query .= "     COMPANY_CD ";

        return $query;
    }
}
