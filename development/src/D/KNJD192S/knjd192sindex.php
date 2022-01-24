<?php

require_once('for_php7.php');

require_once('knjd192sModel.inc');
require_once('knjd192sQuery.inc');

class knjd192sController extends Controller {
    var $ModelClassName = "knjd192sModel";
    var $ProgramID      = "KNJD192S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd192sModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192sForm1");
                    exit;
                case "change_grade":
                case "knjd192s":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd192sModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192sForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192sForm1");
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
$knjd192sCtl = new knjd192sController;
//var_dump($_REQUEST);
?>
