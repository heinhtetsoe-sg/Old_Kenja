<?php

require_once('for_php7.php');

require_once('knja133sModel.inc');
require_once('knja133sQuery.inc');

class knja133sController extends Controller {
    var $ModelClassName = "knja133sModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "http":
                case "https":
                case "knja133s":
                case "knja133s2":
                    $sessionInstance->knja133sModel();
                    $this->callView("knja133sForm1");
                    exit;
                case "sslApplet":
                    $sessionInstance->knja133sModel();
                    $this->callView("knja133sForm1");
                    exit;
                case "sslExe":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knja133s");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knja133sCtl = new knja133sController;
var_dump($_REQUEST);
?>
