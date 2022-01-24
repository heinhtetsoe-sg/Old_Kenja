<?php

require_once('for_php7.php');

require_once('knjp973Model.inc');
require_once('knjp973Query.inc');

class knjp973Controller extends Controller {
    var $ModelClassName = "knjp973Model";
    var $ProgramID      = "KNJP973";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp973":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp973Model();      //コントロールマスタの呼び出し
                    $this->callView("knjp973Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp973Form1");
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
$knjp973Ctl = new knjp973Controller;
?>
