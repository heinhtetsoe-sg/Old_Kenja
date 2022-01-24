<?php

require_once('for_php7.php');

require_once('knjd618Model.inc');
require_once('knjd618Query.inc');

class knjd618Controller extends Controller {
    var $ModelClassName = "knjd618Model";
    var $ProgramID      = "KNJD618";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd618":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd618Model();        //コントロールマスタの呼び出し
                    $this->callView("knjd618Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd618Form1");
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
$knjd618Ctl = new knjd618Controller;
//var_dump($_REQUEST);
?>
