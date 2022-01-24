<?php

require_once('for_php7.php');

require_once('knjc164Model.inc');
require_once('knjc164Query.inc');

class knjc164Controller extends Controller {
    var $ModelClassName = "knjc164Model";
    var $ProgramID      = "KNJC164";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "changeKind":
                case "knjc164":
                    $sessionInstance->knjc164Model();
                    $this->callView("knjc164Form1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjc164Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID); 
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc164Ctl = new knjc164Controller;
//var_dump($_REQUEST);
?>
