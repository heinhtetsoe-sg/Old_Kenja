<?php

require_once('for_php7.php');

require_once('knjxjobtypes_searchModel.inc');
require_once('knjxjobtypes_searchQuery.inc');

class knjxjobtypes_searchController extends Controller {
    var $ModelClassName = "knjxjobtypes_searchModel";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjxjobtypes_search":
                    $sessionInstance->knjxjobtypes_searchModel();
                    $this->callView("knjxjobtypes_search");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxjobtypes_searchCtl = new knjxjobtypes_searchController;
var_dump($_REQUEST);
?>
