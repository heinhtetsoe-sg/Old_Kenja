<?php

require_once('for_php7.php');

require_once('knje070aModel.inc');
require_once('knje070aQuery.inc');

class knje070aController extends Controller {
    var $ModelClassName = "knje070aModel";
    var $ProgramID      = "KNJE070A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje070a":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje070aModel();       //コントロールマスタの呼び出し
                    $this->callView("knje070aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje070aForm1");
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
$knje070aCtl = new knje070aController;
//var_dump($_REQUEST);
?>
