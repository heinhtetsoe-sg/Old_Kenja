<?php

require_once('for_php7.php');

require_once('knjd185cModel.inc');
require_once('knjd185cQuery.inc');

class knjd185cController extends Controller {
    var $ModelClassName = "knjd185cModel";
    var $ProgramID      = "KNJD185C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd185cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185cForm1");
                    exit;
                case "knjd185c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd185cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd185cForm1");
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
$knjd185cCtl = new knjd185cController;
//var_dump($_REQUEST);
?>
