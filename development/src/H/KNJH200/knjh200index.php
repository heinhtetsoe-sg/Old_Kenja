<?php

require_once('for_php7.php');

require_once('knjh200Model.inc');
require_once('knjh200Query.inc');

class knjh200Controller extends Controller {
    var $ModelClassName = "knjh200Model";
    var $ProgramID      = "KNJH200";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjh200Model();       //コントロールマスタの呼び出し
                    $this->callView("knjh200Form1");
                    exit;
                case "knjh200":
                case "change":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh200Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjh200Form1");
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
$knjh200Ctl = new knjh200Controller;
?>
