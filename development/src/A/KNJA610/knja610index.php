<?php

require_once('for_php7.php');

require_once('knja610Model.inc');
require_once('knja610Query.inc');

class knja610Controller extends Controller {
    var $ModelClassName = "knja610Model";
    var $ProgramID      = "KNJA610";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "init":
                case "knja610":
                    $sessionInstance->knja610Model();
                    $this->callView("knja610Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja610Form1");
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
$knja610Ctl = new knja610Controller;
//var_dump($_REQUEST);
?>
