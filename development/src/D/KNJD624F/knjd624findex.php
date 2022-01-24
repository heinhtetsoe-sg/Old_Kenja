<?php

require_once('for_php7.php');

require_once('knjd624fModel.inc');
require_once('knjd624fQuery.inc');

class knjd624fController extends Controller {
    var $ModelClassName = "knjd624fModel";
    var $ProgramID      = "KNJD624F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd624fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd624fForm1");
                    exit;
                case "change_grade":
                case "knjd624f":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd624fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd624fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd624fForm1");
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
$knjd624fCtl = new knjd624fController;
//var_dump($_REQUEST);
?>
