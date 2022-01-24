<?php

require_once('for_php7.php');

require_once('knjd429bModel.inc');
require_once('knjd429bQuery.inc');

class knjd429bController extends Controller {
    var $ModelClassName = "knjd429bModel";
    var $ProgramID      = "KNJD429B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd429bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd429bForm1");
                    exit;
                case "knjd429b":                                //メニュー画面もしくはSUBMITした場合
                case "changeHukusiki":
                case "changeSchoolKind":
                case "changeGhr":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd429bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd429bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd429bForm1");
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
$knjd429bCtl = new knjd429bController;
//var_dump($_REQUEST);
?>
