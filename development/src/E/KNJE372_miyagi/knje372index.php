<?php

require_once('for_php7.php');

require_once('knje372Model.inc');
require_once('knje372Query.inc');

class knje372Controller extends Controller {
    var $ModelClassName = "knje372Model";
    var $ProgramID      = "KNJE372";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje372":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje372Model();        //コントロールマスタの呼び出し
                    $this->callView("knje372Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje372Form1");
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
$knje372Ctl = new knje372Controller;
//var_dump($_REQUEST);
?>
