<?php

require_once('for_php7.php');

//生徒授業料軽減
class knjp177kModel extends model {
    var $cmd;                   //一覧表示時のcmd
    var $search = array();
    var $income     = array();
    var $schregno   = array();
    var $field      = array();

    function knjp177kModel() {}
    function init() {
        if(VARS::request("PROGRAMID")){
            $this->programid = VARS::request("PROGRAMID");
            unset($this->search);
            unset($this->income);
            unset($this->field);
            unset($this->config);
            unset($this->changePrg);
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd          = VARS::request("cmd");
        //検索条件
        $this->search = array("GRADE"    => VARS::request("GRADE_SEND"),
                              "HR_CLASS" => VARS::request("HR_CLASS_SEND"),
                              "PREF"     => VARS::post("PREF"),
                              "RAD_PREF" => VARS::post("RAD_PREF")
                             );
        if(VARS::get("SENDPRGID")){
            unset($this->search);
            unset($this->income);
            unset($this->field);
            unset($this->config);
            unset($this->changePrg);
            $this->search["GRADE"] = VARS::get("S_GRADE");
            $this->search["HR_CLASS"] = VARS::get("S_HR_CLASS");
            $this->search["PREF"] = VARS::get("S_PREF");
            $this->search["RAD_PREF"] = VARS::get("S_RAD_PREF");
        }
        //軽減額検索
        $this->income  = array("SCHREGNO"       => VARS::post("SCHREGNO"),
                               "GRADE"          => VARS::post("GRADE"),
                               "REDUC_INCOME_1" => VARS::post("REDUC_INCOME_1"),
                               "REDUC_INCOME_2" => VARS::post("REDUC_INCOME_2"),
                               "OBJ_NAME"       => VARS::post("OBJ_NAME")
                              );

        $checkGrade = $this->income["GRADE"] ? $this->income["GRADE"] : $this->search["GRADE"];
        $this->mstLastYear = $checkGrade == "01" ? CTRL_YEAR : CTRL_YEAR - 1;

        //画面のコンテンツ
        $this->field  = array("PASSNO"                  => VARS::post("PASSNO"),
                              "OFFSET_FLG"              => VARS::post("OFFSET_FLG"),
                              "REDUC_RARE_CASE_CD_1"    => VARS::post("REDUC_RARE_CASE_CD_1"),
                              "BASE_MONEY_1"            => VARS::post("BASE_MONEY_1"),
                              "BASE_MONEY_2"            => VARS::post("BASE_MONEY_2"),
                              "REDUC_ADD_FLG_1"         => VARS::post("REDUC_ADD_FLG_1"),
                              "REDUC_INCOME_1"          => VARS::post("REDUC_INCOME_1"),
                              "REDUCTION_SEQ_176_1"     => VARS::post("REDUCTION_SEQ_176_1"),
                              "REDUCTION_ADD_MONEY_1"   => VARS::post("REDUCTION_ADD_MONEY_1"),
                              "REDUC_RARE_CASE_CD_2"    => VARS::post("REDUC_RARE_CASE_CD_2"),
                              "REDUC_ADD_FLG_2"         => VARS::post("REDUC_ADD_FLG_2"),
                              "REDUC_INCOME_2"          => VARS::post("REDUC_INCOME_2"),
                              "REDUCTION_SEQ_176_2"     => VARS::post("REDUCTION_SEQ_176_2"),
                              "REDUCTION_ADD_MONEY_2"   => VARS::post("REDUCTION_ADD_MONEY_2"),
                              "REDUC_REMARK"            => VARS::post("REDUC_REMARK")
                            );

        //異動情報,奨学金情報の参照
        $this->grantTransfer  = array("SCHREGNO"  =>  VARS::post("SCHREGNO") );

        //軽減対象都道府県設定値
        $this->config  = array("REDUC_PREF"       =>  VARS::post("REDUC_PREF"),
                               "RAD_REDUC_PREF"  =>  VARS::post("RAD_REDUC_PREF")
                              );

        //検索対象都道府県
        if (!isset($this->search["RAD_PREF"])){
            $this->search["RAD_PREF"] = 1;
        }
        //軽減対象都道府県設定値
        if (!isset($this->config["RAD_REDUC_PREF"])){
            $this->config["RAD_REDUC_PREF"] = 1;
        }
    }

    function &getMainModel() {}

    // 課税総取得変更のモデルを返します
    function &getSendModel() {
        //金額入力
        $db = Query::dbCheckOut();
        //軽減額取得
        $query = knjp177kQuery::selectQueryReductionMonty($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $json_array = array();
        if (is_array($row)){
            foreach ($row as $key => $val) {
                $json_array[] = "'{$key}':'{$val}'";
            }
        }

        //軽減額取得
        $query = knjp177kQuery::selectQueryReductionAddMonty($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)){
            foreach ($row as $key => $val) {
                $json_array[] = "'{$key}':'{$val}'";
            }
            $json = implode(',', $json_array);
            echo "{ 'result':true,{$json} }";
        }
        if ($json == "") {
            echo "{ 'result':false }";
        }
        Query::dbCheckIn($db);
    }

    // 移動情報のモデルを返します
    function &getSendModelTransfer() {
        $db = Query::dbCheckOut();
        $returnValue = "";

        //移動情報取得
        $query = knjp177kQuery::selectQueryTransfer($this);
        $result2 = $db->query($query);
        while ($row = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            //T1.TRANSFERCD,T1.TRANSFER_SDATE,T1.TRANSFER_EDATE,T2.NAME1
            $returnValue .= "&nbsp;".$row["TRANSFER_SDATE"];
            $returnValue .= ($row["TRANSFER_EDATE"] == "")? "" : "～".$row["TRANSFER_EDATE"];
            $returnValue .= " ".$row["NAME1"]."<BR>";
        }

        if($returnValue != ""){
            echo $returnValue;
        }else{
            echo "NOTFOUND";
        }
        Query::dbCheckIn($db);
    }

    // 奨学金情報のモデルを返します
    function &getSendModelGrant() {
        $db = Query::dbCheckOut();
        $returnValue = "";

        //奨学金情報取得
        $query = knjp177kQuery::selectQueryGrant($this);
        $result2 = $db->query($query);
        while ($row = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            //T1.YEAR,T1.GRANTSDATE,T1.GRANTEDATE,T1.GRANT_MONEY,T1.GRANTCD,T2.NAME1
            $returnValue .= "&nbsp;".$row["YEAR"]."年度";
            $returnValue .= "&nbsp;".$row["GRANTSDATE"];
            $returnValue .= ($row["GRANTEDATE"] == "")? "" : "～".$row["GRANTEDATE"];
            $returnValue .= "&nbsp;".$row["GRANT_MONEY"]."円";
            $returnValue .= " ".$row["GRANTCD"].":".$row["NAME1"]."<BR>";
        }

        if($returnValue != ""){
            echo $returnValue;
        }else{
            echo "NOTFOUND";
        }
        Query::dbCheckIn($db);
    }

}
?>
