<?php

require_once('for_php7.php');

class knjz600Model extends Model {
    var $cmd;
    var $selectdata;    //新クラス（左リスト）
    var $selectdataLabel;    //新クラス（左リスト）
    var $control;

    function knjz600Model()
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
                            );


    }

    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjz600Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
