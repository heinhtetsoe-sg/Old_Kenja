<?php

require_once('for_php7.php');

require_once('knjz190Model.inc');
require_once('knjz190Query.inc');

class knjz190Controller extends Controller {
    var $ModelClassName = "knjz190Model";
    var $ProgramID      = "KNJZ190";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjz190Form1");
                    break 2;
                case "clear":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                case "read":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
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
$knjz190Ctl = new knjz190Controller;
//var_dump($_REQUEST);
?>
