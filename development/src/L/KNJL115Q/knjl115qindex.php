<?php

require_once('for_php7.php');

require_once('knjl115qModel.inc');
require_once('knjl115qQuery.inc');

class knjl115qController extends Controller {
    var $ModelClassName = "knjl115qModel";
    var $ProgramID      = "KNJL115Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl115q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl115qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl115qForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl115qForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl115qCtl = new knjl115qController;
?>
