<?php

require_once('for_php7.php');

require_once('knjl321uModel.inc');
require_once('knjl321uQuery.inc');

class knjl321uController extends Controller {
    var $ModelClassName = "knjl321uModel";
    var $ProgramID      = "KNJL321U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl321u":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl321uModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl321uForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl321uCtl = new knjl321uController;
?>
