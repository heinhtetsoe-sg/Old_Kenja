<?php

require_once('for_php7.php');

require_once('knjxexpkModel.inc');
require_once('knjxexpkQuery.inc');

class knjxexpkController extends Controller {
    var $ModelClassName = "knjxexpkModel";
    var $ProgramID      = "knjxexpk";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "right":
                    $this->callView("knjxSearch");
                    break 2;
                case "show":
                    $this->callView("knjxSearch".$sessionInstance->showno);
                    break 2;
                case "list":
                case "search":
                case "search2":
                case "search3":
                case "search4":
                case "search5":
                case "search6":
                    $this->callView("knjxexpkForm1");
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
$knjxexpkCtl = new knjxexpkController;
//var_dump($_REQUEST);
?>
