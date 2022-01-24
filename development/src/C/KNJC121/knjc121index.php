<?php

require_once('for_php7.php');

require_once('knjc121Model.inc');
require_once('knjc121Query.inc');

class knjc121Controller extends Controller
{
    public $ModelClassName = "knjc121Model";
    public $ProgramID      = "KNJC121";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                    $sessionInstance->knjc121Model();
                    $this->callView("knjc121Form1");
                    exit;
                case "knjc121":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjc121Model();
                    $this->callView("knjc121Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjc121Model();
                    $this->callView("knjc121Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc121Ctl = new knjc121Controller();
var_dump($_REQUEST);
