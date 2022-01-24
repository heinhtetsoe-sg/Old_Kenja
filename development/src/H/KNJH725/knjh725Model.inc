<?php
require_once('for_php7.php');

class knjh725Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;//コントロールマスタ

    public function knjh725Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->field = array("TESTDIV" => VARS::post("TOTAL") == null ? VARS::post("TESTDIV") : "9"); //学力テスト区分

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //実行処理
    public function getExecModel()
    {
        knjh725Query::executeQuery($this);
        if (knjh725Query::executeRireki($this)) {
            $this->setMessage("MSG202");
        }
    }
}
