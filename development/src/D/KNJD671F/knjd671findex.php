<?php

require_once('for_php7.php');

require_once('knjd671fModel.inc');
require_once('knjd671fQuery.inc');

class knjd671fController extends Controller {
    var $ModelClassName = "knjd671fModel";
    var $ProgramID      = "KNJD671F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd671f":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd671fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd671fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd671fForm1");
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
$knjd671fCtl = new knjd671fController;
//var_dump($_REQUEST);
?>
