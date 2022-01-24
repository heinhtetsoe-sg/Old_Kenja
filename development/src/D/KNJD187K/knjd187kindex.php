<?php

require_once('for_php7.php');

require_once('knjd187kModel.inc');
require_once('knjd187kQuery.inc');

class knjd187kController extends Controller {
    var $ModelClassName = "knjd187kModel";
    var $ProgramID      = "KNJD187K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd187kModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187kForm1");
                    exit;
                case "knjd187k":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd187kModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187kForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd187kForm1");
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
$knjd187kCtl = new knjd187kController;
//var_dump($_REQUEST);
?>
