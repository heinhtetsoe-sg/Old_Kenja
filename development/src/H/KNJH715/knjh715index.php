<?php

require_once('for_php7.php');

require_once('knjh715Model.inc');
require_once('knjh715Query.inc');

class knjh715Controller extends Controller
{
    public $ModelClassName = "knjh715Model";
    public $ProgramID      = "KNJH715";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjh715":
                    $sessionInstance->knjh715Model();
                    $this->callView("knjh715Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh715Ctl = new knjh715Controller();
