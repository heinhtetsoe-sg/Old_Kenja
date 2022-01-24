<?php

require_once('for_php7.php');

require_once('knjl342fModel.inc');
require_once('knjl342fQuery.inc');

class knjl342fController extends Controller {
    var $ModelClassName = "knjl342fModel";
    var $ProgramID      = "KNJL342F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl342f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl342fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl342fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl342fCtl = new knjl342fController;
?>
