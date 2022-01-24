<?php

require_once('for_php7.php');

require_once('knjd156dModel.inc');
require_once('knjd156dQuery.inc');

class knjd156dController extends Controller {
    var $ModelClassName = "knjd156dModel";
    var $ProgramID      = "KNJD156D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd156dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd156dForm1");
                    exit;
                case "knjd156d":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd156dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd156dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd156dForm1");
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
$knjd156dCtl = new knjd156dController;
//var_dump($_REQUEST);
?>
