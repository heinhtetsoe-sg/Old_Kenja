<?php

require_once('for_php7.php');

require_once('knjxexp_prischoolModel.inc');
require_once('knjxexp_prischoolQuery.inc');

class knjxexp_prischoolController extends Controller {
    var $ModelClassName = "knjxexp_prischoolModel";
    var $ProgramID      = "KNJXEXP_PRISCHOOL";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "edit":
                case "select":
                case "search":
                case "search2":
                case "priChange":
                case "searchUpd":
                    $this->callView("knjxexp_prischoolForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("list");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxexp_prischoolCtl = new knjxexp_prischoolController;
//var_dump($_REQUEST);
?>
