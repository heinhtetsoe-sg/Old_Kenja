<?php
require_once('for_php7.php');
require_once('knjh725Model.inc');
require_once('knjh725Query.inc');

class knjh725Controller extends Controller
{
    public $ModelClassName = "knjh725Model";
    public $ProgramID      = "KNJH725";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjh725Form1");
                    break 2;
                case "exec":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh725Ctl = new knjh725Controller();
