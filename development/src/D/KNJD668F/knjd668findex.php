<?php

require_once('for_php7.php');

require_once('knjd668fModel.inc');
require_once('knjd668fQuery.inc');

class knjd668fController extends Controller {
    var $ModelClassName = "knjd668fModel";
    var $ProgramID      = "KNJD668F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd668f":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd668fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd668fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd668fForm1");
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
$knjd668fCtl = new knjd668fController;
//var_dump($_REQUEST);
?>
