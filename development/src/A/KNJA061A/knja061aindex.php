<?php

require_once('for_php7.php');

require_once('knja061aModel.inc');
require_once('knja061aQuery.inc');

class knja061aController extends Controller {
    var $ModelClassName = "knja061aModel";
    var $ProgramID      = "KNJA061A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knja061aModel();        //コントロールマスタの呼び出し
                    $this->callView("knja061aForm1");
                    exit;
                case "knja061a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja061aModel();        //コントロールマスタの呼び出し
                    $this->callView("knja061aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja061aForm1");
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
$knja061aCtl = new knja061aController;
//var_dump($_REQUEST);
?>
