<?php

require_once('for_php7.php');

require_once('knje130iModel.inc');
require_once('knje130iQuery.inc');

class knje130iController extends Controller {
    var $ModelClassName = "knje130iModel";
    var $ProgramID      = "KNJE130I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knje130iModel();        //コントロールマスタの呼び出し
                    $this->callView("knje130iForm1");
                    exit;
                case "change_grade":
                case "knje130i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knje130iModel();        //コントロールマスタの呼び出し
                    $this->callView("knje130iForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje130iForm1");
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
$knje130iCtl = new knje130iController;
//var_dump($_REQUEST);
?>
