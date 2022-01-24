<?php

require_once('for_php7.php');

require_once('knjd658bModel.inc');
require_once('knjd658bQuery.inc');

class knjd658bController extends Controller {
    var $ModelClassName = "knjd658bModel";
    var $ProgramID      = "KNJD658B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd658b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd658bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd658bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd658bForm1");
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
$knjd658bCtl = new knjd658bController;
//var_dump($_REQUEST);
?>
