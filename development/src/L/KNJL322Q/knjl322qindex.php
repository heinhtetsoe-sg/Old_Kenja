<?php

require_once('for_php7.php');

require_once('knjl322qModel.inc');
require_once('knjl322qQuery.inc');

class knjl322qController extends Controller {
    var $ModelClassName = "knjl322qModel";
    var $ProgramID      = "KNJL322Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl322q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl322qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl322qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl322qCtl = new knjl322qController;
?>
