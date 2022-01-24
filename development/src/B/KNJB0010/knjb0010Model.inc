<?php

require_once('for_php7.php');

class knjb0010Model extends Model
{
    public $cmd;
    public $groupcd = "";
    public $name = "";
    public $ctrl;
    public $field = array();

    public function knjb0010Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($model->groupcd);
            unset($this->name);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("GROUPCD")!="") {
            $this->groupcd = VARS::get("GROUPCD");
        }

        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }

        $this->field = array("LESSONCNT"   => VARS::post("LESSONCNT"),
                             "FRAMECNT"    => VARS::post("FRAMECNT"),
                             "CHAIRCD"     => VARS::post("selectdata"),
                             "ATTENDCLASS" => VARS::post("selectdata2"));

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    //更新
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjb0010Query::updateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //前年度コピー
    public function getCopyModel()
    {
        knjb0010Query::copyQuery($this);
    }

    //削除
    public function getDeleteModel()
    {
        $this->setMessage("MSG201");
    }
}
?>
