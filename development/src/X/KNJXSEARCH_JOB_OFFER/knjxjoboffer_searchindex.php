<?php

require_once('for_php7.php');

require_once('knjxjoboffer_searchModel.inc');
require_once('knjxjoboffer_searchQuery.inc');

class knjxjoboffer_searchController extends Controller {
    var $ModelClassName = "knjxjoboffer_searchModel";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "search":
                case "knjxjoboffer_search":
                    $sessionInstance->knjxjoboffer_searchModel();
                    $this->callView("knjxjoboffer_search");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxjoboffer_searchCtl = new knjxjoboffer_searchController;
var_dump($_REQUEST);
?>
