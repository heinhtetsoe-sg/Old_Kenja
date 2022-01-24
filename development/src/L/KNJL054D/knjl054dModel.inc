<?php

require_once('for_php7.php');

class knjl054dModel extends Model {
    var $cmd;
    var $year;
    var $applicantdiv;
    var $field = array();
    var $setDataArr = array();

    function knjl054dModel() {}

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->applicantdiv = "1";  //固定値

        //フィールド
        $this->field = array("SUBCLASS_CD"              =>  VARS::post("SUBCLASS_CD")              //科目
                            );

        foreach ($this->setDataArr as $key => $val) {
            $this->field["CORRECT_EXAMNO:".$key] = VARS::post("CORRECT_EXAMNO:".$key);
        }
    }

    function &getMainModel() {}

    //入力項目チェック
    function validateInput() {
        //空文字チェック
        if (!($this->field["SUBCLASS_CD"])) {
            $this->setWarning("MSG301", "( 科目 )");
            return false;
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjl054dQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
