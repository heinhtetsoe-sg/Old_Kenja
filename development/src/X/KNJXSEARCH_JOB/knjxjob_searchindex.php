<?php

require_once('for_php7.php');

require_once('knjxjob_searchModel.inc');
require_once('knjxjob_searchQuery.inc');

class knjxjob_searchController extends Controller {
    var $ModelClassName = "knjxjob_searchModel";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "search":
                case "knjxjob_search":
                    $sessionInstance->knjxjob_searchModel();
                    $this->callView("knjxjob_search");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxjob_searchCtl = new knjxjob_searchController;
var_dump($_REQUEST);
?>
