<?php

require_once('for_php7.php');

require_once('knje365Model.inc');
require_once('knje365Query.inc');

class knje365Controller extends Controller {
    var $ModelClassName = "knje365Model";
    var $ProgramID      = "KNJE365";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje365":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje365Model();       //コントロールマスタの呼び出し
                    $this->callView("knje365Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje365Form1");
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
$knje365Ctl = new knje365Controller;
?>
