<?php

require_once('for_php7.php');

require_once('knje131tModel.inc');
require_once('knje131tQuery.inc');

class knje131tController extends Controller {
    var $ModelClassName = "knje131tModel";
    var $ProgramID      = "KNJE131T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje131t":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje131tModel();        //コントロールマスタの呼び出し
                    $this->callView("knje131tForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje131tForm1");
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
$knje131tCtl = new knje131tController;
//var_dump($_REQUEST);
?>
