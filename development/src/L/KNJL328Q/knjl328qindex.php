<?php

require_once('for_php7.php');

require_once('knjl328qModel.inc');
require_once('knjl328qQuery.inc');

class knjl328qController extends Controller {
    var $ModelClassName = "knjl328qModel";
    var $ProgramID      = "KNJL328Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl328q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl328qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl328qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl328qCtl = new knjl328qController;
?>
