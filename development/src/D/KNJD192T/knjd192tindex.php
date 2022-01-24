<?php

require_once('for_php7.php');

require_once('knjd192tModel.inc');
require_once('knjd192tQuery.inc');

class knjd192tController extends Controller {
    var $ModelClassName = "knjd192tModel";
    var $ProgramID      = "KNJD192T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd192tModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192tForm1");
                    exit;
                case "change_grade":
                case "knjd192t":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd192tModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192tForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192tForm1");
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
$knjd192tCtl = new knjd192tController;
//var_dump($_REQUEST);
?>
