<?php

require_once('for_php7.php');

require_once('knje366Model.inc');
require_once('knje366Query.inc');

class knje366Controller extends Controller {
    var $ModelClassName = "knje366Model";
    var $ProgramID      = "KNJE366";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje366":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje366Model();       //コントロールマスタの呼び出し
                    $this->callView("knje366Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje366Form1");
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
$knje366Ctl = new knje366Controller;
?>
