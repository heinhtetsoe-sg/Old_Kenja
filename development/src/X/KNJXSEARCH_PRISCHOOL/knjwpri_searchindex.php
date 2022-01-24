<?php

require_once('for_php7.php');

require_once('knjwpri_searchModel.inc');
require_once('knjwpri_searchQuery.inc');

class knjwpri_searchController extends Controller {
    var $ModelClassName = "knjwpri_searchModel";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "searchMain2":
                case "searchMain":
                case "search":
                case "knjwpri_search":
                    $sessionInstance->knjwpri_searchModel();
                    $this->callView("knjwpri_search");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjwpri_searchCtl = new knjwpri_searchController;
var_dump($_REQUEST);
?>
