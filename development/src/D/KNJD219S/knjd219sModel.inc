<?php

require_once('for_php7.php');


class knjd219sModel extends Model {
    var $semester;
    var $seme_sch;
    var $grade;
    var $exam;
    var $kind;
    var $item;
    var $sdiv;
    var $cmd;
    var $control;

    function knjd219sModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->semester = VARS::post("SEMESTER");
        $this->seme_sch = ($this->semester != "9") ? $this->semester : CTRL_SEMESTER;
        $this->grade = VARS::post("GRADE");

        $this->exam = VARS::post("EXAM");
        $testArray = explode("-", $this->exam);
        $this->test["TESTKINDCD"]   = $testArray[0];
        $this->test["TESTITEMCD"]   = $testArray[1];
        $this->test["SCORE_DIV"]    = $testArray[2];

        $this->subclassCd = VARS::post("SUBCLASSCD");

        $this->getPropertiesAll();
    }

    function &getMainModel()
    {
    }

    //事前処理チェック
    function validateInput() {
        return true;
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel() {
        //事前処理チェック
        if ($this->validateInput()){
            if (knjd219sQuery::ExecuteQuery($this)) {
                $this->setMessage("MSG202");
            }
        }
    }
}
?>
