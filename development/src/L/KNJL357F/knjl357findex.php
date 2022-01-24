<?php

require_once('for_php7.php');

require_once('knjl357fModel.inc');
require_once('knjl357fQuery.inc');

class knjl357fController extends Controller {
    var $ModelClassName = "knjl357fModel";
    var $ProgramID      = "KNJL357F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl357f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl357fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl357fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl357fCtl = new knjl357fController;
?>
