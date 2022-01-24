<?php

require_once('for_php7.php');

require_once('knje156Model.inc');
require_once('knje156Query.inc');

class knje156Controller extends Controller {
    var $ModelClassName = "knje156Model";
    var $ProgramID      = "KNJE156";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje156":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje156Model();        //コントロールマスタの呼び出し
                    $this->callView("knje156Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje156Form1");
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
$knje156Ctl = new knje156Controller;
//var_dump($_REQUEST);
?>
