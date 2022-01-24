<?php

require_once('for_php7.php');

require_once('knjd185sModel.inc');
require_once('knjd185sQuery.inc');

class knjd185sController extends Controller {
    var $ModelClassName = "knjd185sModel";
    var $ProgramID      = "KNJD185S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd185sModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185sForm1");
                    exit;
                case "knjd185s":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd185sModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185sForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd185sForm1");
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
$knjd185sCtl = new knjd185sController;
//var_dump($_REQUEST);
?>
