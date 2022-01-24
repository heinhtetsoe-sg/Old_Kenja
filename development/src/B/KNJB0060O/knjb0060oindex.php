<?php
// kanji=漢字
// $Id: knjb0060oindex.php,v 1.1 2004/10/13 13:29:39 takaesu Exp $
require_once('knjb0060oModel.inc');
require_once('knjb0060oQuery.inc');

class knjb0060oController extends Controller {
    var $ModelClassName = "knjb0060oModel";
    var $ProgramID      = "KNJB0060O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb0060o":
                    $sessionInstance->knjb0060oModel();
                    $this->callView("knjb0060oForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb0060oCtl = new knjb0060oController;
//var_dump($_REQUEST);
?>
