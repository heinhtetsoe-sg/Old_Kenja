<?php
//メニュー画面
class knjl142kModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $StrExamno;             //開始受験番号
    var $EndExamno;             //終了受験番号
    function knjl142kModel()
    {
    }
        
    function init()
    {
        $this->cmd    = VARS::request("cmd");
        if ($this->cmd == "") {
            $this->StrExamno = "";
            $this->EndExamno = "";
        }

        $this->year   = CTRL_YEAR + 1;

        $this->delchk = VARS::post("DELCHK");
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            if (is_array($this->delchk)) {
                knjl142kQuery::getUpdateQuery($this);
                $this->setMessage("MSG201");
            }
        }
        return;
    }
}
?>
