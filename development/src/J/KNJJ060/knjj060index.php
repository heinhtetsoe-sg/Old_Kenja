<?php

require_once('for_php7.php');

require_once('knjj060Model.inc');
require_once('knjj060Query.inc');

class knjj060Controller extends Controller {
    var $ModelClassName = "knjj060Model";
    var $ProgramID      = "KNJJ060";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj060":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjj060Model();       //コントロールマスタの呼び出し
                    $this->callView("knjj060Form1");
                    exit;
                case "csv":         //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjj060Form1");
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
$knjj060Ctl = new knjj060Controller;
//var_dump($_REQUEST);
?>
