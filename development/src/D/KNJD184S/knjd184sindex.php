<?php

require_once('for_php7.php');

require_once('knjd184sModel.inc');
require_once('knjd184sQuery.inc');

class knjd184sController extends Controller {
    var $ModelClassName = "knjd184sModel";
    var $ProgramID      = "KNJD184S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd184sModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd184sForm1");
                    exit;
                case "knjd184s":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd184sModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd184sForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd184sForm1");
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
$knjd184sCtl = new knjd184sController;
//var_dump($_REQUEST);
?>
