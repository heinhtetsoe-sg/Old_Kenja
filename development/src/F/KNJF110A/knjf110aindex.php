<?php

require_once('for_php7.php');

require_once('knjf110aModel.inc');
require_once('knjf110aQuery.inc');

class knjf110aController extends Controller {
    var $ModelClassName = "knjf110aModel";
    var $ProgramID      = "KNJF110A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf110a":                        //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf110aModel();  //コントロールマスタの呼び出し
                    $this->callView("knjf110aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf110aForm1");
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
$knjf110aCtl = new knjf110aController;
//var_dump($_REQUEST);
?>
