<?php

require_once('for_php7.php');

require_once('knjd616vModel.inc');
require_once('knjd616vQuery.inc');

class knjd616vController extends Controller {
    var $ModelClassName = "knjd616vModel";
    var $ProgramID      = "KNJD616V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd616vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd616vForm1");
                    exit;
                case "knjd616v":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd616vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd616vForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd616vForm1");
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
$knjd616vCtl = new knjd616vController;
//var_dump($_REQUEST);
?>
