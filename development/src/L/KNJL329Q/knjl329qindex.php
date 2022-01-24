<?php

require_once('for_php7.php');

require_once('knjl329qModel.inc');
require_once('knjl329qQuery.inc');

class knjl329qController extends Controller {
    var $ModelClassName = "knjl329qModel";
    var $ProgramID      = "KNJL329Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl329q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl329qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl329qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl329qCtl = new knjl329qController;
?>
