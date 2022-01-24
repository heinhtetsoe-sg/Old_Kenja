<?php

require_once('for_php7.php');

require_once('knjh080dModel.inc');
require_once('knjh080dQuery.inc');

class knjh080dController extends Controller {
    var $ModelClassName = "knjh080dModel";
    var $ProgramID      = "KNJH080D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh080d":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh080dModel();      //コントロールマスタの呼び出し
                    $this->callView("knjh080dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh080dCtl = new knjh080dController;
var_dump($_REQUEST);
?>
