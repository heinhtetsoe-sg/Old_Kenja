<?php

require_once('for_php7.php');

require_once('knjh566bModel.inc');
require_once('knjh566bQuery.inc');

class knjh566bController extends Controller {
    var $ModelClassName = "knjh566bModel";
    var $ProgramID      = "KNJH566B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh566b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh566bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjh566bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh566bCtl = new knjh566bController;
?>
