<?php

require_once('for_php7.php');

require_once('knjh080fModel.inc');
require_once('knjh080fQuery.inc');

class knjh080fController extends Controller {
    var $ModelClassName = "knjh080fModel";
    var $ProgramID      = "KNJH080F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh080f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh080fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjh080fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh080fCtl = new knjh080fController;
?>
