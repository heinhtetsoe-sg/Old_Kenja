<?php

require_once('for_php7.php');

require_once('knjd192rModel.inc');
require_once('knjd192rQuery.inc');

class knjd192rController extends Controller {
    var $ModelClassName = "knjd192rModel";
    var $ProgramID      = "KNJD192R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd192rModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192rForm1");
                    exit;
                case "change_grade":
                case "knjd192r":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd192rModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192rForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192rForm1");
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
$knjd192rCtl = new knjd192rController;
//var_dump($_REQUEST);
?>
