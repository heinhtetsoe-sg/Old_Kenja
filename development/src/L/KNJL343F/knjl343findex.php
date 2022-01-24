<?php

require_once('for_php7.php');

require_once('knjl343fModel.inc');
require_once('knjl343fQuery.inc');

class knjl343fController extends Controller {
    var $ModelClassName = "knjl343fModel";
    var $ProgramID      = "KNJL343F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl343f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl343fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl343fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl343fCtl = new knjl343fController;
?>
