<?php

require_once('for_php7.php');
//生徒授業料軽減
class knjp171kModel extends model {
    var $cmd;                   //一覧表示時のcmd
    var $search = array();
    var $income     = array();
    var $schregno   = array();
    var $field      = array();

    function knjp171kModel() {}
    function init() {
        if(VARS::request("PROGRAMID")){
            $this->programid = VARS::request("PROGRAMID");
            unset($this->search);
            unset($this->income);
            unset($this->field);
            unset($this->config);
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd          = VARS::request("cmd");
        //検索条件
        $this->search = array("GRADE"    => VARS::post("GRADE"),
                              "HR_CLASS" => VARS::post("HR_CLASS"),
                              "PREF"     => VARS::post("PREF"),
                              "RAD_PREF" => VARS::post("RAD_PREF")
                             );
        //軽減額検索
        $this->income  = array("SCHREGNO"      => VARS::post("SCHREGNO"),
                               "REDUC_INCOME_1"=> VARS::post("REDUC_INCOME_1"),
                               "REDUC_INCOME_2"=> VARS::post("REDUC_INCOME_2"),
                               "OBJ_NAME"      => VARS::post("OBJ_NAME")
                              );
        //画面のコンテンツ
        $this->field  = array("PASSNO"                  => VARS::post("PASSNO"),
                              "OFFSET_FLG"              => VARS::post("OFFSET_FLG"),
                              "REDUC_DEC_FLG_1"         => VARS::post("REDUC_DEC_FLG_1"),
                              "BASE_MONEY_1"            => VARS::post("BASE_MONEY_1"),
                              "REDUC_ADD_FLG_1"         => VARS::post("REDUC_ADD_FLG_1"),
                              "REDUC_INCOME_1"          => VARS::post("REDUC_INCOME_1"),
                              "REDUCTION_SEQ_1"         => VARS::post("REDUCTION_SEQ_1"),
                              "REDUCTION_ADD_MONEY_1"   => VARS::post("REDUCTION_ADD_MONEY_1"),
                              "REDUC_ADD_FLG_2"         => VARS::post("REDUC_ADD_FLG_2"),
                              "REDUC_INCOME_2"          => VARS::post("REDUC_INCOME_2"),
                              "REDUCTION_SEQ_2"         => VARS::post("REDUCTION_SEQ_2"),
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

    //入力項目チェック
    function validateInput() {
        for($i = 0; $i < get_count($this->schregno); $i++){
            $this->field["REDUC_INCOME_1"][$i] = VARS::post("SET_REDUC_INCOME1_".$this->schregno[$i]);
            if (is_numeric($this->field["REDUC_INCOME_1"][$i]) && strlen($this->field["REDUC_INCOME_1"][$i]) > 8){
                $this->setWarning('MSG901',"課税総取得額　学籍番号：" .$this->schregno[$i]);
                return false;
            }
            $this->field["REDUC_INCOME_2"][$i] = VARS::post("SET_REDUC_INCOME2_".$this->schregno[$i]);
            if (is_numeric($this->field["REDUC_INCOME_2"][$i]) && strlen($this->field["REDUC_INCOME_2"][$i]) > 8){
                $this->setWarning('MSG901',"課税総取得額　学籍番号：" .$this->schregno[$i]);
                return false;
            }
            if (strlen($this->field["REDUC_REMARK"][$i]) > 30){
                $this->setWarning('MSG901',"備考　学籍番号：" .$this->schregno[$i]);
                return false;
            }
            if ($this->field["REDUC_INCOME_1"][$i] != "" && $this->field["REDUCTION_ADD_MONEY_1"][$i] == ""){
                $this->field["REDUCTION_ADD_MONEY_1"][$i] = 0;
            }
            if ($this->field["REDUC_INCOME_2"][$i] != "" && $this->field["REDUCTION_ADD_MONEY_2"][$i] == ""){
                $this->field["REDUCTION_ADD_MONEY_2"][$i] = 0;
            }
        }
        return true;
    }

    /**
     * 更新用のモデルを返します
    */
    function &getUpdateModel() {
        if ($this->validateInput()){
            knjp171kQuery::update($this);
            $this->setMessage("MSG201");
        }
    }

    // 課税総取得変更のモデルを返します
    function &getSendModel() {
        //金額入力
        $db = Query::dbCheckOut();
        //軽減額取得
        $query = knjp171kQuery::selectQueryReductionMonty($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)){
            $json_array = array();
            foreach ($row as $key => $val) {
                $json_array []= "'{$key}':'{$val}'";
            }
            $json = implode(',', $json_array);
            echo "{ 'result':true,{$json} }";
        }else{
            echo "{ 'result':false }";
        }
        Query::dbCheckIn($db);
    }

    // 移動情報のモデルを返します
    function &getSendModelTransfer() {
        $db = Query::dbCheckOut();
        $returnValue = "";

        //移動情報取得
        $query = knjp171kQuery::selectQueryTransfer($this);
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
        $query = knjp171kQuery::selectQueryGrant($this);
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
