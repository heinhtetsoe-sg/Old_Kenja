<?php

require_once('for_php7.php');

require_once('knjb3048Model.inc');
require_once('knjb3048Query.inc');

class knjb3048Controller extends Controller {
    var $ModelClassName = "knjb3048Model";
    var $ProgramID      = "KNJB3048";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb3048":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb3048Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb3048Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjb3048Form1");
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
$knjb3048Ctl = new knjb3048Controller;
?>
