<?php

require_once('for_php7.php');

require_once('knjxcol_searchModel.inc');
require_once('knjxcol_searchQuery.inc');

class knjxcol_searchController extends Controller {
    var $ModelClassName = "knjxcol_searchModel";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "search":
                case "knjxcol_search":
                    $sessionInstance->knjxcol_searchModel();
                    $this->callView("knjxcol_search");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxcol_searchCtl = new knjxcol_searchController;
var_dump($_REQUEST);
?>
