<?php

require_once('for_php7.php');

require_once('knjl043qModel.inc');
require_once('knjl043qQuery.inc');

class knjl043qController extends Controller {
    var $ModelClassName = "knjl043qModel";
    var $ProgramID      = "KNJL043Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl043q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl043qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl043qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl043qCtl = new knjl043qController;
?>
