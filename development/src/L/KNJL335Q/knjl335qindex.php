<?php

require_once('for_php7.php');

require_once('knjl335qModel.inc');
require_once('knjl335qQuery.inc');

class knjl335qController extends Controller {
    var $ModelClassName = "knjl335qModel";
    var $ProgramID      = "KNJL335Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl335q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl335qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl335qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl335qCtl = new knjl335qController;
?>
