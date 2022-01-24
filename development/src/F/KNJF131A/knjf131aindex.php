<?php

require_once('for_php7.php');

require_once('knjf131aModel.inc');
require_once('knjf131aQuery.inc');

class knjf131aController extends Controller {
    var $ModelClassName = "knjf131aModel";
    var $ProgramID      = "KNJF131A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf131a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf131aModel();      //コントロールマスタの呼び出し
                    $this->callView("knjf131aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf131aForm1");
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
$knjf131aCtl = new knjf131aController;
//var_dump($_REQUEST);
?>
