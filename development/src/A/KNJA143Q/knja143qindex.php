<?php

require_once('for_php7.php');

require_once('knja143qModel.inc');
require_once('knja143qQuery.inc');

class knja143qController extends Controller {
    var $ModelClassName = "knja143qModel";
    var $ProgramID      = "KNJA143Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja143q":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja143qModel();       //コントロールマスタの呼び出し
                    $this->callView("knja143qForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO002
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja143qModel();       //コントロールマスタの呼び出し
                    $this->callView("knja143qForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja143qForm1");
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
$knja143qCtl = new knja143qController;
//var_dump($_REQUEST);
?>
