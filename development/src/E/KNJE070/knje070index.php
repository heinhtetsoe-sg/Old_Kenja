<?php

require_once('for_php7.php');

require_once('knje070Model.inc');
require_once('knje070Query.inc');

class knje070Controller extends Controller {
    var $ModelClassName = "knje070Model";
    var $ProgramID      = "KNJE070";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje070":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje070Model();       //コントロールマスタの呼び出し
                    $this->callView("knje070Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje070Form1");
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
$knje070Ctl = new knje070Controller;
//var_dump($_REQUEST);
?>
