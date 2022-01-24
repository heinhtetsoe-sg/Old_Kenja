<?php

require_once('for_php7.php');

require_once('knja131Model.inc');
require_once('knja131Query.inc');

class knja131Controller extends Controller {
    var $ModelClassName = "knja131Model";
    var $ProgramID      = "KNJA131";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja131":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja131Model();       //コントロールマスタの呼び出し
                    $this->callView("knja131Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja131Form1");
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
$knja131Ctl = new knja131Controller;
//var_dump($_REQUEST);
?>
