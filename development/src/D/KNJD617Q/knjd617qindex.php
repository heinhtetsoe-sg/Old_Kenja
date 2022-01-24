<?php

require_once('for_php7.php');

require_once('knjd617qModel.inc');
require_once('knjd617qQuery.inc');

class knjd617qController extends Controller {
    var $ModelClassName = "knjd617qModel";
    var $ProgramID      = "KNJD617Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd617qModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd617qForm1");
                    exit;
                case "knjd617q":                                //メニュー画面もしくはSUBMITした場合
                case "changeSeme":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd617qModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd617qForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd617qForm1");
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
$knjd617qCtl = new knjd617qController;
//var_dump($_REQUEST);
?>
