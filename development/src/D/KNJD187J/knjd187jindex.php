<?php

require_once('for_php7.php');

require_once('knjd187jModel.inc');
require_once('knjd187jQuery.inc');

class knjd187jController extends Controller {
    var $ModelClassName = "knjd187jModel";
    var $ProgramID      = "KNJD187J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd187jModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187jForm1");
                    exit;
                case "knjd187j":                                //メニュー画面もしくはSUBMITした場合
                case "chgSeme":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd187jModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187jForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd187jForm1");
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
$knjd187jCtl = new knjd187jController;
//var_dump($_REQUEST);
?>
