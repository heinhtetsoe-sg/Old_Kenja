<?php

require_once('for_php7.php');

require_once('knjd619qModel.inc');
require_once('knjd619qQuery.inc');

class knjd619qController extends Controller {
    var $ModelClassName = "knjd619qModel";
    var $ProgramID      = "KNJD619Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd619qModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd619qForm1");
                    exit;
                case "knjd619q":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd619qModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd619qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd619qCtl = new knjd619qController;
//var_dump($_REQUEST);
?>
