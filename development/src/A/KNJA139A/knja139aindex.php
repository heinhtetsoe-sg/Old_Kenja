<?php

require_once('for_php7.php');

require_once('knja139aModel.inc');
require_once('knja139aQuery.inc');

class knja139aController extends Controller {
    var $ModelClassName = "knja139aModel";
    var $ProgramID      = "KNJA139A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja139a":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja139aModel();       //コントロールマスタの呼び出し
                    $this->callView("knja139aForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO002
                    $sessionInstance->knja139aModel();       //コントロールマスタの呼び出し
                    $this->callView("knja139aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja139aForm1");
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
$knja139aCtl = new knja139aController;
//var_dump($_REQUEST);
?>
