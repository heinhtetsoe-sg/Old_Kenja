<?php

require_once('for_php7.php');

require_once('knjd187dModel.inc');
require_once('knjd187dQuery.inc');

class knjd187dController extends Controller {
    var $ModelClassName = "knjd187dModel";
    var $ProgramID      = "KNJD187D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd187dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187dForm1");
                    exit;
                case "knjd187d":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd187dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd187dForm1");
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
$knjd187dCtl = new knjd187dController;
//var_dump($_REQUEST);
?>
