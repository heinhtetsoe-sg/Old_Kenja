<?php

require_once('for_php7.php');

require_once('knjd187lModel.inc');
require_once('knjd187lQuery.inc');

class knjd187lController extends Controller {
    var $ModelClassName = "knjd187lModel";
    var $ProgramID      = "KNJD187L";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd187lModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187lForm1");
                    exit;
                case "knjd187l":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd187lModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187lForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd187lForm1");
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
$knjd187lCtl = new knjd187lController;
//var_dump($_REQUEST);
?>
