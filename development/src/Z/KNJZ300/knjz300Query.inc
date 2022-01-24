<?php

require_once('for_php7.php');
class knjz300Query extends Query
{

    //次回パスワード強制変更フィールドの有無
    public function getChgPwdField()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME = 'USER_MST' ";
        $query .= "     AND NAME = 'CHG_PWD_FLG' ";

        return $query;
    }

    //user_mstテーブル、userid重複チェック
    public function chkID($scd, $uid, $model)
    {
        $rtn_val = 0;

        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM USER_MST ";
        $query .= " WHERE USERID  = '" .$uid ."'";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }


        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if ($row) {
            $query  = "SELECT * FROM USER_MST ";
            $query .= " WHERE USERID  = '".$uid."' ";
            $query .= " AND STAFFCD = '".$scd."' ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if (!$row) {
                $rtn_val = 1;
            }
        }
        Query::dbCheckIn($db);
        return $rtn_val;
    }

    //コンボボックス内年度取得
    public function selectYearQuery()
    {
        $query = " SELECT DISTINCT YEAR FROM V_STAFF_MST ORDER BY YEAR DESC";
        return $query;
    }

    //事前処理チェック(職員マスタ)
    public function getCheckListQuery($model)
    {
        $year_val = $model->year;
        $query  = "SELECT ";
        $query .= " * ";
        $query .= " FROM V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '" .$year_val."' ";

        return $query;
    }

    //リスト内情報取得
    public function readQuery($model)
    {
        $year_val = $model->year;

        if ($model->sort["SRT_U"] == 1) {
            $OrderBy1 = "ORDER BY VS.STAFFCD ASC";
            $OrderBy2 = "";
        } elseif ($model->sort["SRT_U"] == -1) {
            $OrderBy1 = "ORDER BY VS.STAFFCD DESC";
            $OrderBy2 = "";
        } elseif ($model->sort["SRT_S"] == 1) {
            $OrderBy2 = "ORDER BY STAFFNAME_KANA ASC";
            $OrderBy1 = "";
        } elseif ($model->sort["SRT_S"] == -1) {
            $OrderBy2 = "ORDER BY STAFFNAME_KANA DESC";
            $OrderBy1 = "";
        }

        $query  = " SELECT ";
        $query .= "     VS.STAFFCD AS STAFFCD, VS.STAFFNAME AS STAFFNAME, ";
        $query .= "     VS.STAFFNAME_KANA AS STAFFKANA, US.USERID AS USERID, VS.UPDATED , US.INVALID_FLG, US.PWDTERMCHK_FLG, ";
        if ($model->isChgPwdUse) {
            $query .= "     US.CHG_PWD_FLG, ";
        }
        $query .= "     SD.FIELD1 AS ENT_ADMIN ";
        $query .= " FROM V_STAFF_MST VS LEFT JOIN USER_MST US ON VS.STAFFCD = US.STAFFCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND US.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND US.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN STAFF_DETAIL_MST SD ";
        $query .= "          ON VS.YEAR         = SD.YEAR ";
        $query .= "         AND VS.STAFFCD      = SD.STAFFCD ";
        $query .= "         AND SD.STAFF_SEQ    = '009' ";
        $query .= " WHERE ";
        $query .= " VS.YEAR = '" .$year_val."' ";
        $query .= $OrderBy1.$OrderBy2;
        return $query;
    }

    //１レコード取得
    public function getRow($cd, $on_year, $model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT ";
        $query .= " VS.STAFFCD, VS.STAFFNAME, VS.STAFFNAME_KANA AS STAFFKANA, ";
        $query .= " US.USERID, US.PASSWD, US.INVALID_FLG, US.UPDATED, US.INVALID_FLG, US.PWDTERMCHK_FLG ";
        if ($model->isChgPwdUse) {
            $query .= " ,US.CHG_PWD_FLG ";
        }
        $query .= " FROM V_STAFF_MST VS LEFT JOIN USER_MST US ON VS.STAFFCD = US.STAFFCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND US.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND US.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE VS.STAFFCD = '" .$cd ."' AND VS.YEAR = '" .$on_year ."'";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //user_mstテーブルにデータ存在するか？
    public function getPass($cd, $model)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM USER_MST WHERE STAFFCD = '" .$cd ."'";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    public function &getInsertQuery($fields, $model)
    {
        $flug="";
        if (!stristr($fields["PASSWD"], "*")) {
            $fields["PASSWD"] = md5($fields["PASSWD"]);
            $flug = true;
        }

        $db = Query::dbCheckOut();

        $data = array();
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]          = SCHOOLKIND;
        }
        $data["STAFFCD"][TEXT]              = $fields["USERSCD"];
        $data["USERID"][TEXT]               = $fields["USERID"];

        if ($flug) {
            $data["PASSWD"][TEXT]               = $fields["PASSWD"];
        } else {
            $data["PASSWD"][TEXT]               = $fields["passwd"];
        }

        $data["INVALID_FLG"][TEXT]          = $fields["INVALID_FLG"];
        $data["PWDTERMCHK_FLG"][TEXT]       = $fields["PWDTERMCHK_FLG"];
        if ($model->isChgPwdUse) {
            $data["CHG_PWD_FLG"][TEXT]          = $fields["CHG_PWD_FLG"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "user_mst");

        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

    //DELETE
    public function &getDeleteQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM USER_MST ";
        $query .= " WHERE STAFFCD  = '" .$fields["USERSCD"] ."'";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

    //DELETE
    public function &getDeleteUsergroupQuery($fields, $year, $model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM USERGROUP_DAT ";
        $query .= " WHERE YEAR = '" .$year ."'";
        $query .= " AND STAFFCD = '" .$fields["USERSCD"] ."'";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

    //USER_PWD_HIST_MSTテーブルにデータ存在するか？
    public function getPassHist($staffcd, $passwd)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     USER_PWD_HIST_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$staffcd."' ";
        $query .= "     AND PASSWD = '".$passwd."' ";

        $histCnt = $db->getOne($query);
        Query::dbCheckIn($db);

        return $histCnt;
    }

    //USER_PWD_HIST_MSTテーブルINSERT
    public function getInsertHist($staffcd, $passwd)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["STAFFCD"][TEXT]          = $staffcd;
        $data["PASSWD"][TEXT]           = $passwd;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "USER_PWD_HIST_MST");
        $db->query($query);

        Query::dbCheckIn($db);

        return true;
    }

    //CSVデータ*****/
    //CSV書出し
    public function getCsvData($model)
    {
        $query  = " SELECT ";
        $query .= "     STAFF_M.STAFFCD AS STAFFCD, ";
        $query .= "     STAFF_M.STAFFNAME AS STAFFNAME, ";
        $query .= "     STAFF_M.STAFFNAME_KANA AS STAFFKANA, ";
        $query .= "     USER_M.USERID AS USERID, ";
        $query .= "     CASE ";
        $query .= "         WHEN USER_M.INVALID_FLG = '1' THEN USER_M.INVALID_FLG ELSE '' ";
        $query .= "     END AS INVALID_FLG, ";
        $query .= "     CASE ";
        $query .= "         WHEN USER_M.PWDTERMCHK_FLG = '1' THEN USER_M.PWDTERMCHK_FLG ELSE '' ";
        $query .= "     END AS PWDTERMCHK_FLG, ";
        if ($model->isChgPwdUse) {
            $query .= "     CASE ";
            $query .= "         WHEN USER_M.CHG_PWD_FLG = '1' THEN USER_M.CHG_PWD_FLG ELSE '' ";
            $query .= "     END AS CHG_PWD_FLG, ";
        }
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST STAFF_M ";
        $query .= " LEFT JOIN USER_MST USER_M ON STAFF_M.STAFFCD = USER_M.STAFFCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     AND USER_M.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "                     AND USER_M.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     STAFF_M.YEAR = '".$model->csvYear."' ";
        $query .= " ORDER BY ";
        $query .= "     STAFF_M.STAFFCD ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //職員存在チェック
    public function checkStaff($data, $model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND STAFFCD = '".$data["STAFFCD"]."' ";

        return $query;
    }

    //ユーザーＩＤチェック
    public function checkUserid($data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     USER_MST ";
        $query .= " WHERE ";
        $query .= "     USERID = '".$data["USERID"]."' ";

        return $query;
    }

    //ユーザーＩＤチェック２
    public function checkUserid2($data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     USER_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$data["STAFFCD"]."' ";

        return $query;
    }

    //PASSWD取得
    public function getPassCvs($cd, $model)
    {
        $query = "SELECT * FROM USER_MST WHERE STAFFCD = '" .$cd ."'";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //利用者マスタ登録データチェック
    public function getUserCount($setData)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     USER_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$setData["STAFFCD"]}' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $data = array();
        $db->autoCommit(false);

        $newPass = "pass";
        $setPass = md5($newPass);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //PASSWD取得
            $query  = knjz300Query::getPassCvs($data_arr[$i]["STAFFCD"], $model);
            $getPass = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //データセット
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]          = SCHOOLKIND;
            }
            $data["STAFFCD"][TEXT]        = $data_arr[$i]["STAFFCD"];
            $data["USERID"][TEXT]         = $data_arr[$i]["USERID"];
            $data["PASSWD"][TEXT]         = ($getPass["PASSWD"]) ? $getPass["PASSWD"] : $setPass;
            $data["INVALID_FLG"][TEXT]    = ($data_arr[$i]["INVALID_FLG"] == "" || $data_arr[$i]["INVALID_FLG"] == "0") ? "0" : "1";
            $data["PWDTERMCHK_FLG"][TEXT] = ($data_arr[$i]["PWDTERMCHK_FLG"] == "" || $data_arr[$i]["PWDTERMCHK_FLG"] == "0") ? "0" : "1";
            if ($model->isChgPwdUse) {
                $data["CHG_PWD_FLG"][TEXT]    = ($data_arr[$i]["CHG_PWD_FLG"] == "" || $data_arr[$i]["CHG_PWD_FLG"] == "0") ? "0" : "1";
            }
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]      = "SYSDATE()";
            //登録データチェック
            $query  = knjz300Query::getUserCount($data_arr[$i]);
            if (1 > $db->getOne($query)) {
                $query = Query::insertSQL($data, "USER_MST");
            } else {
                $where  = " WHERE ";
                $where .= "     STAFFCD = '{$data_arr[$i]["STAFFCD"]}' ";
                if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $where .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                    $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                }

                $query = Query::updateSQL($data, "USER_MST", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();

        return $cnt;
    }
}
