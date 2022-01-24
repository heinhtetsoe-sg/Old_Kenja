<?php

require_once('for_php7.php');

require_once('knjd192hModel.inc');
require_once('knjd192hQuery.inc');

class knjd192hController extends Controller {
    var $ModelClassName = "knjd192hModel";
    var $ProgramID      = "KNJD192H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd192hModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192hForm1");
                    exit;
                case "change_grade":
                case "knjd192h":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd192hModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192hForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192hForm1");
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
$knjd192hCtl = new knjd192hController;
//var_dump($_REQUEST);
?>
