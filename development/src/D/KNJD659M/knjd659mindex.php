<?php

require_once('for_php7.php');

require_once('knjd659mModel.inc');
require_once('knjd659mQuery.inc');

class knjd659mController extends Controller {
    var $ModelClassName = "knjd659mModel";
    var $ProgramID      = "KNJD659M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd659mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd659mForm1");
                    exit;
                case "knjd659m":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd659mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd659mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd659mForm1");
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
$knjd659mCtl = new knjd659mController;
//var_dump($_REQUEST);
?>
