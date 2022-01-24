<?php

require_once('for_php7.php');

require_once('knjb0200Model.inc');
require_once('knjb0200Query.inc');

class knjb0200Controller extends Controller {
    var $ModelClassName = "knjb0200Model";
    var $ProgramID      = "KNJB0200";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "read":
                case "setSub":
                case "knjb0200":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjb0200Model();
                    $this->callView("knjb0200Form1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "heikouUpdate":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateHeikouModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb0200Ctl = new knjb0200Controller;
var_dump($_REQUEST);
?>
