<?php

require_once('for_php7.php');

require_once('knjd186lModel.inc');
require_once('knjd186lQuery.inc');

class knjd186lController extends Controller {
    var $ModelClassName = "knjd186lModel";
    var $ProgramID      = "KNJD186L";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd186lModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd186lForm1");
                    exit;
                case "knjd186l":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd186lModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd186lForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd186lForm1");
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
$knjd186lCtl = new knjd186lController;
//var_dump($_REQUEST);
?>
