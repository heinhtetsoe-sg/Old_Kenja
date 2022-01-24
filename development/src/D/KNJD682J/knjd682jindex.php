<?php

require_once('for_php7.php');

require_once('knjd682jModel.inc');
require_once('knjd682jQuery.inc');

class knjd682jController extends Controller {
    var $ModelClassName = "knjd682jModel";
    var $ProgramID      = "KNJD682J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd682j":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd682jModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd682jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd682jCtl = new knjd682jController;
?>
