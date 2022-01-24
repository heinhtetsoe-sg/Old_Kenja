<?php

require_once('for_php7.php');

require_once('knje434aModel.inc');
require_once('knje434aQuery.inc');

class knje434aController extends Controller {
    var $ModelClassName = "knje434aModel";
    var $ProgramID      = "KNJE434A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje434a":
                    $sessionInstance->knje434aModel();
                    $this->callView("knje434aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje434aForm1");
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
$knje434aCtl = new knje434aController;
?>
