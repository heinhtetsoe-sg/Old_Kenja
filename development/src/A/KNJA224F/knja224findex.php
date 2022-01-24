<?php

require_once('for_php7.php');

require_once('knja224fModel.inc');
require_once('knja224fQuery.inc');

class knja224fController extends Controller {
    var $ModelClassName = "knja224fModel";
    var $ProgramID      = "KNJA224F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja224f":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja224fModel();        //コントロールマスタの呼び出し
                    $this->callView("knja224fForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja224fForm1");
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
$knja224fCtl = new knja224fController;
//var_dump($_REQUEST);
?>
