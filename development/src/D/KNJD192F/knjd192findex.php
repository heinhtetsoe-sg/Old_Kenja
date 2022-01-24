<?php

require_once('for_php7.php');

require_once('knjd192fModel.inc');
require_once('knjd192fQuery.inc');

class knjd192fController extends Controller {
    var $ModelClassName = "knjd192fModel";
    var $ProgramID      = "KNJD192F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd192f":            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd192fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd192fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd192fCtl = new knjd192fController;
?>
