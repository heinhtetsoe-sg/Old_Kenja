<?php

require_once('for_php7.php');

require_once('knjc166gModel.inc');
require_once('knjc166gQuery.inc');

class knjc166gController extends Controller {
    var $ModelClassName = "knjc166gModel";
    var $ProgramID      = "KNJC166G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc166g":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc166gModel();      //コントロールマスタの呼び出し
                    $this->callView("knjc166gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc166gCtl = new knjc166gController;
?>
