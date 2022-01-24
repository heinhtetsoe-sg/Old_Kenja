<?php

require_once('for_php7.php');

require_once('knjl345fModel.inc');
require_once('knjl345fQuery.inc');

class knjl345fController extends Controller {
    var $ModelClassName = "knjl345fModel";
    var $ProgramID      = "KNJL345F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl345f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl345fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl345fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl345fCtl = new knjl345fController;
?>
