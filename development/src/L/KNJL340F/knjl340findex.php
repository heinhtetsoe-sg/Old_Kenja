<?php

require_once('for_php7.php');

require_once('knjl340fModel.inc');
require_once('knjl340fQuery.inc');

class knjl340fController extends Controller {
    var $ModelClassName = "knjl340fModel";
    var $ProgramID      = "KNJL340F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl340f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl340fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl340fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl340fCtl = new knjl340fController;
?>
