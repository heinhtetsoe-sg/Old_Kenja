<?php

require_once('for_php7.php');

require_once('knjd173jModel.inc');
require_once('knjd173jQuery.inc');

class knjd173jController extends Controller {
    var $ModelClassName = "knjd173jModel";
    var $ProgramID      = "KNJD173J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd173jModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd173jForm1");
                    exit;
                case "knjd173j":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd173jModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd173jForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd173jForm1");
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
$knjd173jCtl = new knjd173jController;
//var_dump($_REQUEST);
?>
