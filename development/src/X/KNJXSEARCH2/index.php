<?php

require_once('for_php7.php');

require_once('knjxsearch2Model.inc');
require_once('knjxsearch2Query.inc');

class knjxsearch2Controller extends Controller {
    var $ModelClassName = "knjxsearch2Model";
    var $ProgramID      = "KNJXSEARCH2";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "search":
                    $this->callView("knjxsearch2Form1");
                    break 2;
                case "search_view":	//検索画面
                    $this->callView("knjxSearch2");
                    break 2;
                case "":
                    $this->callView("knjxsearch2Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxsearch2Ctl = new knjxsearch2Controller;
//var_dump($_REQUEST);
?>
