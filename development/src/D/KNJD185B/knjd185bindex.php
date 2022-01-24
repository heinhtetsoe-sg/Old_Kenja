<?php

require_once('for_php7.php');

require_once('knjd185bModel.inc');
require_once('knjd185bQuery.inc');

class knjd185bController extends Controller {
    var $ModelClassName = "knjd185bModel";
    var $ProgramID      = "KNJD185B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd185bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185bForm1");
                    exit;
                case "knjd185b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd185bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd185bForm1");
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
$knjd185bCtl = new knjd185bController;
//var_dump($_REQUEST);
?>
