<?php

require_once('for_php7.php');

require_once('knjd185nModel.inc');
require_once('knjd185nQuery.inc');

class knjd185nController extends Controller {
    var $ModelClassName = "knjd185nModel";
    var $ProgramID      = "KNJD185N";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd185n":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd185nModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd185nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd185nCtl = new knjd185nController;
?>
