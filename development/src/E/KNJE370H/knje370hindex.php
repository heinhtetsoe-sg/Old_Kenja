<?php

require_once('for_php7.php');

require_once('knje370hModel.inc');
require_once('knje370hQuery.inc');

class knje370hController extends Controller {
    var $ModelClassName = "knje370hModel";
    var $ProgramID      = "KNJE370H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje370h":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje370hModel();       //コントロールマスタの呼び出し
                    $this->callView("knje370hForm1");
                    exit;
                // case "csv":     //CSVダウンロード
                //     $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                //     if (!$sessionInstance->getDownloadModel()) {
                //         $this->callView("knje370hForm1");
                //     }
                //     break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje370hCtl = new knje370hController;
?>
