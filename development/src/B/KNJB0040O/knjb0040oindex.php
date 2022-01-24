<?php
// kanji=漢字
// $Id: knjb0040oindex.php,v 1.3 2004/10/13 12:59:51 tamura Exp $
require_once('knjb0040oModel.inc');
require_once('knjb0040oQuery.inc');

class knjb0040oController extends Controller {
    var $ModelClassName = "knjb0040oModel";
    var $ProgramID      = "KNJB0040o";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb0040o":
                    $sessionInstance->knjb0040oModel();
                    $this->callView("knjb0040oForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb0040oCtl = new knjb0040oController;
//var_dump($_REQUEST);
?>
