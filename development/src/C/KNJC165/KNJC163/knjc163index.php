<?php

require_once('for_php7.php');

require_once('knjc163Model.inc');
require_once('knjc163Query.inc');

class knjc163Controller extends Controller {
    var $ModelClassName = "knjc163Model";
    var $ProgramID      = "KNJC163";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc163":
                    $sessionInstance->knjc163Model();
                    $this->callView("knjc163Form1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjc163Form1");
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
$knjc163Ctl = new knjc163Controller;
//var_dump($_REQUEST);
?>
