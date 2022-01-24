<?php

require_once('for_php7.php');

require_once('knjf074Model.inc');
require_once('knjf074Query.inc');

class knjf074Controller extends Controller {
    var $ModelClassName = "knjf074Model";
    var $ProgramID      = "KNJF074";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf074":                        //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf074Model();  //コントロールマスタの呼び出し
                    $this->callView("knjf074Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf074Form1");
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
$knjf074Ctl = new knjf074Controller;
//var_dump($_REQUEST);
?>
