<?php

require_once('for_php7.php');

require_once('knjl303qModel.inc');
require_once('knjl303qQuery.inc');

class knjl303qController extends Controller {
    var $ModelClassName = "knjl303qModel";
    var $ProgramID      = "KNJL303Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl303q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl303qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl303qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl303qCtl = new knjl303qController;
?>
