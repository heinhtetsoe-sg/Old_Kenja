<?php

require_once('for_php7.php');

require_once('knjd187nModel.inc');
require_once('knjd187nQuery.inc');

class knjd187nController extends Controller {
    var $ModelClassName = "knjd187nModel";
    var $ProgramID      = "KNJD187N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd187nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187nForm1");
                    exit;
                case "knjd187n":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd187nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187nForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd187nForm1");
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
$knjd187nCtl = new knjd187nController;
//var_dump($_REQUEST);
?>
