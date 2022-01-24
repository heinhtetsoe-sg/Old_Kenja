<?php

require_once('for_php7.php');


// kanji=漢字

class knjd210cModel extends Model {
    var $field = array();
    var $seme;
    var $cmd;
    var $control;

    function knjd210cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->field = array("SEMESTER" => VARS::post("SEMESTER"),
                             "GRADE"    => VARS::post("GRADE"),
                             "TESTKIND" => VARS::post("TESTKIND"));
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
            if (knjd210cQuery::insertExe($this)) {
                $this->setMessage("MSG202");
            }
        }
    }
}
?>
