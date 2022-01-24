<?php

require_once('for_php7.php');

require_once('knjd615mModel.inc');
require_once('knjd615mQuery.inc');

class knjd615mController extends Controller {
    var $ModelClassName = "knjd615mModel";
    var $ProgramID      = "KNJD615M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd615mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615mForm1");
                    exit;
                case "knjd615m":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd615mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd615mForm1");
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
$knjd615mCtl = new knjd615mController;
//var_dump($_REQUEST);
?>
