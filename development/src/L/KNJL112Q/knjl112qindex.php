<?php

require_once('for_php7.php');

require_once('knjl112qModel.inc');
require_once('knjl112qQuery.inc');

class knjl112qController extends Controller {
    var $ModelClassName = "knjl112qModel";
    var $ProgramID      = "KNJL112Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl112q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl112qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl112qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl112qCtl = new knjl112qController;
?>
