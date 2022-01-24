<?php

require_once('for_php7.php');

require_once('knjh566aModel.inc');
require_once('knjh566aQuery.inc');

class knjh566aController extends Controller {
    var $ModelClassName = "knjh566aModel";
    var $ProgramID      = "KNJH566A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh566a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh566aModel();        //コントロールマスタの呼び出し
                    $this->callView("knjh566aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh566aCtl = new knjh566aController;
?>
