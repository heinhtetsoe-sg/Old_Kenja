<?php

require_once('for_php7.php');

require_once('knjxsearch7Model.inc');
require_once('knjxsearch7Query.inc');

class knjxsearch7Controller extends Controller {
    var $ModelClassName = "knjxsearch7Model";
    var $ProgramID      = "KNJXSEARCH7";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "search":
                    $this->callView("knjxsearch7Form1");
                    break 2;
                case "search_view":	    //検索画面
                case "search_view2":	//検索画面（最終年度）
                    $this->callView("knjxSearch7");
                    break 2;
                case "":
                    $this->callView("knjxsearch7Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxsearch7Ctl = new knjxsearch7Controller;
//var_dump($_REQUEST);
?>
