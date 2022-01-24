<?php

require_once('for_php7.php');

require_once('knjd186gModel.inc');
require_once('knjd186gQuery.inc');

class knjd186gController extends Controller {
    var $ModelClassName = "knjd186gModel";
    var $ProgramID      = "KNJD186G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd186gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd186gForm1");
                    exit;
                case "knjd186g":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd186gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd186gForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd186gForm1");
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
$knjd186gCtl = new knjd186gController;
//var_dump($_REQUEST);
?>
