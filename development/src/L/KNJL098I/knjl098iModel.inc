<?php

class knjl098iModel extends model
{
    var $field = array();
    var $cmd;
    var $ObjYear;
    var $data;

    function knjl098iModel()
    {
    }

    function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "EXAM_TYPE"        =>  "1"                             //受験型
                             );
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->field["APPLICANTDIV"] == "") {
            $this->setWarning("MSG301","( 入試制度 )");
            return false;
        }
        if ($this->field["TESTDIV"] == "") {
            $this->setWarning("MSG301","( 入試区分 )");
            return false;
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            //更新処理
            knjl098iQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
