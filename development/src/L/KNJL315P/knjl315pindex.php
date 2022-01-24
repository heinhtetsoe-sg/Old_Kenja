<?php

require_once('for_php7.php');

require_once('knjl315pModel.inc');
require_once('knjl315pQuery.inc');

class knjl315pController extends Controller {
    var $ModelClassName = "knjl315pModel";
    var $ProgramID      = "KNJL315P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl315p":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl315pModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl315pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl315pCtl = new knjl315pController;
?>
