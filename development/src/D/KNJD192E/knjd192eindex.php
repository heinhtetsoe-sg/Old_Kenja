<?php

require_once('for_php7.php');

require_once('knjd192eModel.inc');
require_once('knjd192eQuery.inc');

class knjd192eController extends Controller {
    var $ModelClassName = "knjd192eModel";
    var $ProgramID      = "KNJD192E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd192eModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192eForm1");
                    exit;
                case "change_grade":
                case "knjd192e":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd192eModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192eForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192eForm1");
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
$knjd192eCtl = new knjd192eController;
//var_dump($_REQUEST);
?>
