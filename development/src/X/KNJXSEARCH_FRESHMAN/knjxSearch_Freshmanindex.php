<?php

require_once('for_php7.php');

require_once('knjxSearch_FreshmanModel.inc');
require_once('knjxSearch_FreshmanQuery.inc');

class knjxSearch_FreshmanController extends Controller {
    var $ModelClassName = "knjxSearch_FreshmanModel";
    var $ProgramID      = "KNJXSEARCH_FRESHMAN";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "search":
                    $this->callView("knjxSearch_FreshmanForm1");
                    break 2;
                case "search_view":	//検索画面
                    $this->callView("knjxSearch_Freshman");
                    break 2;
                case "":
                    $this->callView("knjxSearch_FreshmanForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxSearch_FreshmanCtl = new knjxSearch_FreshmanController;
//var_dump($_REQUEST);
?>
