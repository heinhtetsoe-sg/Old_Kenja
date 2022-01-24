<?php

require_once('for_php7.php');

require_once('knjh713Model.inc');
require_once('knjh713Query.inc');

class knjh713Controller extends Controller
{
    public $ModelClassName = "knjh713Model";
    public $ProgramID      = "KNJH713";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh713":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh713Model();      //コントロールマスタの呼び出し
                    $this->callView("knjh713Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh713Ctl = new knjh713Controller();
