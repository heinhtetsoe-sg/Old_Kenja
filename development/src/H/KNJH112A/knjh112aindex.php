<?php

require_once('for_php7.php');

require_once('knjh112aModel.inc');
require_once('knjh112aQuery.inc');

class knjh112aController extends Controller {
    var $ModelClassName = "knjh112aModel";
    var $ProgramID      = "KNJH112A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh112a":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh112aModel();
                    $this->callView("knjh112aForm1");
                    exit;
                case "csv":         //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjh112aForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh112aCtl = new knjh112aController;
//var_dump($_REQUEST);
?>
