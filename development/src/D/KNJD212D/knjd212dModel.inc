<?php

require_once('for_php7.php');


class knjd212dModel extends Model {
    var $semester;
    var $grade;
    var $exam;
    var $cmd;
    var $control;

    function knjd212dModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->semester = VARS::post("SEMESTER");
        $this->grade = VARS::post("GRADE");
        $this->field = array(
                           "RECORD_DIV" => VARS::post("RECORD_DIV")
                       );

        $this->exam = "";
        if ($this->field["RECORD_DIV"] == "1") {
            $this->exam = $this->semester."-99-00-09"; // 学期評定
        } else if ($this->field["RECORD_DIV"] == "2") {
            $this->exam = "9-99-00-09"; // 学年評定
        }
        $testArray = explode("-", $this->exam);
        $this->test["SEMESTER"]     = $testArray[0];
        $this->test["TESTKINDCD"]   = $testArray[1];
        $this->test["TESTITEMCD"]   = $testArray[2];
        $this->test["SCORE_DIV"]    = $testArray[3];

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
            if (knjd212dQuery::ExecuteQuery($this)) {
                $this->setMessage("MSG202");
            }
        }
    }
}
?>
