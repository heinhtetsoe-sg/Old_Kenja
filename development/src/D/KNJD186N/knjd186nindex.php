<?php

require_once('for_php7.php');

require_once('knjd186nModel.inc');
require_once('knjd186nQuery.inc');

class knjd186nController extends Controller {
    var $ModelClassName = "knjd186nModel";
    var $ProgramID      = "KNJD186N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd186nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd186nForm1");
                    exit;
                case "knjd186n":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd186nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd186nForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd186nForm1");
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
$knjd186nCtl = new knjd186nController;
//var_dump($_REQUEST);
?>
