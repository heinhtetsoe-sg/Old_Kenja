<?php

require_once('for_php7.php');

require_once('knjd615qModel.inc');
require_once('knjd615qQuery.inc');

class knjd615qController extends Controller {
    var $ModelClassName = "knjd615qModel";
    var $ProgramID      = "KNJD615Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd615qModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615qForm1");
                    exit;
                case "knjd615q":                                //メニュー画面もしくはSUBMITした場合
                case "changeSeme":
                case "changeGrade":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd615qModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615qForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getCsvModel()){
                        $this->callView("knjd615qForm1");
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
$knjd615qCtl = new knjd615qController;
//var_dump($_REQUEST);
?>
