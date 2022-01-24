<?php

require_once('for_php7.php');

require_once('knjosearchModel.inc');
require_once('knjosearchQuery.inc');

class knjosearchController extends Controller {
    var $ModelClassName = "knjosearchModel";
    var $ProgramID      = "KNJOSEARCH";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "search":
                    $this->callView("knjosearchForm1");
                    break 2;
                // case "search_view":	//検索画面
                //     $this->callView("knjoSearch");
                //     break 2;
                case "":
                    $this->callView("knjosearchForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjosearchCtl = new knjosearchController;
//var_dump($_REQUEST);
?>
