<?php

require_once('for_php7.php');

require_once('knjf072Model.inc');
require_once('knjf072Query.inc');

class knjf072Controller extends Controller {
    var $ModelClassName = "knjf072Model";
    var $ProgramID      = "KNJF072";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf072":                        //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf072Model();  //コントロールマスタの呼び出し
                    $this->callView("knjf072Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf072Form1");
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
$knjf072Ctl = new knjf072Controller;
//var_dump($_REQUEST);
?>
