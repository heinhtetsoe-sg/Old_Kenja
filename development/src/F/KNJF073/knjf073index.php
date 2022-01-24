<?php

require_once('for_php7.php');

require_once('knjf073Model.inc');
require_once('knjf073Query.inc');

class knjf073Controller extends Controller {
    var $ModelClassName = "knjf073Model";
    var $ProgramID      = "KNJF073";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf073":                        //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf073Model();  //コントロールマスタの呼び出し
                    $this->callView("knjf073Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf073Form1");
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
$knjf073Ctl = new knjf073Controller;
//var_dump($_REQUEST);
?>
