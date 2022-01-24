<?php

require_once('for_php7.php');

require_once('knjz022Model.inc');
require_once('knjz022Query.inc');

class knjz022Controller extends Controller {
    var $ModelClassName = "knjz022Model";
    var $ProgramID      = "KNJZ020"; //学校マスタメンテの権限

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "main2":
                case "change":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ022");
                    $this->callView("knjz022Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ022");
                    $this->checkAuth(DEF_UPDATABLE);
                    if ($sessionInstance->getUpdateModel()) {
                        $sessionInstance->setCmd("main");
                    } else {
                        $sessionInstance->setCmd("main2");
                    }
                    break 1;
                case "error":
                    $this->callView("error");
                    break 1;
                case "":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ022");
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
$knjz022Ctl = new knjz022Controller;
?>
