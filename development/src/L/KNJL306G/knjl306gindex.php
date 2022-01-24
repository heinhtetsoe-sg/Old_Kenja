<?php

require_once('for_php7.php');

require_once('knjl306gModel.inc');
require_once('knjl306gQuery.inc');

class knjl306gController extends Controller {
    var $ModelClassName = "knjl306gModel";
    var $ProgramID      = "KNJL306G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl306g":
                    $sessionInstance->knjl306gModel();
                    $this->callView("knjl306gForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl306gForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl306gCtl = new knjl306gController;
//var_dump($_REQUEST);
?>
