<?php

require_once('for_php7.php');

require_once('knjd186mModel.inc');
require_once('knjd186mQuery.inc');

class knjd186mController extends Controller {
    var $ModelClassName = "knjd186mModel";
    var $ProgramID      = "KNJD186M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd186mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd186mForm1");
                    exit;
                case "knjd186m":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd186mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd186mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd186mForm1");
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
$knjd186mCtl = new knjd186mController;
//var_dump($_REQUEST);
?>
