<?php
require_once('knje070gModel.inc');
require_once('knje070gQuery.inc');

class knje070gController extends Controller {
    var $ModelClassName = "knje070gModel";
    var $ProgramID      = "KNJE070G";

    function main()
    {
        $sessionInstance =& Model::getModel();
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje070g":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje070gModel();       //コントロールマスタの呼び出し
                    $this->callView("knje070gForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje070gForm1");
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
$knje070gCtl = new knje070gController;
//var_dump($_REQUEST);
?>
