<?php

require_once('for_php7.php');

require_once('knjd129jModel.inc');
require_once('knjd129jQuery.inc');

class knjd129jController extends Controller {
    var $ModelClassName = "knjd129jModel";
    var $ProgramID      = "KNJD129J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjd129jForm1");
                    break 2;
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
$knjd129jCtl = new knjd129jController;
?>
