<?php

require_once('for_php7.php');

require_once('knjd181Model.inc');
require_once('knjd181Query.inc');

class knjd181Controller extends Controller
{
    public $ModelClassName = "knjd181Model";
    public $ProgramID      = "KNJD181";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear":
                case "knjd181":
                    $sessionInstance->knjd181Model();
                    $this->callView("knjd181Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd181Ctl = new knjd181Controller();
