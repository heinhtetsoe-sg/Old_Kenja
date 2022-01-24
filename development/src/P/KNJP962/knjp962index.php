<?php

require_once('for_php7.php');

require_once('knjp962Model.inc');
require_once('knjp962Query.inc');

class knjp962Controller extends Controller
{
    public $ModelClassName = "knjp962Model";
    public $ProgramID      = "KNJP962";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp962":                       //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp962Model(); //コントロールマスタの呼び出し
                    $this->callView("knjp962Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp962Ctl = new knjp962Controller();
