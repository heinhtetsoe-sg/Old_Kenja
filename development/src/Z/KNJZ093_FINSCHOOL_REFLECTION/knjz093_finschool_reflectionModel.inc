<?php

require_once('for_php7.php');

class knjz093_finschool_reflectionModel extends Model
{
    public $cmd;
    public $control;
    public $selectFinschoolType;
    public $selectdata;
    public $auth;

    //メンバ変数の初期化
    public function knjz093_finschool_reflectionModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->selectFinschoolType);
            unset($this->selectdata);

            unset($this->sendAuth);
            unset($this->sendPrgid);
            unset($this->sendSubmit);
        }

        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");

        if ($this->cmd == "changeType") {
            $this->selectFinschoolType = VARS::post("SELECT_FINSCHOOL_TYPE");
        }

        if (VARS::get("SEND_PRGID")) {
            unset($this->selectFinschoolType);

            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->sendPrgid    = VARS::get("SEND_PRGID");
            $this->sendSubmit   = VARS::get("SEND_SUBMIT");
        }

        $this->auth = (VARS::get("SEND_PRGID")) ? $this->sendAuth : AUTHORITY;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->update();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        knjz093_finschool_reflectionQuery::getUpdateQuery($this);
        return;
    }
}
?>
