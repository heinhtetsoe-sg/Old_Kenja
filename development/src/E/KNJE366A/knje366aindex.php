<?php

require_once('for_php7.php');

require_once('knje366aModel.inc');
require_once('knje366aQuery.inc');

class knje366aController extends Controller {
    var $ModelClassName = "knje366aModel";
    var $ProgramID      = "KNJE366A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje366a":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje366aModel();       //コントロールマスタの呼び出し
                    $this->callView("knje366aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje366aForm1");
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
$knje366aCtl = new knje366aController;
?>
