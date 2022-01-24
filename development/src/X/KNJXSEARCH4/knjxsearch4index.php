<?php

require_once('for_php7.php');

require_once('knjxsearch4Model.inc');
require_once('knjxsearch4Query.inc');

class knjxsearch4Controller extends Controller {
    var $ModelClassName = "knjxsearch4Model";
    var $ProgramID      = "KNJXSEARCH4";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjxsearch4Form1");
                    break 2;
                case "search_view":	    //検索画面
                case "search_view2":	//検索画面（卒業年度）
                    $this->callView("knjxSearch");
                    break 2;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjxsearch4Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxsearch4Ctl = new knjxsearch4Controller;
//var_dump($_REQUEST);
?>
