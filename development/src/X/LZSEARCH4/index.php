<?php

require_once('for_php7.php');

require_once('lzsearchModel.inc');
require_once('lzsearchQuery.inc');

class lzsearchController extends Controller {
    var $ModelClassName = "lzsearchModel";
    var $ProgramID      = "LZSEARCH";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "search":
                    $this->callView("list");
                    break 2;
                case "search_view":	//��������
                    $this->callView("search");
                    break 2;
                case "":
                    $this->callView("list");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("̤�б��Υ��������{$sessionInstance->cmd}�Ǥ�"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$lzsearchCtl = new lzsearchController;
//var_dump($_REQUEST);
?>
