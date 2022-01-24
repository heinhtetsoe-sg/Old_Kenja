<?php

require_once('for_php7.php');

require_once('knjd105fModel.inc');
require_once('knjd105fQuery.inc');

class knjd105fController extends Controller {
    var $ModelClassName = "knjd105fModel";
    var $ProgramID      = "KNJD105F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd105f":                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd105fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd105fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd105fCtl = new knjd105fController;
?>
