<?php

require_once('for_php7.php');

require_once('knjd232xModel.inc');
require_once('knjd232xQuery.inc');

class knjd232xController extends Controller {
    var $ModelClassName = "knjd232xModel";
    var $ProgramID      = "KNJD232X";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd232x":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd232xModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd232xForm1");
                    exit;
                case "csv1":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel1()){
                        $this->callView("knjd232xForm1");
                    }
                    break 2;
                case "csv2":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel2()){
                        $this->callView("knjd232xForm1");
                    }
                    break 2;
                case "csv3":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel3()){
                        $this->callView("knjd232xForm1");
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
$knjd232xCtl = new knjd232xController;
?>
