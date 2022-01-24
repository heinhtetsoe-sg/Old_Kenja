<?php

require_once('for_php7.php');

require_once('knjl316pModel.inc');
require_once('knjl316pQuery.inc');

class knjl316pController extends Controller {
    var $ModelClassName = "knjl316pModel";
    var $ProgramID      = "KNJL316P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl316p":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl316pModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl316pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl316pCtl = new knjl316pController;
?>
