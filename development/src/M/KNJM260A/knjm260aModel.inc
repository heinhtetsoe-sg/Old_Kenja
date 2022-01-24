<?php

require_once('for_php7.php');
class knjm260aModel extends Model {

    var $field      = array();
    var $setData    = array();
    var $cmd;
    var $schregno;

    function knjm260aModel()
    {
       common::GetControlMaster_Fnc($this->control_data);

    }
    function init()
    {
        $this->field = array("SCHREGNO" =>  VARS::post("SCHREGNO"));

        $this->cmd = VARS::request("cmd");
        $this->schregno = VARS::request("SCHREGNO");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    function getUpdData()
    {
        foreach ($this->subclassArray as $key => $val) {
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
    function validateInput()
    {
		return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){

            $this->getUpdData();

            knjm260aQuery::DeleteQuery($this);
            knjm260aQuery::InsertQuery($this);
            $this->schregno = $this->field["SCHREGNO"];

            $this->setMessage("MSG201");
        }
    }

}
?>
