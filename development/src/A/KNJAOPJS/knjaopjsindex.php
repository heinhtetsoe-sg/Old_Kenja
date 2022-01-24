<?php

require_once('for_php7.php');

require_once('knjaopjsModel.inc');
require_once('knjaopjsQuery.inc');

class knjaopjsController extends Controller {
    var $ModelClassName = "knjaopjsModel";
    var $ProgramID      = "KNJAOPJS";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjaopjsForm1");
                    break 2;
                case "sslApplet":
                    $sessionInstance->knjaopjsModel();
                    $this->callView("knjaopjsForm1");
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
                    $this->callView("knjaopjsForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjaopjsCtl = new knjaopjsController;
?>
