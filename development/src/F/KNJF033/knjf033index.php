<?php

require_once('for_php7.php');

require_once('knjf033Model.inc');
require_once('knjf033Query.inc');

class knjf033Controller extends Controller {
    var $ModelClassName = "knjf033Model";
    var $ProgramID      = "KNJF033";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf033":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf033Model();       //コントロールマスタの呼び出し
                    $this->callView("knjf033Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf033Form1");
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
$knjf033Ctl = new knjf033Controller;
?>
