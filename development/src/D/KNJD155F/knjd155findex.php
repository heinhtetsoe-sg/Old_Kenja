<?php

require_once('for_php7.php');

require_once('knjd155fModel.inc');
require_once('knjd155fQuery.inc');

class knjd155fController extends Controller {
    var $ModelClassName = "knjd155fModel";
    var $ProgramID      = "KNJD155F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd155fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd155fForm1");
                    exit;
                case "change_grade":
                case "knjd155f":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd155fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd155fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd155fForm1");
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
$knjd155fCtl = new knjd155fController;
//var_dump($_REQUEST);
?>
