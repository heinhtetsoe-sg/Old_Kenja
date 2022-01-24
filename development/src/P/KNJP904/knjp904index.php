<?php

require_once('for_php7.php');

require_once('knjp904Model.inc');
require_once('knjp904Query.inc');

class knjp904Controller extends Controller {
    var $ModelClassName = "knjp904Model";
    var $ProgramID      = "KNJP904";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjp904":
                case "clear":
                case "search":
                    $sessionInstance->knjp904Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp904Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp904Ctl = new knjp904Controller;
?>
