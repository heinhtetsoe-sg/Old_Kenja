<?php

require_once('for_php7.php');

require_once('knjl338fModel.inc');
require_once('knjl338fQuery.inc');

class knjl338fController extends Controller {
    var $ModelClassName = "knjl338fModel";
    var $ProgramID      = "KNJL338F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl338f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl338fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl338fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl338fCtl = new knjl338fController;
?>
