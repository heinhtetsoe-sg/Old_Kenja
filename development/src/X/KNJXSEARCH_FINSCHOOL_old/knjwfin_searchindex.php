<?php

require_once('for_php7.php');

require_once('knjwfin_searchModel.inc');
require_once('knjwfin_searchQuery.inc');

class knjwfin_searchController extends Controller {
    var $ModelClassName = "knjwfin_searchModel";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "searchMain":
                case "search":
                case "knjwfin_search":
                    $sessionInstance->knjwfin_searchModel();
                    $this->callView("knjwfin_search");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjwfin_searchCtl = new knjwfin_searchController;
var_dump($_REQUEST);
?>
