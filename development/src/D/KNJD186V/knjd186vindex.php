<?php

require_once('for_php7.php');

require_once('knjd186vModel.inc');
require_once('knjd186vQuery.inc');

class knjd186vController extends Controller {
    var $ModelClassName = "knjd186vModel";
    var $ProgramID      = "KNJD186V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd186vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd186vForm1");
                    exit;
                case "knjd186v":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd186vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd186vForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd186vForm1");
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
$knjd186vCtl = new knjd186vController;
//var_dump($_REQUEST);
?>
