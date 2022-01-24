<?php

require_once('for_php7.php');

require_once('knja611vModel.inc');
require_once('knja611vQuery.inc');

class knja611vController extends Controller {
    var $ModelClassName = "knja611vModel";
    var $ProgramID      = "KNJA611V";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "init":
                case "knja611v":
                    $sessionInstance->knja611vModel();
                    $this->callView("knja611vForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja611vForm1");
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
$knja611vCtl = new knja611vController;
?>
