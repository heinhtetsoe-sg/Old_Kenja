<?php

require_once('for_php7.php');

require_once('knjd187gModel.inc');
require_once('knjd187gQuery.inc');

class knjd187gController extends Controller {
    var $ModelClassName = "knjd187gModel";
    var $ProgramID      = "KNJD187G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd187gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187gForm1");
                    exit;
                case "knjd187g":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd187gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187gForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd187gForm1");
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
$knjd187gCtl = new knjd187gController;
//var_dump($_REQUEST);
?>
