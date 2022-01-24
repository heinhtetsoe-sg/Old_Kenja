<?php

require_once('for_php7.php');

require_once('knjd232yModel.inc');
require_once('knjd232yQuery.inc');

class knjd232yController extends Controller {
    var $ModelClassName = "knjd232yModel";
    var $ProgramID      = "KNJD232Y";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki_change":
                case "knjd232y":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd232yModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd232yForm1");
                    exit;
                case "csv1":     //CSVダウンロード (成績優良者)
                    if (!$sessionInstance->getDownloadModel1()){
                        $this->callView("knjd232yForm1");
                    }
                    break 2;
                case "csv2":     //CSVダウンロード (成績不振者)
                case "csv2_1":   //CSVダウンロード (成績不振者、特別活動)
                case "csv3":     //CSVダウンロード (成績不振者、過去の不認定科目)
                    if (!$sessionInstance->getDownloadModel2()){
                        $this->callView("knjd232yForm1");
                    }
                    break 2;
                case "csv4":     //CSVダウンロード (出欠状況)
                    if (!$sessionInstance->getDownloadModel4()){
                        $this->callView("knjd232yForm1");
                    }
                    break 2;
                case "csv5":     //CSVダウンロード (皆勤者)
                    if (!$sessionInstance->getDownloadModel5()){
                        $this->callView("knjd232yForm1");
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
$knjd232yCtl = new knjd232yController;
?>
