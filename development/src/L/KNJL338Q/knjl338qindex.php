<?php

require_once('for_php7.php');

require_once('knjl338qModel.inc');
require_once('knjl338qQuery.inc');

class knjl338qController extends Controller {
    var $ModelClassName = "knjl338qModel";
    var $ProgramID      = "KNJL338Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl338q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl338qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl338qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl338qCtl = new knjl338qController;
?>
