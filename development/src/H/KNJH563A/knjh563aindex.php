<?php

require_once('for_php7.php');

require_once('knjh563aModel.inc');
require_once('knjh563aQuery.inc');

class knjh563aController extends Controller {
    var $ModelClassName = "knjh563aModel";
    var $ProgramID      = "KNJH563A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh563a":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh563aModel();      //コントロールマスタの呼び出し
                    $this->callView("knjh563aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh563aCtl = new knjh563aController;
?>
