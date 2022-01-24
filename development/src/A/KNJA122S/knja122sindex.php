<?php

require_once('for_php7.php');

require_once('knja122sModel.inc');
require_once('knja122sQuery.inc');

class knja122sController extends Controller {
    var $ModelClassName = "knja122sModel";
    var $ProgramID      = "KNJA122S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "https":
                case "knja122s":
                case "knja122s2":
                    $sessionInstance->knja122sModel();
                    $this->callView("knja122sForm1");
                    exit;
                case "sslApplet":
                    $sessionInstance->knja122sModel();
                    $this->callView("knja122sForm1");
                    exit;
                case "sslExe":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knja122s");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knja122sCtl = new knja122sController;
var_dump($_REQUEST);
?>
