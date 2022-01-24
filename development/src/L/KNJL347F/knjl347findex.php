<?php

require_once('for_php7.php');

require_once('knjl347fModel.inc');
require_once('knjl347fQuery.inc');

class knjl347fController extends Controller {
    var $ModelClassName = "knjl347fModel";
    var $ProgramID      = "KNJL347F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl347f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl347fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl347fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl347fCtl = new knjl347fController;
?>
