<?php

require_once('for_php7.php');

require_once('knjc166fModel.inc');
require_once('knjc166fQuery.inc');

class knjc166fController extends Controller {
    var $ModelClassName = "knjc166fModel";
    var $ProgramID      = "KNJC166F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc166f":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc166fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjc166fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc166fCtl = new knjc166fController;
?>
