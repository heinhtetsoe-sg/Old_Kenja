<?php

require_once('for_php7.php');

require_once('knjf111aModel.inc');
require_once('knjf111aQuery.inc');

class knjf111aController extends Controller {
    var $ModelClassName = "knjf111aModel";
    var $ProgramID      = "KNJF111A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "year":
                case "knjf111a":                        //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf111aModel();  //コントロールマスタの呼び出し
                    $this->callView("knjf111aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf111aForm1");
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
$knjf111aCtl = new knjf111aController;
//var_dump($_REQUEST);
?>
