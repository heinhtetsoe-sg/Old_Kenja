<?php

require_once('for_php7.php');

require_once('knjd187cModel.inc');
require_once('knjd187cQuery.inc');

class knjd187cController extends Controller {
    var $ModelClassName = "knjd187cModel";
    var $ProgramID      = "KNJD187C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd187cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187cForm1");
                    exit;
                case "knjd187c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd187cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd187cForm1");
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
$knjd187cCtl = new knjd187cController;
//var_dump($_REQUEST);
?>
