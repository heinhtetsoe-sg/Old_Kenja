<?php

require_once('for_php7.php');

require_once('knjp740Model.inc');
require_once('knjp740Query.inc');

class knjp740Controller extends Controller {
    var $ModelClassName = "knjp740Model";
    var $ProgramID      = "KNJP740";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp740":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp740Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp740Form1");
                    exit;
                case "print":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    $sessionInstance->getDownloadModel();
                    $this->callView("knjp740Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjp740Form1");
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
$knjp740Ctl = new knjp740Controller;
?>
