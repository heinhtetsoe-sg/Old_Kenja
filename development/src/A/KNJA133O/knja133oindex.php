<?php
require_once('knja133oModel.inc');
require_once('knja133oQuery.inc');

class knja133oController extends Controller {
    var $ModelClassName = "knja133oModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "http":
                case "https":
                case "knja133o":
                case "knja133o2":
                    $sessionInstance->knja133oModel();
                    $this->callView("knja133oForm1");
                    exit;
                case "sslApplet":
                    $sessionInstance->knja133oModel();
                    $this->callView("knja133oForm1");
                    exit;
                case "sslExe":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knja133o");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knja133oCtl = new knja133oController;
var_dump($_REQUEST);
?>
