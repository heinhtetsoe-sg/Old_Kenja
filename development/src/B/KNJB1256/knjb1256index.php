<?php

require_once('for_php7.php');

require_once('knjb1256Model.inc');
require_once('knjb1256Query.inc');

class knjb1256Controller extends Controller {
    var $ModelClassName = "knjb1256Model";
    var $ProgramID      = "KNJB1256";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "subMain":
                case "changeHr";
                case "clear";
                    $sessionInstance->knjb1256Model();
                    $this->callView("knjb1256Form1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("subMain");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("subMain");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1256Ctl = new knjb1256Controller;
?>
