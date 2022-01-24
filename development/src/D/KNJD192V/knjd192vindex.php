<?php

require_once('for_php7.php');

require_once('knjd192vModel.inc');
require_once('knjd192vQuery.inc');

class knjd192vController extends Controller {
    var $ModelClassName = "knjd192vModel";
    var $ProgramID      = "KNJD192V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd192vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192vForm1");
                    exit;
                case "change_grade":
                case "knjd192v":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd192vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192vForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192vForm1");
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
$knjd192vCtl = new knjd192vController;
//var_dump($_REQUEST);
?>
