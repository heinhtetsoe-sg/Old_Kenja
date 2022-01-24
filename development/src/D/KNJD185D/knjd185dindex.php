<?php

require_once('for_php7.php');

require_once('knjd185dModel.inc');
require_once('knjd185dQuery.inc');

class knjd185dController extends Controller {
    var $ModelClassName = "knjd185dModel";
    var $ProgramID      = "KNJD185D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd185d":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd185dModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd185dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd185dCtl = new knjd185dController;
?>
