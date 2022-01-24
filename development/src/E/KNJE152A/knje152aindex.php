<?php

require_once('for_php7.php');

require_once('knje152aModel.inc');
require_once('knje152aQuery.inc');

class knje152aController extends Controller {
    var $ModelClassName = "knje152aModel";
    var $ProgramID      = "KNJE152A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knje152a":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje152aModel();       //コントロールマスタの呼び出し
                    $this->callView("knje152aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje152aForm1");
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
$knje152aCtl = new knje152aController;
//var_dump($_REQUEST);
?>
