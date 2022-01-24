<?php

require_once('for_php7.php');

class knjm260bModel extends Model {

    var $field      = array();
    var $setData    = array();
    var $cmd;
    var $schregno;
    var $disabled = true;

    function knjm260bModel() {
       common::GetControlMaster_Fnc($this->control_data);

    }
    function init() {
        $this->cmd = VARS::request("cmd");

        $this->subclassCd = VARS::post("SUBCLASSCD");
        if ($this->subclassCd == "") {
            $this->subclassCd = VARS::post("H_SUBCLASSCD");
        }
        $this->chairCd    = VARS::post("CHAIRCD");
        if ($this->chairCd == "") {
            $this->chairCd = VARS::post("H_CHAIRCD");
        }

        $this->getPropertiesAll();
    }

    function getUpdData() {
        foreach ($this->schregNoArray as $key => $val) {
            $this->setData[$val] = array("R_VAL1"           =>  VARS::post("R_VAL1_".$val),         //合格
                                         "R_VAL2"           =>  VARS::post("R_VAL2_".$val),         //再提出
                                         "R_VAL3"           =>  VARS::post("R_VAL3_".$val),         //不合格
                                         "T_VAL1"           =>  VARS::post("T_VAL1_".$val),         //評価
                                         "S_VAL1"           =>  VARS::post("S_VAL1_".$val),         //視聴報告
                                         "DBNAME"           =>  VARS::post("DBNAME")                //DB変更に備えて
                                         );
        }

    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {

            $this->getUpdData();

            knjm260bQuery::DeleteQuery($this);
            knjm260bQuery::InsertQuery($this);

            $this->setMessage("MSG201");
        }
    }

}
?>
