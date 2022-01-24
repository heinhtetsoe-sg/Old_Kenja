<?php

require_once('for_php7.php');

require_once('knjb180Model.inc');
require_once('knjb180Query.inc');

class knjb180Controller extends Controller {
    var $ModelClassName = "knjb180Model";
    var $ProgramID      = "KNJB180";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "error":
                    $this->callView("error");
                    break 2;
                case "output2":
                case "output1":
                    if (!$sessionInstance->OutputTmpFile()) {
                        $this->callView("knjb180Form1");
                    }
                    break 2;
                case "check":
                    if (!$sessionInstance->getDownloadCheckModel()) {
                        $this->callView("knjb180Form1");
                    }
                    break 2;
                case "":
                case "edit":
                case "main":
                    $this->callView("knjb180Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb180Ctl = new knjb180Controller;

//var_dump($_REQUEST);
?>
