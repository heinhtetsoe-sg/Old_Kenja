<?php

require_once('for_php7.php');

require_once('knjd184dModel.inc');
require_once('knjd184dQuery.inc');

class knjd184dController extends Controller {
    var $ModelClassName = "knjd184dModel";
    var $ProgramID      = "KNJD184D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd184dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd184dForm1");
                    exit;
                case "knjd184d":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd184dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd184dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd184dForm1");
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
$knjd184dCtl = new knjd184dController;
//var_dump($_REQUEST);
?>
