<?php

require_once('for_php7.php');

require_once('knjd126sModel.inc');
require_once('knjd126sQuery.inc');

class knjd126sController extends Controller {
    var $ModelClassName = "knjd126sModel";
    var $ProgramID      = "KNJD126S";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "form1":
                case "select1":
                case "reset":
                    $this->callView("knjd126sForm1");
                    break 2;
                case "form2":
                case "select2":
                case "form2_reset":
                    $this->callView("knjd126sForm2");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    break 1;
                case "error":
                    $this->callView("error");
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
$knjd126sCtl = new knjd126sController;
?>
