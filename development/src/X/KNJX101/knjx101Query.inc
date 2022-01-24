<?php

require_once('for_php7.php');

class knjx101query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX101' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度＆学期取得
    public function getYearSemesterCombo()
    {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L2.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST T1 ";
        $query .= "     INNER JOIN SCH_CHR_DAT L1 ON L1.EXECUTEDATE = T1.EXECUTEDATE AND L1.PERIODCD = T1.PERIODCD AND ";
        $query .= "                                  L1.CHAIRCD = T1.CHAIRCD AND L1.DATADIV = '2'";
        $query .= "     INNER JOIN SEMESTER_MST L2 ON L2.YEAR = T1.YEAR AND L2.SEMESTER = T1.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER ";

        return $query;
    }

    //テスト種目取得
    public function getTestKindcd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || ' ' || L3.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST T1 ";
        $query .= "     INNER JOIN SCH_CHR_DAT L1 ON L1.EXECUTEDATE = T1.EXECUTEDATE AND L1.PERIODCD = T1.PERIODCD AND ";
        $query .= "                                  L1.CHAIRCD = T1.CHAIRCD AND L1.DATADIV = '2'";
        $query .= "     INNER JOIN SEMESTER_MST L2 ON L2.YEAR = T1.YEAR AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "     INNER JOIN ".$model->testTable." L3 ON L3.YEAR = T1.YEAR AND L3.TESTKINDCD = T1.TESTKINDCD AND L3.TESTITEMCD = T1.TESTITEMCD ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW" || $model->testTable == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "                                  AND L3.SEMESTER = T1.SEMESTER ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '".$model->field["YEAR_SEME"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //実施日付取得
    public function getExecutedateCombo($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.EXECUTEDATE AS VALUE, ";
        $query .= "     T1.EXECUTEDATE AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST T1 ";
        $query .= "     INNER JOIN SCH_CHR_DAT L1 ON L1.EXECUTEDATE = T1.EXECUTEDATE AND L1.PERIODCD = T1.PERIODCD AND ";
        $query .= "                                  L1.CHAIRCD = T1.CHAIRCD AND L1.DATADIV = '2'";
        $query .= "     INNER JOIN SEMESTER_MST L2 ON L2.YEAR = T1.YEAR AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "     INNER JOIN ".$model->testTable." L3 ON L3.YEAR = T1.YEAR AND L3.TESTKINDCD = T1.TESTKINDCD AND L3.TESTITEMCD = T1.TESTITEMCD ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW" || $model->testTable == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "                                  AND L3.SEMESTER = T1.SEMESTER ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '".$model->field["YEAR_SEME"]."' ";
        if (strlen($model->field["TESTKINDCD"])) {
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '".$model->field["TESTKINDCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座取得
    public function getChaircdCombo($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || ' ' || L4.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST T1 ";
        $query .= "     INNER JOIN SCH_CHR_DAT L1 ON L1.EXECUTEDATE = T1.EXECUTEDATE AND L1.PERIODCD = T1.PERIODCD AND ";
        $query .= "                                  L1.CHAIRCD = T1.CHAIRCD AND L1.DATADIV = '2'";
        $query .= "     INNER JOIN SEMESTER_MST L2 ON L2.YEAR = T1.YEAR AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "     INNER JOIN ".$model->testTable." L3 ON L3.YEAR = T1.YEAR AND L3.TESTKINDCD = T1.TESTKINDCD AND L3.TESTITEMCD = T1.TESTITEMCD ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW" || $model->testTable == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "                                  AND L3.SEMESTER = T1.SEMESTER ";
        }
        $query .= "     INNER JOIN CHAIR_DAT L4 ON L4.YEAR = T1.YEAR AND L4.SEMESTER = T1.SEMESTER AND L4.CHAIRCD = T1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '".$model->field["YEAR_SEME"]."' ";
        if (strlen($model->field["TESTKINDCD"])) {
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '".$model->field["TESTKINDCD"]."' ";
        }
        if (strlen($model->field["EXECUTEDATE"])) {
            $query .= "     AND T1.EXECUTEDATE = '".$model->field["EXECUTEDATE"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     L3.TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST T1 ";
        $query .= "     INNER JOIN SCH_CHR_DAT L1 ON L1.EXECUTEDATE = T1.EXECUTEDATE AND L1.PERIODCD = T1.PERIODCD AND ";
        $query .= "                                  L1.CHAIRCD = T1.CHAIRCD AND L1.DATADIV = '2'";
        $query .= "     INNER JOIN SEMESTER_MST L2 ON L2.YEAR = T1.YEAR AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "     INNER JOIN ".$model->testTable." L3 ON L3.YEAR = T1.YEAR AND L3.TESTKINDCD = T1.TESTKINDCD AND L3.TESTITEMCD = T1.TESTITEMCD ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW" || $model->testTable == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "                                  AND L3.SEMESTER = T1.SEMESTER ";
        }
        $query .= "     INNER JOIN CHAIR_DAT L4 ON L4.YEAR = T1.YEAR AND L4.SEMESTER = T1.SEMESTER AND L4.CHAIRCD = T1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '".$model->field["YEAR_SEME"]."' ";
        if (strlen($model->field["TESTKINDCD"])) {
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '".$model->field["TESTKINDCD"]."' ";
        }
        if (strlen($model->field["EXECUTEDATE"])) {
            $query .= "     AND T1.EXECUTEDATE = '".$model->field["EXECUTEDATE"]."' ";
        }
        if (strlen($model->field["CHAIRCD"])) {
            $query .= "     AND T1.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD ";

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck($data, $model, $chk_no)
    {
        $flg    = true;
        $query  = "";
        //条件選択
        switch ($chk_no) {
            case "1":
                $where  = " CHAIR_DAT WHERE YEAR = Fiscalyear(date('".$data["EXECUTEDATE"]."')) ";
                $where .= " AND SEMESTER = Y2t(date('".$data["EXECUTEDATE"]."'),'N') ";
                $where .= " AND CHAIRCD = '".$data["CHAIRCD"]."' ";
                break;
            case "2":
                $where  =   $model->testTable." WHERE YEAR = Fiscalyear(date('".$data["EXECUTEDATE"]."')) ";
                $where .= " AND TESTKINDCD = '".$data["TESTKINDCD"]."' AND TESTITEMCD = '".$data["TESTITEMCD"]."' ";
                if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW" || $model->testTable == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
                    $where .= " AND SEMESTER = Y2t(date('".$data["EXECUTEDATE"]."'),'N') ";
                }
                break;
            case "3":
                $where  = " NAME_MST WHERE NAMECD1 = 'B001' AND NAMECD2 = '".$data["PERIODCD"]."' ";
                break;
            default:
                $flg = false;
                break;
        }
        if ($flg) {
            $query = " SELECT 'X' FROM " . $where;
        }

        return $query;
    }

    /*****************/
    /*  SCH_CHR_DAT  */
    /*****************/
    //データチェック(追加・更新・削除用)
    public function getExistCheck($data)
    {
        $query  = " SELECT * FROM SCH_CHR_DAT ";
        $query .= " WHERE EXECUTEDATE   = '".$data["EXECUTEDATE"]."' ";
        $query .= "     AND PERIODCD    = '".$data["PERIODCD"]."' ";
        $query .= "     AND CHAIRCD     = '".$data["CHAIRCD"]."' ";

        return $query;
    }

    //削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = " DELETE FROM SCH_CHR_DAT ";
        $query .= " WHERE EXECUTEDATE   = '".$data["EXECUTEDATE"]."' ";
        $query .= "     AND PERIODCD    = '".$data["PERIODCD"]."' ";
        $query .= "     AND CHAIRCD     = '".$data["CHAIRCD"]."' ";

        $result = $db->query($query);
    }

    //追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data, $registercd)
    {
        $data1["EXECUTEDATE"][TEXT]         = $data["EXECUTEDATE"];
        $data1["PERIODCD"][TEXT]            = $data["PERIODCD"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["EXECUTED"][TEXT]            = '0';
        $data1["DATADIV"][TEXT]             = '2';
        $data1["YEAR"][TEXT]                = $data["YEAR"];
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data1, "SCH_CHR_DAT");

        $result = $db->query($query);
    }

    //更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data, $registercd)
    {
        $data1["EXECUTED"][TEXT]            = '0';
        $data1["DATADIV"][TEXT]             = '2';
        $data1["YEAR"][TEXT]                = $data["YEAR"];
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $where  = " WHERE EXECUTEDATE   = '".$data["EXECUTEDATE"]."' ";
        $where .= "     AND PERIODCD    = '".$data["PERIODCD"]."' ";
        $where .= "     AND CHAIRCD     = '".$data["CHAIRCD"]."' ";

        $query = Query::updateSQL($data1, "SCH_CHR_DAT", $where);

        $result = $db->query($query);
    }

    /******************/
    /*  SCH_CHR_TEST  */
    /******************/
    //データチェック(追加・更新・削除用)
    public function getExistCheck2($data)
    {
        $query  = " SELECT * FROM SCH_CHR_TEST ";
        $query .= " WHERE EXECUTEDATE   = '".$data["EXECUTEDATE"]."' ";
        $query .= "     AND PERIODCD    = '".$data["PERIODCD"]."' ";
        $query .= "     AND CHAIRCD     = '".$data["CHAIRCD"]."' ";

        return $query;
    }

    //削除
    public function deleteQueryCsv2(&$db, $data)
    {
        $query  = " DELETE FROM SCH_CHR_TEST ";
        $query .= " WHERE EXECUTEDATE   = '".$data["EXECUTEDATE"]."' ";
        $query .= "     AND PERIODCD    = '".$data["PERIODCD"]."' ";
        $query .= "     AND CHAIRCD     = '".$data["CHAIRCD"]."' ";

        $result = $db->query($query);
    }

    //追加（ＣＳＶデータより読込）
    public function insertQueryCsv2(&$db, $data, $registercd)
    {
        $data1["EXECUTEDATE"][TEXT]         = $data["EXECUTEDATE"];
        $data1["PERIODCD"][TEXT]            = $data["PERIODCD"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["TESTKINDCD"][TEXT]          = $data["TESTKINDCD"];
        $data1["TESTITEMCD"][TEXT]          = $data["TESTITEMCD"];
        $data1["EXECUTED"][TEXT]            = '0';
        $data1["YEAR"][TEXT]                = $data["YEAR"];
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data1, "SCH_CHR_TEST");

        $result = $db->query($query);
    }

    //更新（ＣＳＶデータより読込）
    public function updateQueryCsv2(&$db, $data, $registercd)
    {
        $data1["TESTKINDCD"][TEXT]          = $data["TESTKINDCD"];
        $data1["TESTITEMCD"][TEXT]          = $data["TESTITEMCD"];
        $data1["YEAR"][TEXT]                = $data["YEAR"];
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $where  = " WHERE EXECUTEDATE   = '".$data["EXECUTEDATE"]."' ";
        $where .= "     AND PERIODCD    = '".$data["PERIODCD"]."' ";
        $where .= "     AND CHAIRCD     = '".$data["CHAIRCD"]."' ";

        $query = Query::updateSQL($data1, "SCH_CHR_TEST", $where);

        $result = $db->query($query);
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

    //関連テーブルの削除
    public function deleteQueryCsvOthers(&$db, $data)
    {
        //SCH_CHR_COUNTFLG
        $query  = " DELETE FROM SCH_CHR_COUNTFLG ";
        $query .= " WHERE EXECUTEDATE   = '".$data["EXECUTEDATE"]."' ";
        $query .= "     AND PERIODCD    = '".$data["PERIODCD"]."' ";
        $query .= "     AND CHAIRCD     = '".$data["CHAIRCD"]."' ";
        $result = $db->query($query);
        //SCH_CHR_HRATE_DAT
        $query  = " DELETE FROM SCH_CHR_HRATE_DAT ";
        $query .= " WHERE EXECUTEDATE   = '".$data["EXECUTEDATE"]."' ";
        $query .= "     AND PERIODCD    = '".$data["PERIODCD"]."' ";
        $query .= "     AND CHAIRCD     = '".$data["CHAIRCD"]."' ";
        $result = $db->query($query);
        //SCH_STF_DAT
        $query  = " DELETE FROM SCH_STF_DAT ";
        $query .= " WHERE EXECUTEDATE   = '".$data["EXECUTEDATE"]."' ";
        $query .= "     AND PERIODCD    = '".$data["PERIODCD"]."' ";
        $query .= "     AND CHAIRCD     = '".$data["CHAIRCD"]."' ";
        $result = $db->query($query);
        //SCH_FAC_DAT
        $query  = " DELETE FROM SCH_FAC_DAT ";
        $query .= " WHERE EXECUTEDATE   = '".$data["EXECUTEDATE"]."' ";
        $query .= "     AND PERIODCD    = '".$data["PERIODCD"]."' ";
        $query .= "     AND CHAIRCD     = '".$data["CHAIRCD"]."' ";
        $result = $db->query($query);
        //ATTEND_DAT
        $query  = " DELETE FROM ATTEND_DAT ";
        $query .= " WHERE ATTENDDATE    = '".$data["EXECUTEDATE"]."' ";
        $query .= "     AND PERIODCD    = '".$data["PERIODCD"]."' ";
        $query .= "     AND CHAIRCD     = '".$data["CHAIRCD"]."' ";
        $result = $db->query($query);
    }
}
