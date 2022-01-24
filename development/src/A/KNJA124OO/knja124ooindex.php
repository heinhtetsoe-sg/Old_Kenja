<?php
require_once('knja124ooModel.inc');
require_once('knja124ooQuery.inc');

class knja124ooController extends Controller {
    var $ModelClassName = "knja124ooModel";
    var $ProgramID      = "KNJA124OO";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knja124ooForm1");
                    break 2;
                case "sslApplet":
                    $sessionInstance->knja124ooModel();
                    $this->callView("knja124ooForm1");
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
                    $this->callView("knja124ooForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja124ooCtl = new knja124ooController;
?>
