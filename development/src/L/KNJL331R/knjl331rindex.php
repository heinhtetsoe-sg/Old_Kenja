<?php

require_once('for_php7.php');

require_once('knjl331rModel.inc');
require_once('knjl331rQuery.inc');

class knjl331rController extends Controller {
    var $ModelClassName = "knjl331rModel";
    var $ProgramID      = "KNJL331R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl331rForm1");
                    }
                    break 2;
                case "":
                case "knjl331r":
                    $sessionInstance->knjl331rModel();
                    $this->callView("knjl331rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl331rCtl = new knjl331rController;
//var_dump($_REQUEST);
?>
