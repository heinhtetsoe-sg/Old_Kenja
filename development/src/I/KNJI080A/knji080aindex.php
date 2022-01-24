<?php

require_once('for_php7.php');

require_once('knji080aModel.inc');
require_once('knji080aQuery.inc');

class knji080aController extends Controller {
    var $ModelClassName = "knji080aModel";
    var $ProgramID      = "KNJI080A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knji080a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knji080aModel();      //コントロールマスタの呼び出し
                    $this->callView("knji080aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knji080a");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knji080aCtl = new knji080aController;
var_dump($_REQUEST);
?>
