<?php

require_once('for_php7.php');

require_once('knjb0210Model.inc');
require_once('knjb0210Query.inc');

class knjb0210Controller extends Controller {
    var $ModelClassName = "knjb0210Model";
    var $ProgramID      = "KNJB0210";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "read":
                case "setSub":
                case "knjb0210":
                    $sessionInstance->knjb0210Model();
                    $this->callView("knjb0210Form1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "copy":
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
$knjb0210Ctl = new knjb0210Controller;
var_dump($_REQUEST);
?>
