<?php

require_once('for_php7.php');

require_once('knjd232vModel.inc');
require_once('knjd232vQuery.inc');

class knjd232vController extends Controller {
    var $ModelClassName = "knjd232vModel";
    var $ProgramID      = "KNJD232V";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki_change":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd232vModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd232vForm1");
                    exit;
                case "knjd232v":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd232vModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd232vForm1");
                    exit;
                case "csv1":     //CSVダウンロード (成績優良者)
                    if (!$sessionInstance->getDownloadModel1()){
                        $this->callView("knjd232vForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID); 
                    break 2;
                case "csv2":     //CSVダウンロード (成績不振者)
                case "csv2_1":   //CSVダウンロード (成績不振者、特別活動)
                case "csv3":     //CSVダウンロード (成績不振者、過去の不認定科目)
                    if (!$sessionInstance->getDownloadModel2()){
                        $this->callView("knjd232vForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID); 
                    break 2;
                case "csv4":     //CSVダウンロード (出欠状況)
                    if (!$sessionInstance->getDownloadModel4()){
                        $this->callView("knjd232vForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID); 
                    break 2;
                case "csv5":     //CSVダウンロード (皆勤者)
                    if (!$sessionInstance->getDownloadModel5()){
                        $this->callView("knjd232vForm1");
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
$knjd232vCtl = new knjd232vController;
?>
