<?php

require_once('for_php7.php');


class knjd212eModel extends Model {
    var $semester;
    var $grade;
    var $exam;
    var $cmd;
    var $control;

    function knjd212eModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND");
        $this->grade = VARS::post("GRADE");
        $this->semester = VARS::post("SEMESTER");
        $this->testcd = VARS::post("TESTCD");
        $testArray = explode("-", $this->testcd);
        $this->test["SEMESTER"]     = $testArray[0];
        $this->test["TESTKINDCD"]   = $testArray[1];
        $this->test["TESTITEMCD"]   = $testArray[2];
        $this->test["SCORE_DIV"]    = $testArray[3];

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function &getMainModel()
    {
    }

    //事前処理チェック
    function validateInput() {
        $db = Query::dbCheckOut();
        $query = knjd212eQuery::getMikomiRateQuery($this);
        $this->mikomiRate = $db->getOne($query);
        Query::dbCheckIn($db);
        if (!strlen($this->mikomiRate)) {
            $this->setWarning("MSG305", "\\n見込点算出比率が設定されているか確認して下さい。");
            return false;
        }
        return true;
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel() {
        //事前処理チェック
        if ($this->validateInput()){
            if (knjd212eQuery::ExecuteQuery($this)) {
                $this->setMessage("MSG202");
            }
        }
    }
}
?>
