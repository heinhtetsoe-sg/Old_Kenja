<?php

require_once('for_php7.php');

require_once('knjm270Model.inc');
require_once('knjm270Query.inc');

class knjm270Controller extends Controller {
    var $ModelClassName = "knjm270Model";
    var $ProgramID      = "KNJM270";

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
                    $this->callView("knjm270SubForm1");
                    break 2;
                case "":
                case "read":
                case "addread":
                case "main":
                    $this->callView("knjm270Form1");
                    break 2;
                case "dsub":
                    $this->callView("knjm270Form1");
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
$knjm270Ctl = new knjm270Controller;
//var_dump($_REQUEST);
?>
