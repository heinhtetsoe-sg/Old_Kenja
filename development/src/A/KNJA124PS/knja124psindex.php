<?php

require_once('for_php7.php');

require_once('knja124psModel.inc');
require_once('knja124psQuery.inc');

class knja124psController extends Controller {
    var $ModelClassName = "knja124psModel";
    var $ProgramID      = "KNJA124PS";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knja124psForm1");
                    break 2;
                case "sslApplet":
                    $sessionInstance->knja124psModel();
                    $this->callView("knja124psForm1");
                    exit;
                case "sslExe":
                    $sessionInstance->getShomeiModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "shomei":
                    $sessionInstance->getShomeiModel();
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
                    $this->callView("knja124psForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja124psCtl = new knja124psController;
?>
