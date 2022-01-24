<?php

require_once('for_php7.php');

require_once('knje150Model.inc');
require_once('knje150Query.inc');

class knje150Controller extends Controller {
    var $ModelClassName = "knje150Model";
    var $ProgramID      = "KNJE150";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje150":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje150Model();        //コントロールマスタの呼び出し
                    $this->callView("knje150Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje150Form1");
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
$knje150Ctl = new knje150Controller;
//var_dump($_REQUEST);
?>
