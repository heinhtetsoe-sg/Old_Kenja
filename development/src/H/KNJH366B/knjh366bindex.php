<?php

require_once('for_php7.php');

require_once('knjh366bModel.inc');
require_once('knjh366bQuery.inc');

class knjh366bController extends Controller {
    var $ModelClassName = "knjh366bModel";
    var $ProgramID      = "KNJH366B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh366b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh366bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjh366bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh366bCtl = new knjh366bController;
?>
