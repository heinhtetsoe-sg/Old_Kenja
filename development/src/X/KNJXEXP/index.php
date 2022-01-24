<?php

require_once('for_php7.php');

require_once('knjxexpModel.inc');
require_once('knjxexpQuery.inc');

class knjxexpController extends Controller
{
    public $ModelClassName = "knjxexpModel";
    public $ProgramID      = "KNJXEXP";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "right":
                    $this->callView("knjxSearch");
                    break 2;
                case "list":
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjxexpForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("list");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxexpCtl = new knjxexpController;
//var_dump($_REQUEST);
