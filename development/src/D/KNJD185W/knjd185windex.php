<?php

require_once('for_php7.php');

require_once('knjd185wModel.inc');
require_once('knjd185wQuery.inc');

class knjd185wController extends Controller {
    var $ModelClassName = "knjd185wModel";
    var $ProgramID      = "KNJD185W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd185w":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd185wModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd185wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd185wCtl = new knjd185wController;
?>
