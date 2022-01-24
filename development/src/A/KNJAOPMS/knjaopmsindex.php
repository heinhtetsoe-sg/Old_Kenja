<?php

require_once('for_php7.php');

require_once('knjaopmsModel.inc');
require_once('knjaopmsQuery.inc');

class knjaopmsController extends Controller {
    var $ModelClassName = "knjaopmsModel";
    var $ProgramID      = "KNJAOPMS";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjaopmsForm1");
                    break 2;
                case "sslApplet":
                    $sessionInstance->knjaopmsModel();
                    $this->callView("knjaopmsForm1");
                    exit;
                case "sslExe":
                    $sessionInstance->getShomeiModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "shomei":
                    $sessionInstance->getShomeiModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "sasimodosi":
                    $sessionInstance->getSasimodosiModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->getSendAuth();
                    $this->callView("knjaopmsForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjaopmsCtl = new knjaopmsController;
?>
