<?php

require_once('for_php7.php');

require_once('knjl342qModel.inc');
require_once('knjl342qQuery.inc');

class knjl342qController extends Controller {
    var $ModelClassName = "knjl342qModel";
    var $ProgramID      = "KNJL342Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl342q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl342qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl342qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl342qCtl = new knjl342qController;
?>
