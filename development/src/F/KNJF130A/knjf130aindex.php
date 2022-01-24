<?php

require_once('for_php7.php');

require_once('knjf130aModel.inc');
require_once('knjf130aQuery.inc');

class knjf130aController extends Controller {
    var $ModelClassName = "knjf130aModel";
    var $ProgramID      = "KNJF130A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf130a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf130aModel();      //コントロールマスタの呼び出し
                    $this->callView("knjf130aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf130aForm1");
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
$knjf130aCtl = new knjf130aController;
//var_dump($_REQUEST);
?>
