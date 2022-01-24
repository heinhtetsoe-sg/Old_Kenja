<?php

require_once('for_php7.php');

require_once('knjd615nModel.inc');
require_once('knjd615nQuery.inc');

class knjd615nController extends Controller {
    var $ModelClassName = "knjd615nModel";
    var $ProgramID      = "KNJD615N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd615nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615nForm1");
                    exit;
                case "knjd615n":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd615nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615nForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd615nForm1");
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
$knjd615nCtl = new knjd615nController;
//var_dump($_REQUEST);
?>
