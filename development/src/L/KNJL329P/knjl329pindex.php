<?php

require_once('for_php7.php');

require_once('knjl329pModel.inc');
require_once('knjl329pQuery.inc');

class knjl329pController extends Controller {
    var $ModelClassName = "knjl329pModel";
    var $ProgramID      = "KNJL329P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl329p":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl329pModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl329pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl329pCtl = new knjl329pController;
?>
