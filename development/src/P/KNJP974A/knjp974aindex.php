<?php

require_once('for_php7.php');

require_once('knjp974aModel.inc');
require_once('knjp974aQuery.inc');

class knjp974aController extends Controller {
    var $ModelClassName = "knjp974aModel";
    var $ProgramID      = "KNJP974A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp974a":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp974aModel();       //コントロールマスタの呼び出し
                    $this->callView("knjp974aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp974aForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp974aCtl = new knjp974aController;
?>
