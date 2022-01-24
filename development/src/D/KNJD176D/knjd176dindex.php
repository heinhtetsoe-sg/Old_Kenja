<?php

require_once('for_php7.php');

require_once('knjd176dModel.inc');
require_once('knjd176dQuery.inc');

class knjd176dController extends Controller {
    var $ModelClassName = "knjd176dModel";
    var $ProgramID      = "KNJD176D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd176d":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd176dModel();   //コントロールマスタの呼び出し
                    $this->callView("knjd176dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd176dForm1");
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
$knjd176dCtl = new knjd176dController;
//var_dump($_REQUEST);
?>
