<?php

require_once('for_php7.php');

class knjxselect_staffModel extends Model
{
    public $field = array();
    public $cmd;
    public $textCd;
    public $textName;
    public $control;       //コントロールマスタ

    public function knjxselect_staffModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        //STAFFCD、STAFFNAMEを表示するテキスト名取得
        if (VARS::get("TEXT_CD")) {
            $this->textCd = VARS::get("TEXT_CD");
        }
        if (VARS::get("TEXT_NAME")) {
            $this->textName = VARS::get("TEXT_NAME");
        }
    }
}
