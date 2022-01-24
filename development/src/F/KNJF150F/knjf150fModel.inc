<?php

require_once('for_php7.php');

class knjf150fModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ

    public function knjf150fModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("SDATE"  => VARS::post("SDATE"), //対象日(開始)
                             "EDATE"  => VARS::post("EDATE"), //対象日(終了)
                             "DBNAME" => VARS::post("DBNAME") //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function clean()
    {
    }
}
