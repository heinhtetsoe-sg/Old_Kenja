<?php

require_once('for_php7.php');

require_once('knjd184cModel.inc');
require_once('knjd184cQuery.inc');

class knjd184cController extends Controller {
    var $ModelClassName = "knjd184cModel";
    var $ProgramID      = "KNJD184C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd184cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd184cForm1");
                    exit;
                case "knjd184c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd184cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd184cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd184cForm1");
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
$knjd184cCtl = new knjd184cController;
//var_dump($_REQUEST);
?>
