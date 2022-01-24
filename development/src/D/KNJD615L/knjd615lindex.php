<?php

require_once('for_php7.php');

require_once('knjd615lModel.inc');
require_once('knjd615lQuery.inc');

class knjd615lController extends Controller {
    var $ModelClassName = "knjd615lModel";
    var $ProgramID      = "KNJD615L";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd615lModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615lForm1");
                    exit;
                case "knjd615l":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd615lModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615lForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd615lForm1");
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
$knjd615lCtl = new knjd615lController;
//var_dump($_REQUEST);
?>
