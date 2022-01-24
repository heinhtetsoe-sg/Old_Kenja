<?php

require_once('for_php7.php');

require_once('knje370iModel.inc');
require_once('knje370iQuery.inc');

class knje370iController extends Controller {
    var $ModelClassName = "knje370iModel";
    var $ProgramID      = "KNJE370I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje370i":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje370iModel();       //コントロールマスタの呼び出し
                    $this->callView("knje370iForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje370iForm1");
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
$knje370iCtl = new knje370iController;
?>
