<?php

require_once('for_php7.php');

require_once('knje153Model.inc');
require_once('knje153Query.inc');

class knje153Controller extends Controller {
    var $ModelClassName = "knje153Model";
    var $ProgramID      = "KNJE153";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje153":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje153Model();       //コントロールマスタの呼び出し
                    $this->callView("knje153Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje153Form1");
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
$knje153Ctl = new knje153Controller;
?>
