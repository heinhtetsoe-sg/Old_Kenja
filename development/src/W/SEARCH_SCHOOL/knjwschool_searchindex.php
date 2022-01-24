<?php

require_once('for_php7.php');

require_once('knjwschool_searchModel.inc');
require_once('knjwschool_searchQuery.inc');

class knjwschool_searchController extends Controller {
    var $ModelClassName = "knjwschool_searchModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "search":
                case "knjwschool_search":
                    $sessionInstance->knjwschool_searchModel();
                    $this->callView("knjwschool_search");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjwschool_searchCtl = new knjwschool_searchController;
var_dump($_REQUEST);
?>
