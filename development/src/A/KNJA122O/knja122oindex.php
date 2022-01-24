<?php
require_once('knja122oModel.inc');
require_once('knja122oQuery.inc');

class knja122oController extends Controller {
    var $ModelClassName = "knja122oModel";
    var $ProgramID      = "KNJA122O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "https":
                case "knja122o":
                case "knja122o2":
                    $sessionInstance->knja122oModel();
                    $this->callView("knja122oForm1");
                    exit;
                case "sslApplet":
                    $sessionInstance->knja122oModel();
                    $this->callView("knja122oForm1");
                    exit;
                case "sslExe":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knja122o");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knja122oCtl = new knja122oController;
var_dump($_REQUEST);
?>
