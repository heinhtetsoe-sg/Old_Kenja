<?php

require_once('for_php7.php');

require_once('knjd625gModel.inc');
require_once('knjd625gQuery.inc');

class knjd625gController extends Controller {
    var $ModelClassName = "knjd625gModel";
    var $ProgramID      = "KNJD625G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd625gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd625gForm1");
                    exit;
                case "knjd625g":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd625gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd625gForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd625gForm1");
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
$knjd625gCtl = new knjd625gController;
//var_dump($_REQUEST);
?>
