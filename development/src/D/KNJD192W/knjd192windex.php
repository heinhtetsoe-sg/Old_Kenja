<?php

require_once('for_php7.php');

require_once('knjd192wModel.inc');
require_once('knjd192wQuery.inc');

class knjd192wController extends Controller {
    var $ModelClassName = "knjd192wModel";
    var $ProgramID      = "KNJD192W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd192wModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192wForm1");
                    exit;
                case "change_grade":
                case "knjd192w":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd192wModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192wForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192wForm1");
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
$knjd192wCtl = new knjd192wController;
//var_dump($_REQUEST);
?>
