<?php

require_once('for_php7.php');

require_once('knjb170Model.inc');
require_once('knjb170Query.inc');

class knjb170Controller extends Controller {
    var $ModelClassName = "knjb170Model";
    var $ProgramID      = "KNJB170";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "error":
                    $this->callView("error");
                    break 2;
                case "output":
                    if (!$sessionInstance->OutputTmpFile()) {
                        $this->callView("knjb170Form1");
                    }
                    break 2;
                case "check":
                    $sessionInstance->getDownloadCheckModel();
                    break 2;
                case "":
                case "edit":
                case "main":
                    $this->callView("knjb170Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb170Ctl = new knjb170Controller;

//var_dump($_REQUEST);
?>
