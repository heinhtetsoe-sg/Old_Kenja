<?php

require_once('for_php7.php');

require_once('knjc200_searchModel.inc');
require_once('knjc200_searchQuery.inc');

class knjc200_searchController extends Controller {
    var $ModelClassName = "knjc200_searchModel";
    var $ProgramID      = "KNJC200";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "search":
                case "knjc200_search":
                    $sessionInstance->setAccessLogDetail("S", "KNJC200_SEARCH"); 
                    $sessionInstance->knjc200_searchModel();
                    $this->callView("knjc200_search");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc200_searchCtl = new knjc200_searchController;
var_dump($_REQUEST);
?>
