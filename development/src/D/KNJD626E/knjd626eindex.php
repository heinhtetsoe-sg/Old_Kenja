<?php

require_once('for_php7.php');

require_once('knjd626eModel.inc');
require_once('knjd626eQuery.inc');

class knjd626eController extends Controller {
    var $ModelClassName = "knjd626eModel";
    var $ProgramID      = "KNJD626E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd626eModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd626eForm1");
                    exit;
                case "knjd626eChangeGroupDiv":
                case "knjd626e":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd626eModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd626eForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd626eForm1");
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
$knjd626eCtl = new knjd626eController;
//var_dump($_REQUEST);
?>
