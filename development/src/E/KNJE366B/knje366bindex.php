<?php

require_once('for_php7.php');

require_once('knje366bModel.inc');
require_once('knje366bQuery.inc');

class knje366bController extends Controller {
    var $ModelClassName = "knje366bModel";
    var $ProgramID      = "KNJE366B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                // case "knje366b":                                //メニュー画面もしくはSUBMITした場合
                case "changeYear":
                    $sessionInstance->knje366bModel();        //コントロールマスタの呼び出し
                    $this->callView("knje366bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje366bForm1");
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
$knje366bCtl = new knje366bController;
//var_dump($_REQUEST);
?>
