<?php

require_once('for_php7.php');

require_once('knjz021Model.inc');
require_once('knjz021Query.inc');

class knjz021Controller extends Controller {
    var $ModelClassName = "knjz021Model";
    var $ProgramID      = "KNJZ020"; //学校マスタメンテの権限

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "chenge":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ021");
                    $this->callView("knjz021Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ021");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("chenge");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 1;
                case "":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ021");
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
$knjz021Ctl = new knjz021Controller;
//var_dump($_REQUEST);
?>
