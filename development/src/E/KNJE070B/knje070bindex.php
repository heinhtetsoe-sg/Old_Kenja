<?php

require_once('for_php7.php');

require_once('knje070bModel.inc');
require_once('knje070bQuery.inc');

class knje070bController extends Controller {
    var $ModelClassName = "knje070bModel";
    var $ProgramID      = "KNJE070B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje070b":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje070bModel();       //コントロールマスタの呼び出し
                    $this->callView("knje070bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje070bForm1");
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
$knje070bCtl = new knje070bController;
//var_dump($_REQUEST);
?>
