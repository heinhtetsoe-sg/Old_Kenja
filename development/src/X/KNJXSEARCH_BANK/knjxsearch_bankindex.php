<?php

require_once('for_php7.php');

require_once('knjxsearch_bankModel.inc');
require_once('knjxsearch_bankQuery.inc');

class knjxsearch_bankController extends Controller {
    var $ModelClassName = "knjxsearch_bankModel";
    var $ProgramID      = "KNJXSEARCH_BANK";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "chg_grade":
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjxsearch_bankForm1");
                    break 2;
                case "search_view":	    //検索画面
                case "search_view2":	//検索画面（卒業年度）
                    $this->callView("knjxSearch");
                    break 2;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->setCmd("chg_grade");
                    $this->callView("knjxsearch_bankForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxsearch_bankCtl = new knjxsearch_bankController;
//var_dump($_REQUEST);
?>
