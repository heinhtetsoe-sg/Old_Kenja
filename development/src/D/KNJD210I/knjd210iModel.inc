<?php

require_once('for_php7.php');


class knjd210iModel extends Model {
    var $field = array();
    var $seme;
    var $cmd;
    var $control;
    var $selectdata;
    var $z010;

    function knjd210iModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->field = array("GRADE"        => VARS::post("GRADE"),
                             "SEMESTER"     => VARS::post("SEMESTER"),
                             "TESTKIND"     => VARS::post("TESTKIND"),
                             "COURSE"       => VARS::post("COURSE")
                            );
        $this->seme = $this->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $this->field["SEMESTER"];
        //処理対象科目(左のリスト）
        $this->selectdata = VARS::post("selectdata");
        //プロパティファイル
        $this->getPropertiesAll();
    }

    function &getMainModel()
    {
    }

    //事前処理チェック
    function validateInput()
    {
        return true;
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        //事前処理チェック
        if ($this->validateInput()){
            if (knjd210iQuery::insertExe($this)) {
                $this->setMessage("MSG202");
            }
        }
    }
}
?>
