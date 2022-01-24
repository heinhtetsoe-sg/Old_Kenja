<?php

require_once('for_php7.php');

class knjl343qModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;

    public function knjl343qModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV")           //入試区分
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
