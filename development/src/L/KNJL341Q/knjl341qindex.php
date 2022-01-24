<?php

require_once('for_php7.php');

require_once('knjl341qModel.inc');
require_once('knjl341qQuery.inc');

class knjl341qController extends Controller {
    var $ModelClassName = "knjl341qModel";
    var $ProgramID      = "KNJL341Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl341q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl341qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl341qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl341qCtl = new knjl341qController;
?>
