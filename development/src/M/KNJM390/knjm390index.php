<?php

require_once('for_php7.php');

require_once('knjm390Model.inc');
require_once('knjm390Query.inc');

class knjm390Controller extends Controller {
    var $ModelClassName = "knjm390Model";
    var $ProgramID      = "KNJM390";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("addread");
                    break 1;
                case "alldel":
                case "chdel":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read2");
                    break 1;
                case "subform1":
                case "read2":
                case "reset":
                    $this->callView("knjm390SubForm1");
                    break 2;
                case "":
                case "change":
                case "read":
                case "addread":
                case "main":
                    $this->callView("knjm390Form1");
                    break 2;
                case "dsub":
                    $this->callView("knjm390Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm390Ctl = new knjm390Controller;
//var_dump($_REQUEST);
?>
