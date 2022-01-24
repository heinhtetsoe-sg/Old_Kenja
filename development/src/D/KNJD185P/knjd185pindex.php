<?php

require_once('for_php7.php');

require_once('knjd185pModel.inc');
require_once('knjd185pQuery.inc');

class knjd185pController extends Controller {
    var $ModelClassName = "knjd185pModel";
    var $ProgramID      = "KNJD185P";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd185pModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185pForm1");
                    exit;
                case "knjd185p":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd185pModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185pForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd185pForm1");
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
$knjd185pCtl = new knjd185pController;
//var_dump($_REQUEST);
?>
