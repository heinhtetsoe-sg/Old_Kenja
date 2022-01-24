<?php
// kanji=漢字
// $Id: knjb3040oindex.php,v $
require_once('knjb3040oModel.inc');
require_once('knjb3040oQuery.inc');

class knjb3040oController extends Controller {
    var $ModelClassName = "knjb3040oModel";
    var $ProgramID      = "KNJB3040";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb3040o":
                    $sessionInstance->knjb3040oModel();
                    $this->callView("knjb3040oForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb3040oCtl = new knjb3040oController;
//var_dump($_REQUEST);
?>
