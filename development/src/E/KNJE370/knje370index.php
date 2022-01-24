<?php

require_once('for_php7.php');

require_once('knje370Model.inc');
require_once('knje370Query.inc');

class knje370Controller extends Controller {
    var $ModelClassName = "knje370Model";
    var $ProgramID      = "KNJE370";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje370":                                //メニュー画面もしくはSUBMITした場合
                case "changeYear":
                case "changeGradeHr":
                    $sessionInstance->knje370Model();        //コントロールマスタの呼び出し
                    $this->callView("knje370Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje370Form1");
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
$knje370Ctl = new knje370Controller;
//var_dump($_REQUEST);
?>
