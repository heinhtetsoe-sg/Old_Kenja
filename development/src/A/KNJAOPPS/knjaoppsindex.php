<?php

require_once('for_php7.php');

require_once('knjaoppsModel.inc');
require_once('knjaoppsQuery.inc');

class knjaoppsController extends Controller {
    var $ModelClassName = "knjaoppsModel";
    var $ProgramID      = "KNJAOPPS";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjaoppsForm1");
                    break 2;
                case "sslApplet":
                    $sessionInstance->knjaoppsModel();
                    $this->callView("knjaoppsForm1");
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
                case "form2":
                    $this->callView("knjaoppsForm2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->getSendAuth();
                    $this->callView("knjaoppsForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjaoppsCtl = new knjaoppsController;
?>
