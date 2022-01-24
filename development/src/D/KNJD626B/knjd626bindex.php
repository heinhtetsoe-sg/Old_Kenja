<?php

require_once('for_php7.php');

require_once('knjd626bModel.inc');
require_once('knjd626bQuery.inc');

class knjd626bController extends Controller {
    var $ModelClassName = "knjd626bModel";
    var $ProgramID      = "KNJD626B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd626bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd626bForm1");
                    exit;
                case "change_grade":
                case "knjd626b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd626bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd626bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd626bForm1");
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
$knjd626bCtl = new knjd626bController;
//var_dump($_REQUEST);
?>
