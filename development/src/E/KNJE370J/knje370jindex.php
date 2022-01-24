<?php

require_once('for_php7.php');

require_once('knje370jModel.inc');
require_once('knje370jQuery.inc');

class knje370jController extends Controller {
    var $ModelClassName = "knje370jModel";
    var $ProgramID      = "KNJE370J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje370j":                                //メニュー画面もしくはSUBMITした場合
                case "changeGradeHr":
                    $sessionInstance->knje370jModel();        //コントロールマスタの呼び出し
                    $this->callView("knje370jForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje370jForm1");
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
$knje370jCtl = new knje370jController;
//var_dump($_REQUEST);
?>
