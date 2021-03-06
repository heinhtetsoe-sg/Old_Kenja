<?php

require_once('for_php7.php');

require_once('knjc166iModel.inc');
require_once('knjc166iQuery.inc');

class knjc166iController extends Controller {
    var $ModelClassName = "knjc166iModel";
    var $ProgramID      = "KNJC166I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc166i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc166iModel();      //コントロールマスタの呼び出し
                    $this->callView("knjc166iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc166iCtl = new knjc166iController;
?>
