<?php

require_once('for_php7.php');

require_once('knjxindustrys_searchModel.inc');
require_once('knjxindustrys_searchQuery.inc');

class knjxindustrys_searchController extends Controller {
    var $ModelClassName = "knjxindustrys_searchModel";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "search":
                case "knjxindustrys_search":
                    $sessionInstance->knjxindustrys_searchModel();
                    $this->callView("knjxindustrys_search");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxindustrys_searchCtl = new knjxindustrys_searchController;
var_dump($_REQUEST);
?>
