<?php

require_once('for_php7.php');

require_once('knjf307aModel.inc');
require_once('knjf307aQuery.inc');

class knjf307aController extends Controller {
    var $ModelClassName = "knjf307aModel";
    var $ProgramID      = "KNJF307A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf307a":
                    $sessionInstance->knjf307aModel();
                    $this->callView("knjf307aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf307aForm1");
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
$knjf307aCtl = new knjf307aController;
?>
