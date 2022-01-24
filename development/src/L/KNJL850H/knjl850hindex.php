<?php
require_once('knjl850hModel.inc');
require_once('knjl850hQuery.inc');

class knjl850hController extends Controller {
    var $ModelClassName = "knjl850hModel";
    var $ProgramID      = "KNJL850H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl850hForm1");
                    break 2;
                case "knjl850h":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjl850iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl850hForm1");
                    exit;
                case "print":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCheckModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl850hCtl = new knjl850hController;
?>
