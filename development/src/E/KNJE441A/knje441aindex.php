<?php

require_once('for_php7.php');

require_once('knje441aModel.inc');
require_once('knje441aQuery.inc');

class knje441aController extends Controller {
    var $ModelClassName = "knje441aModel";
    var $ProgramID      = "KNJE441A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje441a":
                    $sessionInstance->knje441aModel();
                    $this->callView("knje441aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje441aForm1");
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
$knje441aCtl = new knje441aController;
?>
