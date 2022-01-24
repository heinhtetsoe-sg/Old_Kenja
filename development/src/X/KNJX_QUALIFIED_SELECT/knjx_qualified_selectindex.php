<?php

require_once('for_php7.php');

require_once('knjx_qualified_selectModel.inc');
require_once('knjx_qualified_selectQuery.inc');

class knjx_qualified_selectController extends Controller {
    var $ModelClassName = "knjx_qualified_selectModel";
    var $ProgramID      = "KNJX_QUALIFIED_SELECT";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                    $this->callView("knjx_qualified_selectForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_qualified_selectCtl = new knjx_qualified_selectController;
?>
