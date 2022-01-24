<?php

require_once('for_php7.php');

require_once('knje381Model.inc');
require_once('knje381Query.inc');

class knje381Controller extends Controller {
    var $ModelClassName = "knje381Model";
    var $ProgramID      = "KNJE381";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje381":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje381Model();       //コントロールマスタの呼び出し
                    $this->callView("knje381Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje381Form1");
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
$knje381Ctl = new knje381Controller;
?>
