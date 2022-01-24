<?php

require_once('for_php7.php');

require_once('knjd620qModel.inc');
require_once('knjd620qQuery.inc');

class knjd620qController extends Controller {
    var $ModelClassName = "knjd620qModel";
    var $ProgramID      = "KNJD620Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd620qModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd620qForm1");
                    exit;
                case "knjd620q":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd620qModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd620qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd620qCtl = new knjd620qController;
//var_dump($_REQUEST);
?>
