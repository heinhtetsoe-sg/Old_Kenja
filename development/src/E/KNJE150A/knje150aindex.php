<?php

require_once('for_php7.php');

require_once('knje150aModel.inc');
require_once('knje150aQuery.inc');

class knje150aController extends Controller {
    var $ModelClassName = "knje150aModel";
    var $ProgramID      = "KNJE150A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje150a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje150aModel();        //コントロールマスタの呼び出し
                    $this->callView("knje150aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje150aForm1");
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
$knje150aCtl = new knje150aController;
//var_dump($_REQUEST);
?>
