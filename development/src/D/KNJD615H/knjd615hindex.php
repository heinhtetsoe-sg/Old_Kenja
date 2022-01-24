<?php

require_once('for_php7.php');

require_once('knjd615hModel.inc');
require_once('knjd615hQuery.inc');

class knjd615hController extends Controller {
    var $ModelClassName = "knjd615hModel";
    var $ProgramID      = "KNJD615H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd615hModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615hForm1");
                    exit;
                case "knjd615h":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd615hModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615hForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd615hForm1");
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
$knjd615hCtl = new knjd615hController;
//var_dump($_REQUEST);
?>
