<?php

require_once('for_php7.php');

require_once('knjd187eModel.inc');
require_once('knjd187eQuery.inc');

class knjd187eController extends Controller {
    var $ModelClassName = "knjd187eModel";
    var $ProgramID      = "KNJD187E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd187e":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd187eModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd187eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd187eCtl = new knjd187eController;
?>
