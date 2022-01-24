<?php

require_once('for_php7.php');

require_once('knjl304qModel.inc');
require_once('knjl304qQuery.inc');

class knjl304qController extends Controller {
    var $ModelClassName = "knjl304qModel";
    var $ProgramID      = "KNJL304Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl304q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl304qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl304qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl304qCtl = new knjl304qController;
?>
