<?php

require_once('for_php7.php');

//定数定義

class knjl501aModel extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;
    var $sendAuth;
    var $auth;

    //メンバ変数の初期化
    function knjl501aModel()
    {
    }
    function init()
    {
        if(VARS::get("SEND_PRGID")){
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd               = VARS::request("cmd");
        $temp = VARS::post("year");
        if (isset($temp)) {
            $this->year          = VARS::post("year");
        }else{
            $this->year          = CTRL_YEAR+1;           //現在処理年度を初期値にする
        }
        $this->selectdata        = VARS::post("selectdata");
    }

    //入力項目チェック
    function validateInput()
    {
        if (!is_numeric($this->year)){
            $this->warning = "対象年度を選択してください。";
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjl501aQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
