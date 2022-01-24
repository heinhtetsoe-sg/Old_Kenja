<?php

require_once('for_php7.php');

require_once('knjd171eModel.inc');
require_once('knjd171eQuery.inc');

class knjd171eController extends Controller {
    var $ModelClassName = "knjd171eModel";
    var $ProgramID      = "KNJD171E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd171e":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd171eModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd171eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd171eCtl = new knjd171eController;
?>
