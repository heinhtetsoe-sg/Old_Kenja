<?php

require_once('for_php7.php');

require_once('knjd656bModel.inc');
require_once('knjd656bQuery.inc');

class knjd656bController extends Controller {
    var $ModelClassName = "knjd656bModel";
    var $ProgramID      = "KNJD656B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd656b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd656bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd656bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd656bForm1");
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
$knjd656bCtl = new knjd656bController;
//var_dump($_REQUEST);
?>
