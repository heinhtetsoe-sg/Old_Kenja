<?php

require_once('for_php7.php');


require_once('knje065iModel.inc');
require_once('knje065iQuery.inc');

class knje065iController extends Controller {
    var $ModelClassName = "knje065iModel";
    var $ProgramID      = "KNJE065I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje065iForm1");
                    break 2;
                case "main":
                    $this->callView("knje065iForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knje065iCtl = new knje065iController;
?>
