<?php

require_once('for_php7.php');

require_once('knjp961Model.inc');
require_once('knjp961Query.inc');

class knjp961Controller extends Controller
{
    public $ModelClassName = "knjp961Model";
    public $ProgramID      = "KNJP961";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp961":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp961Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp961Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp961Ctl = new knjp961Controller();
