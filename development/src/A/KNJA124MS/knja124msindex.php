<?php

require_once('for_php7.php');

require_once('knja124msModel.inc');
require_once('knja124msQuery.inc');

class knja124msController extends Controller {
    var $ModelClassName = "knja124msModel";
    var $ProgramID      = "KNJA124MS";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knja124msForm1");
                    break 2;
                case "sslApplet":
                    $sessionInstance->knja124msModel();
                    $this->callView("knja124msForm1");
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
                    $this->callView("knja124msForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja124msCtl = new knja124msController;
?>
