<?php

require_once('for_php7.php');

require_once('knjl301qModel.inc');
require_once('knjl301qQuery.inc');

class knjl301qController extends Controller {
    var $ModelClassName = "knjl301qModel";
    var $ProgramID      = "KNJL301Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl301qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl301qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl301qCtl = new knjl301qController;
?>
