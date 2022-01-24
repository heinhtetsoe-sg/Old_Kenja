<?php

require_once('for_php7.php');

//メニュー画面
class knjl084jModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $ObjYear;
    var $startnumber;

    function knjl084jModel()
    {
        $this->ObjYear = CTRL_YEAR+1;
    }

    function init()
    {
        $this->cmd         = VARS::request("cmd");
        $this->startnumber = sprintf("%06d", VARS::request("STARTNUMBER"));
    }

    //入力項目チェック
    function validateInput()
    {
        $db = Query::dbCheckOut();

        //番号オーバーチェック
        if(0 < $db->getOne(knjl084jQuery::checkSuccess($this))){
            $this->setWarning("MSG915","（開始番号）");
            return false;
        }

        Query::dbCheckIn($db);
        return true;
    }

    // 確定のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            knjl084jQuery::updateNumber($this);
            $this->setMessage("MSG201");
        }
    }

    // クリアのモデルを返します
    function &getClearModel()
    {
        knjl084jQuery::ClearNumber($this);

        $this->setMessage("MSG201");
    }

}
?>
