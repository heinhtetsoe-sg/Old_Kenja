<?php

require_once('for_php7.php');

require_once('knjl324pModel.inc');
require_once('knjl324pQuery.inc');

class knjl324pController extends Controller {
    var $ModelClassName = "knjl324pModel";
    var $ProgramID      = "KNJL324P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl324p":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl324pModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl324pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl324pCtl = new knjl324pController;
?>
