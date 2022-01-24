<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: index.php 56591 2017-10-22 13:04:39Z maeshiro $
require_once('knjxsearch9Model.inc');
require_once('knjxsearch9Query.inc');

class knjxsearch9Controller extends Controller {
    var $ModelClassName = "knjxsearch9Model";
    var $ProgramID      = "KNJXSEARCH9";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "search":
                    $this->callView("knjxsearch9Form1");
                    break 2;
                case "search_view":	//検索画面
                    $this->callView("knjxSearch9");
                    break 2;
                case "":
                    $this->callView("knjxsearch9Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxsearch9Ctl = new knjxsearch9Controller;
//var_dump($_REQUEST);
?>
