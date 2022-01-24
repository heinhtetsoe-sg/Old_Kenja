<?php

require_once('for_php7.php');

require_once('knjd667vModel.inc');
require_once('knjd667vQuery.inc');

class knjd667vController extends Controller {
    var $ModelClassName = "knjd667vModel";
    var $ProgramID      = "KNJD667V";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd667vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd667vForm1");
                    exit;
                case "change_grade":
                case "knjd667v":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd667vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd667vForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd667vForm1");
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
$knjd667vCtl = new knjd667vController;
//var_dump($_REQUEST);
?>
