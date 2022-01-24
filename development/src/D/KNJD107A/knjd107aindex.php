<?php

require_once('for_php7.php');

require_once('knjd107aModel.inc');
require_once('knjd107aQuery.inc');

class knjd107aController extends Controller {
    var $ModelClassName = "knjd107aModel";
    var $ProgramID      = "KNJD107A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd107a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd107aModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd107aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd107aForm1");
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
$knjd107aCtl = new knjd107aController;
//var_dump($_REQUEST);
?>
