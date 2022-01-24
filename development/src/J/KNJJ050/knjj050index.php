<?php

require_once('for_php7.php');

require_once('knjj050Model.inc');
require_once('knjj050Query.inc');

class knjj050Controller extends Controller {
    var $ModelClassName = "knjj050Model";
    var $ProgramID      = "KNJJ050";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj050":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjj050Model();        //コントロールマスタの呼び出し
                    $this->callView("knjj050Form1");
                    exit;
                case "csv":         //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjj050Form1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj050Ctl = new knjj050Controller;
//var_dump($_REQUEST);
?>
