<?php

require_once('for_php7.php');

require_once('knjd659bModel.inc');
require_once('knjd659bQuery.inc');

class knjd659bController extends Controller {
    var $ModelClassName = "knjd659bModel";
    var $ProgramID      = "KNJD659B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd659b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd659bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd659bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd659bForm1");
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
$knjd659bCtl = new knjd659bController;
//var_dump($_REQUEST);
?>
