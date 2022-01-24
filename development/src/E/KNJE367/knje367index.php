<?php

require_once('for_php7.php');

require_once('knje367Model.inc');
require_once('knje367Query.inc');

class knje367Controller extends Controller {
    var $ModelClassName = "knje367Model";
    var $ProgramID      = "KNJE367";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje367":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje367Model();       //コントロールマスタの呼び出し
                    $this->callView("knje367Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje367Form1");
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
$knje367Ctl = new knje367Controller;
?>
