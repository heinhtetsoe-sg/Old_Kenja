<?php

require_once('for_php7.php');

require_once('knjz212Model.inc');
require_once('knjz212Query.inc');

class knjz212Controller extends Controller {
    var $ModelClassName = "knjz212Model";
    var $ProgramID      = "KNJZ212";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "cmbChange":
                case "reset":
                case "read":
                    $sessionInstance->getMainModel();
                    $this->callView("knjz212Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
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
$knjz212Ctl = new knjz212Controller;
?>
