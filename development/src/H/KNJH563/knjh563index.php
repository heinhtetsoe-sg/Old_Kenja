<?php

require_once('for_php7.php');

require_once('knjh563Model.inc');
require_once('knjh563Query.inc');

class knjh563Controller extends Controller {
    var $ModelClassName = "knjh563Model";
    var $ProgramID      = "KNJH563";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh563":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh563Model();       //コントロールマスタの呼び出し
                    $this->callView("knjh563Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjh563Form1");
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
$knjh563Ctl = new knjh563Controller;
//var_dump($_REQUEST);
?>
