<?php

require_once('for_php7.php');

require_once('knjd192jModel.inc');
require_once('knjd192jQuery.inc');

class knjd192jController extends Controller {
    var $ModelClassName = "knjd192jModel";
    var $ProgramID      = "KNJD192J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd192jModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192jForm1");
                    exit;
                case "change_grade":
                case "knjd192j":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd192jModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192jForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192jForm1");
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
$knjd192jCtl = new knjd192jController;
//var_dump($_REQUEST);
?>
