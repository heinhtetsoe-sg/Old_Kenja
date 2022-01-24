<?php

require_once('for_php7.php');

require_once('knjj143Model.inc');
require_once('knjj143Query.inc');

class knjj143Controller extends Controller {
    var $ModelClassName = "knjj143Model";
    var $ProgramID      = "KNJJ143";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj143":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjj143Model();       //コントロールマスタの呼び出し
                    $this->callView("knjj143Form1");
                    exit;
                case "csv_council":
                case "csv_committee":
                    $sessionInstance->downloadCsvFile(trim($sessionInstance->cmd));
                    $sessionInstance->setCmd("");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj143Ctl = new knjj143Controller;
?>
