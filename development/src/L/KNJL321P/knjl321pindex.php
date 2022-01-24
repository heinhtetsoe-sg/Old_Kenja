<?php

require_once('for_php7.php');

require_once('knjl321pModel.inc');
require_once('knjl321pQuery.inc');

class knjl321pController extends Controller {
    var $ModelClassName = "knjl321pModel";
    var $ProgramID      = "KNJL321P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl321p":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl321pModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl321pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl321pCtl = new knjl321pController;
?>
