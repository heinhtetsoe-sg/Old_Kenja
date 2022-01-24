<?php

require_once('for_php7.php');

require_once('knjl076fModel.inc');
require_once('knjl076fQuery.inc');

class knjl076fController extends Controller {
    var $ModelClassName = "knjl076fModel";
    var $ProgramID      = "KNJL076F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl076f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl076fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl076fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl076fForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl076fCtl = new knjl076fController;
?>
