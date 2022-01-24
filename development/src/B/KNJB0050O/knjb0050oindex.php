<?php
// kanji=漢字
// $Id: knjb0050oindex.php,v 1.3 2009/05/26 08:17:47 takara Exp $
require_once('knjb0050oModel.inc');
require_once('knjb0050oQuery.inc');

class knjb0050oController extends Controller {
    var $ModelClassName = "knjb0050oModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {

                case "":
                case "knjb0050o":
                    $this->callView("knjb0050oForm1");
                    break 2;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb0050oCtl = new knjb0050oController;
//var_dump($_REQUEST);
?>
