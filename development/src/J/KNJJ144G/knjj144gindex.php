<?php

require_once('for_php7.php');

require_once('knjj144gModel.inc');
require_once('knjj144gQuery.inc');

class knjj144gController extends Controller {
    var $ModelClassName = "knjj144gModel";
    var $ProgramID      = "KNJJ144G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjj144gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjj144gForm1");
                    exit;
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjj144gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjj144gForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjj144gForm1");
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
$knjj144gCtl = new knjj144gController;
?>
