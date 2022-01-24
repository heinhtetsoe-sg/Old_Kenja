<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: index.php 56591 2017-10-22 13:04:39Z maeshiro $
require_once('knjxsearch8Model.inc');
require_once('knjxsearch8Query.inc');

class knjxsearch8Controller extends Controller {
    var $ModelClassName = "knjxsearch8Model";
    var $ProgramID      = "KNJXSEARCH8";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "search":
                    $this->callView("knjxsearch8Form1");
                    break 2;
                case "search_view":	//検索画面
                    $this->callView("knjxSearch8");
                    break 2;
                case "":
                    $this->callView("knjxsearch8Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxsearch8Ctl = new knjxsearch8Controller;
//var_dump($_REQUEST);
?>
