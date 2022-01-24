<?php

require_once('for_php7.php');

require_once('knjl346fModel.inc');
require_once('knjl346fQuery.inc');

class knjl346fController extends Controller {
    var $ModelClassName = "knjl346fModel";
    var $ProgramID      = "KNJL346F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl346f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl346fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl346fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl346fCtl = new knjl346fController;
?>
