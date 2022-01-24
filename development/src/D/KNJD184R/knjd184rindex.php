<?php

require_once('for_php7.php');

require_once('knjd184rModel.inc');
require_once('knjd184rQuery.inc');

class knjd184rController extends Controller {
    var $ModelClassName = "knjd184rModel";
    var $ProgramID      = "KNJD184R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd184rModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd184rForm1");
                    exit;
                case "knjd184r":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd184rModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd184rForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd184rForm1");
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
$knjd184rCtl = new knjd184rController;
//var_dump($_REQUEST);
?>
