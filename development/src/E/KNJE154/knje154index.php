<?php

require_once('for_php7.php');

require_once('knje154Model.inc');
require_once('knje154Query.inc');

class knje154Controller extends Controller {
    var $ModelClassName = "knje154Model";
    var $ProgramID      = "KNJE154";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje154":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje154Model();        //コントロールマスタの呼び出し
                    $this->callView("knje154Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje154Form1");
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
$knje154Ctl = new knje154Controller;
//var_dump($_REQUEST);
?>
