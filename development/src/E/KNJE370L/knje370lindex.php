<?php

require_once('for_php7.php');

require_once('knje370lModel.inc');
require_once('knje370lQuery.inc');

class knje370lController extends Controller {
    var $ModelClassName = "knje370lModel";
    var $ProgramID      = "KNJE370L";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knje370lModel();        //コントロールマスタの呼び出し
                    $this->callView("knje370lForm1");
                    exit;
                case "change_grade":
                case "knje370l":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knje370lModel();        //コントロールマスタの呼び出し
                    $this->callView("knje370lForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knje370lCtl = new knje370lController;
//var_dump($_REQUEST);
?>
