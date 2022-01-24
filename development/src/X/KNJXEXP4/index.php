<?php

require_once('for_php7.php');

require_once('knjxexp4Model.inc');
require_once('knjxexp4Query.inc');

class knjxexp4Controller extends Controller {
    var $ModelClassName = "knjxexp4Model";
    var $ProgramID      = "KNJXEXP4";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "edit":
                case "select":
                case "search":
                case "search2":
                    $this->callView("knjxexp4Form1");
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
$knjxexp4Ctl = new knjxexp4Controller;
//var_dump($_REQUEST);
?>
