<?php

require_once('for_php7.php');

require_once('knje443aModel.inc');
require_once('knje443aQuery.inc');

class knje443aController extends Controller {
    var $ModelClassName = "knje443aModel";
    var $ProgramID      = "KNJE443A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje443a":
                    $sessionInstance->knje443aModel();
                    $this->callView("knje443aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje443aForm1");
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
$knje443aCtl = new knje443aController;
?>
