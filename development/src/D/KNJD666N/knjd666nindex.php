<?php

require_once('for_php7.php');

require_once('knjd666nModel.inc');
require_once('knjd666nQuery.inc');

class knjd666nController extends Controller {
    var $ModelClassName = "knjd666nModel";
    var $ProgramID      = "KNJD666N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd666nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd666nForm1");
                    exit;
                case "knjd666n":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd666nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd666nForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd666nForm1");
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
$knjd666nCtl = new knjd666nController;
//var_dump($_REQUEST);
?>
