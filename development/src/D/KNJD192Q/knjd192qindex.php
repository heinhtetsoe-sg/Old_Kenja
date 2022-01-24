<?php

require_once('for_php7.php');

require_once('knjd192qModel.inc');
require_once('knjd192qQuery.inc');

class knjd192qController extends Controller {
    var $ModelClassName = "knjd192qModel";
    var $ProgramID      = "KNJD192Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd192qModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192qForm1");
                    exit;
                case "change_grade":
                case "knjd192q":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd192qModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192qForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192qForm1");
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
$knjd192qCtl = new knjd192qController;
//var_dump($_REQUEST);
?>
