<?php

require_once('for_php7.php');

require_once('knjf120aModel.inc');
require_once('knjf120aQuery.inc');

class knjf120aController extends Controller {
    var $ModelClassName = "knjf120aModel";
    var $ProgramID      = "KNJF120A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf120a":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf120aModel();   //コントロールマスタの呼び出し
                    $this->callView("knjf120aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf120aForm1");
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
$knjf120aCtl = new knjf120aController;
?>
