<?php

require_once('for_php7.php');

class knjh410_seisekiModel extends Model {
    var $cmd;
    var $selectdata;    //新クラス（左リスト）
    var $selectdataLabel;    //新クラス（左リスト）
    var $control;

    function knjh410_seisekiModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->schregno = array();
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->GAKUSEKI = VARS::request("SCHREGNO") != "" ? VARS::request("SCHREGNO") : $this->GAKUSEKI;
        
        $this->year = VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->year;
        
        
        $this->field = array("GYOUSYA"               =>  VARS::post("GYOUSYA") != "" ? VARS::post("GYOUSYA") : "0",
                             
                             "KATA"                  =>  VARS::post("KATA") != "" ? VARS::post("KATA") : "0",
                             
                             "SYUBETU"                  =>  VARS::post("SYUBETU") != "" ? VARS::post("SYUBETU") : "0",
                             
                             "CHECK"                  =>  VARS::post("CHECK") != "" ? VARS::post("CHECK") : array(),
                             
                            );
        if($this->cmd == "syubetu_change"){
            $this->field["CHECK"] = array();
        }
        if($this->cmd == "sanka" || empty($this->field["CHECK"])){
            if($this->field["KATA"] == "0"){
                $this->field["CHECK"] = array("100101", "100201", "100401");
            }else{
                if($this->field["GYOUSYA"] != "2"){
                    $this->field["CHECK"] = array("120001", "120002");
                }else{
                    $this->field["CHECK"] = array();
                }
            }
        }

    }

    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjh410_seisekiQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
